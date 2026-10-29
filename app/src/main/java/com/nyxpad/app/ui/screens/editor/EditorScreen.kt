package com.nyxpad.app.ui.screens.editor

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.ui.components.NyxBottomSheet
import com.nyxpad.app.ui.components.SheetHeader
import com.nyxpad.app.ui.components.WritingMoodBadge
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxEditorFontFamily
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxDisplay
import com.nyxpad.app.ui.theme.NyxLabel
import com.nyxpad.app.ui.theme.moodColor
import com.nyxpad.app.ui.theme.tagColor
import com.nyxpad.app.utils.BiometricUtils
import com.nyxpad.app.utils.DateUtils
import com.nyxpad.app.utils.ExportUtils
import com.nyxpad.app.utils.findFragmentActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }

    var menuOpen by remember { mutableStateOf(false) }
    var showColors by remember { mutableStateOf(false) }
    var showMoods by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }

    BackHandler {
        viewModel.saveAndExit(onBack)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.saveNow()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleLock() {
        val host = activity
        if (host == null) {
            Toast.makeText(context, "Unable to show a security prompt.", Toast.LENGTH_SHORT).show()
            return
        }
        val enabling = !state.isLocked
        BiometricUtils.authenticate(
            activity = host,
            title = if (enabling) "Lock note" else "Unlock note",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = {
                viewModel.setLocked(enabling)
                Toast.makeText(
                    context,
                    if (enabling) "Note locked" else "Note lock removed",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
    }

    fun shareNote() {
        val note = Note(
            id = state.noteId,
            title = state.title,
            content = state.content,
            createdAt = state.createdAt,
            updatedAt = state.updatedAt,
            isPinned = state.isPinned,
            isArchived = state.isArchived,
            isLocked = state.isLocked,
            colorTag = state.colorTag,
            moodTag = state.moodTag,
            wordCount = state.wordCount,
            charCount = state.charCount,
        )
        ExportUtils.shareText(context, note.displayTitle, ExportUtils.plainText(note))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        if (state.focusMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
            ) {
                EditorBody(
                    state = state,
                    focusMode = true,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                )
            }
            IconButton(
                onClick = viewModel::toggleFocusMode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 6.dp, end = 8.dp)
                    .size(42.dp)
                    .background(palette.surface.copy(alpha = 0.92f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Exit focus mode",
                    tint = palette.subtext,
                    modifier = Modifier.size(19.dp),
                )
            }
            Text(
                text = "${state.wordCount} words",
                style = NyxCaption,
                color = palette.subtext.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
            ) {
                EditorTopBar(
                    state = state,
                    onBack = { viewModel.saveAndExit(onBack) },
                    onFocus = viewModel::toggleFocusMode,
                    onSave = viewModel::saveNow,
                    onMenuClick = { menuOpen = true },
                )

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = accent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        EditorBody(
                            state = state,
                            focusMode = false,
                            onTitleChange = viewModel::onTitleChange,
                            onContentChange = viewModel::onContentChange,
                        )
                    }
                    EditorBottomBar(state = state)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 6.dp),
            ) {
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(if (state.isPinned) "Unpin note" else "Pin note") },
                        onClick = {
                            menuOpen = false
                            viewModel.togglePin()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Archive note") },
                        onClick = {
                            menuOpen = false
                            viewModel.archiveAndExit(onBack)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Note color") },
                        onClick = {
                            menuOpen = false
                            showColors = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Writing mood") },
                        onClick = {
                            menuOpen = false
                            showMoods = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (state.isLocked) "Remove note lock" else "Lock note") },
                        onClick = {
                            menuOpen = false
                            toggleLock()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Save as template") },
                        onClick = {
                            menuOpen = false
                            templateName = state.title.take(40)
                            showTemplateDialog = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Share note") },
                        onClick = {
                            menuOpen = false
                            viewModel.saveNow()
                            shareNote()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Move to trash") },
                        onClick = {
                            menuOpen = false
                            showDeleteDialog = true
                        },
                    )
                }
            }
        }
    }

    if (showColors) {
        NyxBottomSheet(onDismissRequest = { showColors = false }) {
            SheetHeader(
                title = "Note color",
                subtitle = "A quiet signal in your grid and list",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                NoteColorTag.entries.forEach { tag ->
                    val selected = state.colorTag == tag
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tagColor(tag))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) accent else palette.border,
                                shape = CircleShape,
                            )
                            .clickable {
                                viewModel.setColor(tag)
                                showColors = false
                            },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showMoods) {
        NyxBottomSheet(onDismissRequest = { showMoods = false }) {
            SheetHeader(
                title = "Writing mood",
                subtitle = "Name the energy behind this note",
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WritingMood.entries.forEach { mood ->
                    val selected = state.moodTag == mood
                    val color = moodColor(mood)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) color.copy(alpha = 0.13f) else palette.surfaceVariant,
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) color.copy(alpha = 0.6f) else palette.border,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                viewModel.setMood(mood)
                                showMoods = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = mood.label,
                            style = NyxBody,
                            color = if (selected) color else palette.text,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Save as template", color = palette.text) },
            text = {
                Column {
                    Text(
                        text = "Reuse this note's title and body whenever you start something new.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template name") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveAsTemplate(templateName)
                        showTemplateDialog = false
                    },
                ) {
                    Text("Save", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Cancel", color = palette.subtext)
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move to trash?", color = palette.text) },
            text = {
                Text(
                    text = "The note will stay in Trash until it is permanently deleted or auto-purged.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAndExit(onBack)
                    },
                ) {
                    Text("Move to trash", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Keep note", color = palette.subtext)
                }
            },
        )
    }
}

@Composable
private fun EditorTopBar(
    state: EditorUiState,
    onBack: () -> Unit,
    onFocus: () -> Unit,
    onSave: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(58.dp)
            .background(palette.background)
            .drawBehind {
                drawLine(
                    color = palette.divider,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Save and go back",
                tint = palette.text,
                modifier = Modifier.size(21.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (state.noteId == 0L) "New note" else "Edit note",
                style = NyxLabel.copy(color = palette.subtext),
            )
            Text(
                text = when {
                    state.isSaving -> "Saving…"
                    state.lastSavedAt != null -> "Saved ${DateUtils.formatTime(state.lastSavedAt)}"
                    state.settings.autoSave -> "Auto-save on"
                    else -> "Auto-save off"
                },
                style = NyxCaption,
                color = palette.subtext.copy(alpha = 0.75f),
            )
        }
        IconButton(onClick = onFocus, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = Icons.Outlined.Fullscreen,
                contentDescription = "Focus mode",
                tint = palette.subtext,
                modifier = Modifier.size(19.dp),
            )
        }
        IconButton(onClick = onSave, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Save note",
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onMenuClick, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More note actions",
                tint = palette.subtext,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun EditorBody(
    state: EditorUiState,
    focusMode: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val editorFont = LocalNyxEditorFontFamily.current
    val contentStyle = TextStyle(
        fontFamily = editorFont,
        fontWeight = FontWeight.Normal,
        fontSize = state.settings.fontSize.sp,
        lineHeight = (state.settings.fontSize * state.settings.lineSpacing).sp,
        color = palette.text,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = if (focusMode) 28.dp else 20.dp,
                vertical = if (focusMode) 30.dp else 14.dp,
            ),
    ) {
        BasicTextField(
            value = state.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = NyxDisplay.copy(
                color = palette.text,
                fontSize = if (focusMode) 30.sp else 27.sp,
                lineHeight = if (focusMode) 37.sp else 34.sp,
            ),
            cursorBrush = SolidColor(accent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (state.title.isEmpty()) {
                        Text(
                            text = "Untitled note",
                            style = NyxDisplay.copy(
                                color = palette.placeholder,
                                fontSize = if (focusMode) 30.sp else 27.sp,
                                lineHeight = if (focusMode) 37.sp else 34.sp,
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = DateUtils.formatEditorDate(state.createdAt),
                style = NyxCaption,
                color = palette.subtext,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (state.moodTag != WritingMood.None) {
                WritingMoodBadge(mood = state.moodTag)
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.divider),
        )
        Spacer(Modifier.height(18.dp))
        BasicTextField(
            value = state.content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (focusMode) 420.dp else 340.dp),
            textStyle = contentStyle,
            cursorBrush = SolidColor(accent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (state.content.isEmpty()) {
                        Text(
                            text = "Start writing. No one else can see this.",
                            style = contentStyle.copy(color = palette.placeholder),
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun EditorBottomBar(state: EditorUiState) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface)
            .navigationBarsPadding()
            .height(48.dp)
            .drawBehind {
                drawLine(
                    color = palette.divider,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${state.wordCount} ${if (state.wordCount == 1) "word" else "words"}  ·  " +
                "${state.charCount} ${if (state.charCount == 1) "character" else "characters"}",
            style = NyxCaption,
            color = palette.subtext,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = when {
                state.isSaving -> "Saving…"
                state.settings.autoSave -> "Auto-save"
                else -> "Unsaved changes"
            },
            style = NyxCaption,
            color = if (state.isSaving) accent else palette.subtext,
        )
    }
}
