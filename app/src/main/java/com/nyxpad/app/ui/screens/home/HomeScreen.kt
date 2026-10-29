package com.nyxpad.app.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.data.preferences.SortOption
import com.nyxpad.app.data.preferences.ViewMode
import com.nyxpad.app.domain.model.BuiltInTemplates
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.ui.components.NoteCard
import com.nyxpad.app.ui.components.NyxBottomSheet
import com.nyxpad.app.ui.components.NyxDrawer
import com.nyxpad.app.ui.components.NyxEmptyState
import com.nyxpad.app.ui.components.NyxFab
import com.nyxpad.app.ui.components.NyxSearchBar
import com.nyxpad.app.ui.components.NyxSectionHeader
import com.nyxpad.app.ui.components.NyxTopBar
import com.nyxpad.app.ui.components.PullRefreshLayout
import com.nyxpad.app.ui.components.SheetHeader
import com.nyxpad.app.ui.navigation.Screen
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxError
import com.nyxpad.app.ui.theme.NyxLabel
import com.nyxpad.app.ui.theme.moodColor
import com.nyxpad.app.ui.theme.tagColor
import com.nyxpad.app.utils.BiometricUtils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.nyxpad.app.utils.ClipboardUtils
import com.nyxpad.app.utils.ExportUtils
import com.nyxpad.app.utils.QrCodeUtils
import com.nyxpad.app.utils.findFragmentActivity
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    pinnedOnly: Boolean,
    onOpenNote: (Long) -> Unit,
    onNewNote: () -> Unit,
    onNavigate: (String) -> Unit,
    scanQrRequest: Boolean = false,
    onScanQrConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var drawerOpen by remember { mutableStateOf(false) }
    var actionNote by remember { mutableStateOf<Note?>(null) }
    var showTemplates by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkTagDialog by remember { mutableStateOf(false) }
    var bulkTagText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val activity = remember(context) { context.findFragmentActivity() }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) {
            val payload = QrCodeUtils.decode(contents)
            viewModel.createNoteFromText(payload.first, payload.second) { id ->
                onOpenNote(id)
            }
        }
    }

    fun launchScanner() {
        scanLauncher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan a NyxPad note")
                setBeepEnabled(false)
                setOrientationLocked(false)
            },
        )
    }

    LaunchedEffect(scanQrRequest) {
        if (scanQrRequest) {
            onScanQrConsumed()
            launchScanner()
        }
    }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    fun toggleSelection(note: Note) {
        selectedIds = if (note.id in selectedIds) {
            selectedIds - note.id
        } else {
            selectedIds + note.id
        }
        if (selectedIds.isEmpty()) selectionMode = false
    }

    fun deleteWithUndo(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModel.bulkDelete(ids)
        exitSelection()
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = if (ids.size == 1) "Moved to trash" else "Moved ${ids.size} notes to trash",
                actionLabel = "Undo",
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreNotes(ids)
            }
        }
    }

    fun openNote(note: Note) {
        if (!note.isLocked) {
            onOpenNote(note.id)
            return
        }
        val host = activity
        if (host == null) {
            Toast.makeText(context, "Unable to show a security prompt.", Toast.LENGTH_SHORT).show()
            return
        }
        BiometricUtils.authenticate(
            activity = host,
            title = "Unlock note",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = { onOpenNote(note.id) },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
    }

    fun openActions(note: Note) {
        if (!note.isLocked) {
            actionNote = note
            return
        }
        val host = activity
        if (host == null) {
            Toast.makeText(context, "Unable to show a security prompt.", Toast.LENGTH_SHORT).show()
            return
        }
        BiometricUtils.authenticate(
            activity = host,
            title = "Unlock note",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = { actionNote = note },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
    }

    fun handleNoteClick(note: Note) {
        if (selectionMode) toggleSelection(note) else openNote(note)
    }

    fun handleNoteLongPress(note: Note) {
        if (selectionMode) {
            toggleSelection(note)
        } else {
            selectionMode = true
            selectedIds = setOf(note.id)
        }
    }

    fun toggleNoteLock(note: Note) {
        val host = activity
        if (host == null) {
            Toast.makeText(context, "Unable to show a security prompt.", Toast.LENGTH_SHORT).show()
            return
        }
        BiometricUtils.authenticate(
            activity = host,
            title = if (note.isLocked) "Unlock note" else "Lock note",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = {
                viewModel.setLocked(note, !note.isLocked)
                actionNote = actionNote?.copy(isLocked = !note.isLocked)
                Toast.makeText(
                    context,
                    if (note.isLocked) "Note unlocked" else "Note locked",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
    }

    BackHandler(enabled = selectionMode || state.searchActive) {
        if (selectionMode) exitSelection() else viewModel.closeSearch()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            if (selectionMode) {
                SelectionTopBar(
                    count = selectedIds.size,
                    allSelected = selectedIds.size == state.notes.size && state.notes.isNotEmpty(),
                    onClose = ::exitSelection,
                    onSelectAll = {
                        if (selectedIds.size == state.notes.size) {
                            exitSelection()
                        } else {
                            selectedIds = state.notes.map { it.id }.toSet()
                        }
                    },
                    onPin = {
                        viewModel.bulkPin(selectedIds.toList(), true)
                        exitSelection()
                    },
                    onArchive = {
                        viewModel.bulkArchive(selectedIds.toList())
                        exitSelection()
                    },
                    onTag = {
                        bulkTagText = ""
                        showBulkTagDialog = true
                    },
                    onDelete = { deleteWithUndo(selectedIds.toList()) },
                )
            } else if (state.searchActive) {
                NyxSearchBar(
                    query = state.query,
                    onQueryChange = viewModel::setQuery,
                    onClose = viewModel::closeSearch,
                )
            } else {
                NyxTopBar(
                    streak = state.settings.currentStreak,
                    viewMode = state.settings.viewMode,
                    searchActive = state.searchActive,
                    onMenuClick = { drawerOpen = true },
                    onSearchClick = viewModel::openSearch,
                    onToggleView = {
                        viewModel.setViewMode(
                            if (state.settings.viewMode == ViewMode.Grid) {
                                ViewMode.List
                            } else {
                                ViewMode.Grid
                            },
                        )
                    },
                    onSortClick = { showSort = true },
                )
            }

            if (!selectionMode && !state.searchActive && state.allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TagFilterChip(
                        label = "All",
                        selected = state.selectedTag == null,
                        onClick = { viewModel.setSelectedTag(null) },
                    )
                    state.allTags.forEach { tag ->
                        TagFilterChip(
                            label = "#$tag",
                            selected = state.selectedTag == tag,
                            onClick = {
                                viewModel.setSelectedTag(
                                    if (state.selectedTag == tag) null else tag,
                                )
                            },
                        )
                    }
                }
            }

            PullRefreshLayout(
                refreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                val visiblePinned = if (pinnedOnly) state.pinnedNotes else state.pinnedNotes
                val showPinnedSection = !pinnedOnly &&
                    state.query.isBlank() &&
                    visiblePinned.isNotEmpty()
                val otherNotes = if (showPinnedSection) {
                    state.notes.filterNot { it.isPinned }
                } else {
                    state.notes
                }
                val isEmpty = if (pinnedOnly) {
                    visiblePinned.isEmpty()
                } else {
                    state.notes.isEmpty()
                }

                if (isEmpty && !state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 148.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val title = when {
                            pinnedOnly -> "Nothing pinned yet"
                            state.query.isNotBlank() -> "No notes found"
                            else -> "A blank page, waiting"
                        }
                        val subtitle = when {
                            pinnedOnly -> "Pin the notes you want within reach and they will live here."
                            state.query.isNotBlank() -> "Try another word, or clear the search to see everything."
                            else -> "Every idea starts with one line. Your words never leave this device."
                        }
                        val actionLabel = when {
                            pinnedOnly -> "Browse notes"
                            state.query.isNotBlank() -> "Clear search"
                            else -> "Write your first note"
                        }
                        NyxEmptyState(
                            title = title,
                            subtitle = subtitle,
                            actionLabel = actionLabel,
                            onAction = {
                                when {
                                    pinnedOnly -> onNavigate(Screen.Home.route)
                                    state.query.isNotBlank() -> viewModel.closeSearch()
                                    else -> onNewNote()
                                }
                            },
                        )
                    }
                } else if (state.settings.viewMode == ViewMode.Grid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 168.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 196.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (pinnedOnly) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                NyxSectionHeader(
                                    title = "Pinned notes",
                                    count = visiblePinned.size,
                                )
                            }
                            gridItems(visiblePinned, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    isGrid = true,
                                    onClick = { handleNoteClick(note) },
                                    onLongPress = { handleNoteLongPress(note) },
                                    onMore = { openActions(note) },
                                )
                            }
                        } else {
                            if (showPinnedSection) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    NyxSectionHeader(
                                        title = "Pinned",
                                        count = visiblePinned.size,
                                    )
                                }
                                gridItems(visiblePinned, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        isGrid = true,
                                        onClick = { handleNoteClick(note) },
                                        onLongPress = { handleNoteLongPress(note) },
                                        onMore = { openActions(note) },
                                    )
                                }
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    NyxSectionHeader(
                                        title = "All notes",
                                        count = otherNotes.size,
                                    )
                                }
                            }
                            gridItems(otherNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    isGrid = true,
                                    onClick = { handleNoteClick(note) },
                                    onLongPress = { handleNoteLongPress(note) },
                                    onMore = { openActions(note) },
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 196.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (pinnedOnly) {
                            item {
                                NyxSectionHeader(
                                    title = "Pinned notes",
                                    count = visiblePinned.size,
                                )
                            }
                            listItems(visiblePinned, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    isGrid = false,
                                    onClick = { handleNoteClick(note) },
                                    onLongPress = { handleNoteLongPress(note) },
                                    onMore = { openActions(note) },
                                )
                            }
                        } else {
                            if (showPinnedSection) {
                                item {
                                    NyxSectionHeader(
                                        title = "Pinned",
                                        count = visiblePinned.size,
                                    )
                                }
                                listItems(visiblePinned, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        isGrid = false,
                                        onClick = { handleNoteClick(note) },
                                        onLongPress = { handleNoteLongPress(note) },
                                        onMore = { openActions(note) },
                                    )
                                }
                                item {
                                    NyxSectionHeader(
                                        title = "All notes",
                                        count = otherNotes.size,
                                    )
                                }
                            }
                            listItems(otherNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    isGrid = false,
                                    onClick = { handleNoteClick(note) },
                                    onLongPress = { handleNoteLongPress(note) },
                                    onMore = { openActions(note) },
                                )
                            }
                        }
                    }
                }
            }
        }

        NyxFab(
            onClick = onNewNote,
            onLongClick = { showTemplates = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 96.dp),
        )

        HomeBottomNav(
            currentRoute = if (pinnedOnly) Screen.Pinned.route else Screen.Home.route,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        NyxDrawer(
            open = drawerOpen,
            currentRoute = if (pinnedOnly) Screen.Pinned.route else Screen.Home.route,
            onOpen = { drawerOpen = true },
            onClose = { drawerOpen = false },
            onNavigate = onNavigate,
            onScanQr = ::launchScanner,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 160.dp),
        )
    }

    actionNote?.let { note ->
        NoteActionsSheet(
            note = note,
            onDismiss = { actionNote = null },
            onTogglePin = {
                viewModel.togglePin(note)
                actionNote = null
            },
            onArchive = {
                viewModel.archive(note)
                actionNote = null
                Toast.makeText(context, "Moved to archive", Toast.LENGTH_SHORT).show()
            },
            onDuplicate = {
                viewModel.duplicate(note) { id ->
                    actionNote = null
                    onOpenNote(id)
                }
            },
            onCopy = {
                ClipboardUtils.copyToClipboard(
                    context,
                    note.displayTitle,
                    buildString {
                        appendLine(note.displayTitle)
                        appendLine()
                        append(note.content)
                    },
                )
                actionNote = null
                Toast.makeText(context, "Note copied", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                ExportUtils.shareText(context, note.displayTitle, ExportUtils.plainText(note))
                actionNote = null
            },
            onToggleLock = { toggleNoteLock(note) },
            onDelete = {
                actionNote = null
                deleteWithUndo(listOf(note.id))
            },
            onColorSelected = { tag ->
                viewModel.setColor(note, tag)
                actionNote = note.copy(colorTag = tag)
            },
            onMoodSelected = { mood ->
                viewModel.setMood(note, mood)
                actionNote = note.copy(moodTag = mood)
            },
        )
    }

    if (showTemplates) {
        NyxBottomSheet(onDismissRequest = { showTemplates = false }) {
            SheetHeader(
                title = "Templates",
                subtitle = "Start with a shape, then make it yours",
            )
            BuiltInTemplates.all.forEach { template ->
                TemplateRow(
                    name = template.name,
                    preview = template.content,
                    onClick = {
                        showTemplates = false
                        if (template.title.isBlank() && template.content.isBlank()) {
                            onNewNote()
                        } else {
                            viewModel.createFromTemplate(
                                NoteTemplate(
                                    name = template.name,
                                    titleTemplate = template.title,
                                    contentTemplate = template.content,
                                ),
                            ) { id -> onOpenNote(id) }
                        }
                    },
                )
            }
            if (templates.isNotEmpty()) {
                Text(
                    text = "YOUR TEMPLATES",
                    style = NyxLabel,
                    color = palette.subtext,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                )
                templates.forEach { template ->
                    TemplateRow(
                        name = template.name,
                        preview = template.contentTemplate,
                        onClick = {
                            showTemplates = false
                            viewModel.createFromTemplate(template) { id -> onOpenNote(id) }
                        },
                        onDelete = { viewModel.deleteTemplate(template) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showBulkTagDialog) {
        AlertDialog(
            onDismissRequest = { showBulkTagDialog = false },
            title = { Text("Add tags", color = palette.text) },
            text = {
                Column {
                    Text(
                        text = "Separate tags with commas. This replaces the tags on " +
                            "${selectedIds.size} selected notes.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bulkTagText,
                        onValueChange = { bulkTagText = it },
                        singleLine = true,
                        label = { Text("Tags") },
                        placeholder = { Text("idea, work, reading") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tags = bulkTagText.split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        viewModel.bulkTag(selectedIds.toList(), tags)
                        showBulkTagDialog = false
                        exitSelection()
                    },
                ) {
                    Text("Apply", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkTagDialog = false }) {
                    Text("Cancel", color = palette.subtext)
                }
            },
        )
    }

    if (showSort) {
        NyxBottomSheet(onDismissRequest = { showSort = false }) {
            SheetHeader(
                title = "Sort notes",
                subtitle = "Choose how your notes are ordered",
            )
            SortOption.entries.forEach { option ->
                val selected = state.settings.sortOption == option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setSortOption(option)
                            showSort = false
                        }
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option.label,
                        style = NyxBody,
                        color = if (selected) accent else palette.text,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun NoteActionsSheet(
    note: Note,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit,
    onColorSelected: (NoteColorTag) -> Unit,
    onMoodSelected: (WritingMood) -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    NyxBottomSheet(onDismissRequest = onDismiss) {
        SheetHeader(
            title = note.displayTitle,
            subtitle = if (note.isLocked) {
                "Locked note — unlock to read"
            } else {
                note.preview.take(90).ifBlank { "No additional text" }
            },
        )

        Text(
            text = "COLOR",
            style = NyxLabel,
            color = palette.subtext,
            modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            NoteColorTag.entries.forEach { tag ->
                val selected = note.colorTag == tag
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(tagColor(tag))
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) accent else palette.border,
                            shape = CircleShape,
                        )
                        .clickable { onColorSelected(tag) },
                )
            }
        }

        Text(
            text = "MOOD",
            style = NyxLabel,
            color = palette.subtext,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WritingMood.entries.forEach { mood ->
                val selected = note.moodTag == mood
                val color = moodColor(mood)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) color.copy(alpha = 0.16f) else palette.surfaceVariant,
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) color.copy(alpha = 0.65f) else palette.border,
                            shape = RoundedCornerShape(50),
                        )
                        .clickable { onMoodSelected(mood) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(color, CircleShape),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = mood.label,
                        style = NyxCaption,
                        color = if (selected) color else palette.subtext,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        ActionRow(
            icon = Icons.Outlined.PushPin,
            label = if (note.isPinned) "Unpin note" else "Pin note",
            onClick = onTogglePin,
        )
        ActionRow(
            icon = Icons.Outlined.Archive,
            label = "Move to archive",
            onClick = onArchive,
        )
        ActionRow(
            icon = Icons.Outlined.ContentCopy,
            label = "Duplicate note",
            onClick = onDuplicate,
        )
        ActionRow(
            icon = Icons.Outlined.ContentCopy,
            label = "Copy text",
            onClick = onCopy,
        )
        ActionRow(
            icon = Icons.Outlined.Share,
            label = "Share note",
            onClick = onShare,
        )
        ActionRow(
            icon = if (note.isLocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
            label = if (note.isLocked) "Remove note lock" else "Lock note",
            onClick = onToggleLock,
        )
        ActionRow(
            icon = Icons.Outlined.DeleteOutline,
            label = "Move to trash",
            tint = palette.subtext,
            onClick = onDelete,
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color? = null,
) {
    val palette = LocalNyxPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: palette.text,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(15.dp))
        Text(
            text = label,
            style = NyxBody,
            color = tint ?: palette.text,
        )
    }
}

@Composable
private fun SelectionTopBar(
    count: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onTag: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(palette.surface.copy(alpha = 0.72f))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Exit selection",
                tint = palette.text,
                modifier = Modifier.size(21.dp),
            )
        }
        Text(
            text = "$count selected",
            style = NyxBody,
            color = accent,
            modifier = Modifier.padding(start = 4.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSelectAll, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.SelectAll,
                contentDescription = if (allSelected) "Clear selection" else "Select all",
                tint = if (allSelected) accent else palette.subtext,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onPin, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.PushPin,
                contentDescription = "Pin selected",
                tint = palette.subtext,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onTag, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.Label,
                contentDescription = "Tag selected",
                tint = palette.subtext,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onArchive, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = "Archive selected",
                tint = palette.subtext,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete selected",
                tint = NyxError,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun HomeBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(shape)
            .background(palette.surface.copy(alpha = 0.94f))
            .border(1.dp, palette.glassBorder, shape)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        homeTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigate(tab.route) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) accent else palette.subtext,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = tab.label,
                    style = NyxCaption,
                    color = if (selected) accent else palette.subtext,
                    maxLines = 1,
                )
            }
        }
    }
}

private data class HomeTab(
    val label: String,
    val route: String,
    val icon: ImageVector,
)

private val homeTabs = listOf(
    HomeTab("Notes", Screen.Home.route, Icons.Outlined.Notes),
    HomeTab("Pinned", Screen.Pinned.route, Icons.Outlined.PushPin),
    HomeTab("Archive", Screen.Archive.route, Icons.Outlined.Archive),
    HomeTab("Settings", Screen.Settings.route, Icons.Outlined.Settings),
)

@Composable
private fun TagFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.16f) else palette.card)
            .border(
                width = 1.dp,
                color = if (selected) accent.copy(alpha = 0.55f) else palette.glassBorder,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = NyxCaption,
            color = if (selected) accent else palette.subtext,
            maxLines = 1,
        )
    }
}

@Composable
private fun TemplateRow(
    name: String,
    preview: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val palette = LocalNyxPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = NyxBody,
                color = palette.text,
            )
            if (preview.isNotBlank()) {
                Text(
                    text = preview.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty(),
                    style = NyxBodySmall,
                    color = palette.subtext,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete template",
                    tint = palette.subtext,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}
