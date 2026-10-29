package com.nyxpad.app.ui.screens.editor

import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.nyxpad.app.ui.theme.NyxWhite
import com.nyxpad.app.ui.theme.moodColor
import com.nyxpad.app.ui.theme.tagColor
import com.nyxpad.app.utils.BiometricUtils
import com.nyxpad.app.utils.ClipboardUtils
import com.nyxpad.app.utils.DateUtils
import com.nyxpad.app.utils.ExportUtils
import com.nyxpad.app.utils.NyxDropServer
import com.nyxpad.app.utils.NyxDropSession
import com.nyxpad.app.utils.QrCodeUtils
import com.nyxpad.app.utils.findFragmentActivity
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.tasklist.TaskListPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = remember(context) { context.findFragmentActivity() }

    var menuOpen by remember { mutableStateOf(false) }
    var showColors by remember { mutableStateOf(false) }
    var showMoods by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }
    var editorValue by remember { mutableStateOf(TextFieldValue(state.content)) }
    var previewMode by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDropDialog by remember { mutableStateOf(false) }
    var dropSession by remember { mutableStateOf<NyxDropSession?>(null) }
    var dropQr by remember { mutableStateOf<Bitmap?>(null) }
    var dropMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(dropSession) {
        onDispose { dropSession?.stop() }
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            editorValue = TextFieldValue(
                text = state.content,
                selection = TextRange(state.content.length),
            )
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
        if (spoken.isNotBlank()) {
            val current = editorValue
            val start = current.selection.start.coerceIn(0, current.text.length)
            val end = current.selection.end.coerceIn(0, current.text.length)
            val newText = current.text.replaceRange(start, end, spoken)
            val cursor = start + spoken.length
            editorValue = TextFieldValue(newText, TextRange(cursor))
            viewModel.onContentChange(newText)
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note")
        }
        runCatching { voiceLauncher.launch(intent) }
            .onFailure {
                Toast.makeText(
                    context,
                    "No speech recognizer is available on this device.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    fun updateContent(value: TextFieldValue) {
        editorValue = value
        viewModel.onContentChange(value.text)
    }

    fun applyFormat(prefix: String, suffix: String = prefix, linePrefix: Boolean = false) {
        val value = editorValue
        val start = value.selection.start.coerceIn(0, value.text.length)
        val end = value.selection.end.coerceIn(0, value.text.length)
        val updated = if (linePrefix) {
            val lineStart = value.text.lastIndexOf('\n', (start - 1).coerceAtLeast(0))
                .let { if (it == -1) 0 else it + 1 }
            val newText = value.text.substring(0, lineStart) +
                prefix +
                value.text.substring(lineStart)
            TextFieldValue(newText, TextRange((start + prefix.length).coerceAtMost(newText.length)))
        } else {
            val selected = value.text.substring(start, end)
            val replacement = prefix + selected + suffix
            val newText = value.text.replaceRange(start, end, replacement)
            val cursor = if (selected.isEmpty()) {
                start + prefix.length
            } else {
                start + replacement.length
            }
            TextFieldValue(newText, TextRange(cursor.coerceAtMost(newText.length)))
        }
        editorValue = updated
        viewModel.onContentChange(updated.text)
    }

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
            tags = state.tags,
        )
        ExportUtils.shareText(context, note.displayTitle, ExportUtils.plainText(note))
    }

    LaunchedEffect(showQrDialog) {
        if (showQrDialog) {
            qrBitmap = withContext(Dispatchers.Default) {
                QrCodeUtils.generate(
                    QrCodeUtils.payload(state.title, state.content),
                )
            }
        }
    }

    LaunchedEffect(showDropDialog, dropSession) {
        val session = dropSession
        if (showDropDialog && session != null) {
            dropQr = withContext(Dispatchers.Default) {
                QrCodeUtils.generate(session.url, size = 640)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
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
                    editorValue = editorValue,
                    previewMode = false,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = ::updateContent,
                    onTagClick = {},
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
                    previewMode = previewMode,
                    onTogglePreview = { previewMode = !previewMode },
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
                            editorValue = editorValue,
                            previewMode = previewMode,
                            onTitleChange = viewModel::onTitleChange,
                            onContentChange = ::updateContent,
                            onTagClick = {
                                tagText = state.tags.joinToString(", ")
                                showTagsDialog = true
                            },
                        )
                    }
                    if (!previewMode) {
                        MarkdownToolbar(
                            onFormat = ::applyFormat,
                            onVoice = ::startVoiceInput,
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
                        text = { Text("Tags") },
                        onClick = {
                            menuOpen = false
                            tagText = state.tags.joinToString(", ")
                            showTagsDialog = true
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
                        text = { Text("Share as QR code") },
                        onClick = {
                            menuOpen = false
                            qrBitmap = null
                            showQrDialog = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("NyxDrop to nearby device") },
                        onClick = {
                            menuOpen = false
                            viewModel.saveNow()
                            dropQr = null
                            if (!state.settings.onlineFeatures) {
                                dropMessage = "Nearby sharing is off. Turn it on in " +
                                    "Settings → Nearby sharing, then try again."
                                dropSession?.stop()
                                dropSession = null
                                showDropDialog = true
                            } else {
                                val session = NyxDropServer.start(
                                    state.title,
                                    state.content,
                                )
                                if (session == null) {
                                    dropMessage = "Connect this device to Wi-Fi first. " +
                                        "NyxDrop needs a local network."
                                    dropSession?.stop()
                                    dropSession = null
                                    showDropDialog = true
                                } else {
                                    dropMessage = null
                                    dropSession?.stop()
                                    dropSession = session
                                    showDropDialog = true
                                }
                            }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Export as Markdown") },
                        onClick = {
                            menuOpen = false
                            viewModel.saveNow()
                            val note = Note(
                                id = state.noteId,
                                title = state.title,
                                content = state.content,
                                createdAt = state.createdAt,
                                updatedAt = state.updatedAt,
                                wordCount = state.wordCount,
                                charCount = state.charCount,
                                tags = state.tags,
                            )
                            scope.launch {
                                ExportUtils.shareAsFile(
                                    context = context,
                                    fileName = "${note.displayTitle}.md",
                                    mimeType = "text/markdown",
                                    content = ExportUtils.markdown(note),
                                )
                            }
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

    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Tags", color = palette.text) },
            text = {
                Column {
                    Text(
                        text = "Separate tags with commas. They appear as filters on the home screen.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tagText,
                        onValueChange = { tagText = it },
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
                        viewModel.setTags(tagText.split(','))
                        showTagsDialog = false
                    },
                ) {
                    Text("Save", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Cancel", color = palette.subtext)
                }
            },
        )
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Share as QR code", color = palette.text) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bitmap = qrBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR code for this note",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(250.dp)
                                .background(NyxWhite, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Scan this with NyxPad on another device. Anyone who " +
                                "scans it can read the note.",
                            style = NyxBodySmall,
                            color = palette.subtext,
                        )
                    } else {
                        Text(
                            text = "This note is too long for a QR code. Use Share note, " +
                                "Export as Markdown, or NyxDrop instead.",
                            style = NyxBodySmall,
                            color = palette.subtext,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close", color = accent)
                }
            },
        )
    }

    if (showDropDialog) {
        val session = dropSession
        AlertDialog(
            onDismissRequest = {
                showDropDialog = false
                dropSession?.stop()
                dropSession = null
                dropQr = null
            },
            title = { Text("NyxDrop", color = palette.text) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (session == null) {
                        Text(
                            text = dropMessage ?: "NyxDrop could not start.",
                            style = NyxBodySmall,
                            color = palette.subtext,
                        )
                    } else {
                        Text(
                            text = "Open this address in any browser on the same Wi-Fi. " +
                                "It works on Windows, macOS, Linux, Android and iOS — " +
                                "no app or account needed.",
                            style = NyxBodySmall,
                            color = palette.subtext,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = session.url,
                            style = NyxBody,
                            color = accent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextButton(
                            onClick = {
                                ClipboardUtils.copyToClipboard(
                                    context,
                                    "NyxDrop link",
                                    session.url,
                                )
                                Toast.makeText(
                                    context,
                                    "Link copied",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        ) {
                            Text("Copy link", color = accent)
                        }
                        val bitmap = dropQr
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR code for the NyxDrop link",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(190.dp)
                                    .background(NyxWhite, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Sharing stops when you close this dialog or after " +
                                "10 minutes.",
                            style = NyxCaption,
                            color = palette.subtext,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDropDialog = false
                        dropSession?.stop()
                        dropSession = null
                        dropQr = null
                    },
                ) {
                    Text(if (session == null) "Close" else "Stop sharing", color = accent)
                }
            },
        )
    }
}

@Composable
private fun EditorTopBar(
    state: EditorUiState,
    previewMode: Boolean,
    onTogglePreview: () -> Unit,
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
            .background(palette.surface.copy(alpha = 0.72f))
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
        IconButton(onClick = onTogglePreview, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = if (previewMode) {
                    Icons.Outlined.Edit
                } else {
                    Icons.Outlined.Visibility
                },
                contentDescription = if (previewMode) "Edit note" else "Preview Markdown",
                tint = if (previewMode) accent else palette.subtext,
                modifier = Modifier.size(19.dp),
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
    editorValue: TextFieldValue,
    previewMode: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onTagClick: () -> Unit,
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
        if (!focusMode) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = accent.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "#$tag",
                            style = NyxCaption,
                            color = accent,
                        )
                    }
                }
                Text(
                    text = if (state.tags.isEmpty()) "+ Add tags" else "+ Tag",
                    style = NyxCaption,
                    color = palette.subtext,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onTagClick)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                )
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
        if (previewMode) {
            MarkdownPreview(
                content = state.content,
                fontSize = state.settings.fontSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (focusMode) 420.dp else 340.dp),
            )
        } else {
            BasicTextField(
                value = editorValue,
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
                        if (editorValue.text.isEmpty()) {
                            Text(
                                text = "Start writing. No one else can see this.",
                                style = contentStyle.copy(color = palette.placeholder),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
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

private data class MarkdownAction(
    val label: String,
    val prefix: String,
    val suffix: String,
    val linePrefix: Boolean = false,
)

private val markdownActions = listOf(
    MarkdownAction("B", "**", "**"),
    MarkdownAction("I", "*", "*"),
    MarkdownAction("H", "# ", "", linePrefix = true),
    MarkdownAction("☑", "- [ ] ", "", linePrefix = true),
    MarkdownAction("•", "- ", "", linePrefix = true),
    MarkdownAction("❝", "> ", "", linePrefix = true),
    MarkdownAction("</>", "`", "`"),
    MarkdownAction("Link", "[", "](https://)"),
)

@Composable
private fun MarkdownToolbar(
    onFormat: (String, String, Boolean) -> Unit,
    onVoice: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface.copy(alpha = 0.72f))
            .drawBehind {
                drawLine(
                    color = palette.divider,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            markdownActions.forEach { action ->
                val shape = RoundedCornerShape(9.dp)
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(palette.card)
                        .border(1.dp, palette.glassBorder, shape)
                        .clickable {
                            onFormat(action.prefix, action.suffix, action.linePrefix)
                        }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = action.label,
                        style = NyxLabel,
                        color = palette.text,
                        maxLines = 1,
                    )
                }
            }
        }
        IconButton(
            onClick = onVoice,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = "Voice input",
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MarkdownPreview(
    content: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val markwon = remember(context, accent, palette) {
        Markwon.builder(context)
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .linkColor(accent.toArgb())
                            .blockQuoteColor(accent.toArgb())
                            .codeTextColor(palette.text.toArgb())
                            .codeBackgroundColor(palette.card.toArgb())
                            .codeBlockTextColor(palette.text.toArgb())
                            .codeBlockBackgroundColor(palette.card.toArgb())
                            .thematicBreakColor(palette.divider.toArgb())
                    }
                },
            )
            .build()
    }
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setTextIsSelectable(true)
                movementMethod = LinkMovementMethod.getInstance()
                setPadding(0, 0, 0, 0)
                setLineSpacing(0f, 1.3f)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { textView ->
            textView.setTextColor(palette.text.toArgb())
            textView.textSize = fontSize.toFloat()
            val renderKey = content to markwon
            if (textView.tag != renderKey) {
                textView.tag = renderKey
                markwon.setMarkdown(
                    textView,
                    content.ifBlank { "*Nothing to preview yet. Start writing.*" },
                )
            }
        },
        modifier = modifier,
    )
}
