package com.nyxpad.app.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.BuildConfig
import com.nyxpad.app.data.preferences.AccentChoice
import com.nyxpad.app.data.preferences.BackgroundStyle
import com.nyxpad.app.data.preferences.NyxFontStyle
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.data.preferences.NyxTheme
import com.nyxpad.app.ui.components.NyxScreenHeader
import com.nyxpad.app.ui.theme.AmoledBackground
import com.nyxpad.app.ui.theme.AmoledCard
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.MidnightBackground
import com.nyxpad.app.ui.theme.MidnightCard
import com.nyxpad.app.ui.theme.NyxBackground
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxCard
import com.nyxpad.app.ui.theme.NyxError
import com.nyxpad.app.ui.theme.NyxHeadline
import com.nyxpad.app.ui.theme.NyxLabel
import com.nyxpad.app.ui.theme.accentFor
import com.nyxpad.app.utils.BackgroundManager
import com.nyxpad.app.utils.BiometricUtils
import com.nyxpad.app.utils.ExportUtils
import com.nyxpad.app.utils.PreferencesKeys
import com.nyxpad.app.utils.findFragmentActivity
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onAppUnlocked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = remember(context) { context.findFragmentActivity() }

    var showPurgeDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var exporting by remember { mutableStateOf(false) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                viewModel.exportBackup(uri)
                    .onSuccess { count ->
                        Toast.makeText(
                            context,
                            "Backup saved with $count notes",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Could not save the backup",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                viewModel.importTextFiles(uris)
                    .onSuccess { count ->
                        Toast.makeText(
                            context,
                            "Imported $count notes",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Could not import those files",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
            }
        }
    }

    val exportMarkdownLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown"),
    ) { uri ->
        exporting = false
        if (uri != null) {
            scope.launch {
                viewModel.exportMarkdown(uri)
                    .onSuccess { count ->
                        Toast.makeText(
                            context,
                            "Exported $count notes",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Could not export notes",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
            }
        }
    }

    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = BackgroundManager.copyToAppStorage(context, uri)
                if (path == null) {
                    Toast.makeText(context, "Could not read that image", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.setCustomBackgroundPath(path)
                    viewModel.setBackgroundStyle(BackgroundStyle.Custom)
                    Toast.makeText(context, "Background updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        val host = activity
        if (host == null) {
            Toast.makeText(context, "Unable to show a security prompt.", Toast.LENGTH_SHORT).show()
            return
        }
        BiometricUtils.authenticate(
            activity = host,
            title = if (enabled) "Enable app lock" else "Disable app lock",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = {
                viewModel.setAppLock(enabled)
                if (enabled) {
                    onAppUnlocked()
                }
                Toast.makeText(
                    context,
                    if (enabled) "App lock enabled" else "App lock disabled",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
    }

    fun exportAll() {
        if (exporting) return
        exporting = true
        scope.launch {
            val notes = viewModel.allNotesForExport()
            exporting = false
            if (notes.isEmpty()) {
                Toast.makeText(context, "There are no notes to export yet.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            ExportUtils.shareAsFile(
                context = context,
                fileName = "nyxpad-notes-${LocalDate.now()}.md",
                mimeType = "text/markdown",
                content = ExportUtils.allNotesAsText(notes),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NyxScreenHeader(
                title = "Settings",
                subtitle = "Make NyxPad yours",
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    PrivacyIntroCard()
                }

                item {
                    SettingsGroup(title = "Appearance") {
                        Text(
                            text = "Theme",
                            style = NyxLabel,
                            color = palette.subtext,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 9.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NyxTheme.entries.forEach { theme ->
                                ThemePreviewCard(
                                    theme = theme,
                                    selected = settings.theme == theme,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.setTheme(theme) },
                                )
                            }
                        }
                        Text(
                            text = "Accent",
                            style = NyxLabel,
                            color = palette.subtext,
                            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 9.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                        ) {
                            AccentChoice.entries.forEach { choice ->
                                val selected = settings.accent == choice
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(accentFor(choice))
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) palette.text else palette.border,
                                            shape = CircleShape,
                                        )
                                        .clickable { viewModel.setAccent(choice) },
                                )
                            }
                        }
                        Text(
                            text = "Background",
                            style = NyxLabel,
                            color = palette.subtext,
                            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 9.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BackgroundStyle.entries.forEach { style ->
                                ChoiceChip(
                                    label = style.label,
                                    selected = settings.backgroundStyle == style,
                                    onClick = {
                                        viewModel.setBackgroundStyle(style)
                                        if (style == BackgroundStyle.Custom) {
                                            backgroundPicker.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                                ),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                        if (settings.backgroundStyle == BackgroundStyle.Custom) {
                            SettingsNavRow(
                                icon = Icons.Outlined.Palette,
                                title = "Choose background photo",
                                subtitle = "Pick an image from your gallery",
                                value = null,
                                onClick = {
                                    backgroundPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            )
                        }
                        Text(
                            text = "Editor font",
                            style = NyxLabel,
                            color = palette.subtext,
                            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 9.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NyxFontStyle.entries.forEach { style ->
                                ChoiceChip(
                                    label = style.label,
                                    selected = settings.fontStyle == style,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.setFontStyle(style) },
                                )
                            }
                        }
                        FontSizeControl(
                            settings = settings,
                            onCommit = viewModel::setFontSize,
                        )
                        LineSpacingControl(
                            settings = settings,
                            onCommit = viewModel::setLineSpacing,
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item {
                    SettingsGroup(title = "Writing") {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Save,
                            title = "Auto-save",
                            subtitle = "Save quietly while you write",
                            checked = settings.autoSave,
                            onCheckedChange = viewModel::setAutoSave,
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Notes,
                            title = "Show word count",
                            subtitle = "Keep the tally visible in the editor",
                            checked = settings.showWordCount,
                            onCheckedChange = viewModel::setShowWordCount,
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Fullscreen,
                            title = "Focus mode by default",
                            subtitle = "Open new notes without distractions",
                            checked = settings.focusModeDefault,
                            onCheckedChange = viewModel::setFocusModeDefault,
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Lock,
                            title = "Note locks",
                            subtitle = "Allow individual notes to be locked",
                            checked = settings.noteLock,
                            onCheckedChange = viewModel::setNoteLock,
                        )
                    }
                }

                item {
                    SettingsGroup(title = "Privacy & security") {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Fingerprint,
                            title = "App lock",
                            subtitle = "Require biometrics or device PIN to open NyxPad",
                            checked = settings.appLock,
                            onCheckedChange = ::toggleAppLock,
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Shield,
                            title = "Nearby sharing (NyxDrop)",
                            subtitle = "Share a note to a browser on your Wi-Fi; off by default",
                            checked = settings.onlineFeatures,
                            onCheckedChange = viewModel::setOnlineFeatures,
                        )
                        SettingsDivider()
                        SettingsNavRow(
                            icon = Icons.Outlined.DeleteOutline,
                            title = "Auto-purge trash",
                            subtitle = "Deleted notes are removed automatically",
                            value = settings.autoPurgeLabel,
                            onClick = { showPurgeDialog = true },
                        )
                    }
                }

                item {
                    SettingsGroup(title = "Backup & restore") {
                        SettingsNavRow(
                            icon = Icons.Outlined.Save,
                            title = "Export backup",
                            subtitle = "Notes, tags, templates and settings in one file",
                            value = null,
                            onClick = {
                                exportBackupLauncher.launch(
                                    "nyxpad-backup-${LocalDate.now()}.json",
                                )
                            },
                        )
                        SettingsDivider()
                        SettingsNavRow(
                            icon = Icons.Outlined.Refresh,
                            title = "Restore backup",
                            subtitle = "Merge with your notes or replace everything",
                            value = null,
                            onClick = { restoreBackupLauncher.launch(arrayOf("*/*")) },
                        )
                        SettingsDivider()
                        SettingsNavRow(
                            icon = Icons.Outlined.Notes,
                            title = "Import Markdown or text",
                            subtitle = "Turn .md and .txt files into notes",
                            value = null,
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                        )
                    }
                }

                item {
                    SettingsGroup(title = "Your data") {
                        SettingsNavRow(
                            icon = Icons.Outlined.FileDownload,
                            title = "Export all notes",
                            subtitle = if (exporting) {
                                "Preparing your Markdown export…"
                            } else {
                                "Save every note as one Markdown file"
                            },
                            value = null,
                            onClick = {
                                exporting = true
                                exportMarkdownLauncher.launch(
                                    "nyxpad-notes-${LocalDate.now()}.md",
                                )
                            },
                        )
                        SettingsDivider()
                        SettingsNavRow(
                            icon = Icons.Outlined.DeleteForever,
                            title = "Delete all notes",
                            subtitle = "This cannot be undone",
                            value = null,
                            danger = true,
                            onClick = { showDeleteDialog = true },
                        )
                    }
                }

                item {
                    SettingsGroup(title = "NyxPad") {
                        SettingsNavRow(
                            icon = Icons.Outlined.Info,
                            title = "About NyxPad",
                            subtitle = "Version, licenses and the privacy promise",
                            value = "v${BuildConfig.VERSION_NAME}",
                            onClick = onAbout,
                        )
                        SettingsDivider()
                        SettingsNavRow(
                            icon = Icons.Outlined.Refresh,
                            title = "Show onboarding again",
                            subtitle = "Revisit the three opening screens",
                            value = null,
                            onClick = {
                                viewModel.replayOnboarding(onReplayOnboarding)
                            },
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = palette.subtext.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = "No accounts. No tracking. No cloud by default.",
                            style = NyxCaption,
                            color = palette.subtext.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }

    if (pendingRestoreUri != null) {
        val restoreUri = pendingRestoreUri
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore backup", color = palette.text) },
            text = {
                Text(
                    text = "Merge adds the backup's notes to your current library. " +
                        "Replace removes every current note and template first. " +
                        "Settings from the backup are applied either way.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                )
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            pendingRestoreUri = null
                            if (restoreUri != null) {
                                scope.launch {
                                    viewModel.restoreBackup(restoreUri, replace = false)
                                        .onSuccess { count ->
                                            Toast.makeText(
                                                context,
                                                "Merged $count notes",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                        .onFailure { error ->
                                            Toast.makeText(
                                                context,
                                                error.message ?: "Could not restore that backup",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                }
                            }
                        },
                    ) {
                        Text("Merge", color = accent)
                    }
                    TextButton(
                        onClick = {
                            pendingRestoreUri = null
                            if (restoreUri != null) {
                                scope.launch {
                                    viewModel.restoreBackup(restoreUri, replace = true)
                                        .onSuccess { count ->
                                            Toast.makeText(
                                                context,
                                                "Restored $count notes",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                        .onFailure { error ->
                                            Toast.makeText(
                                                context,
                                                error.message ?: "Could not restore that backup",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                }
                            }
                        },
                    ) {
                        Text("Replace", color = NyxError)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text("Cancel", color = palette.subtext)
                }
            },
        )
    }

    if (showPurgeDialog) {
        val options = listOf(
            7 to "After 7 days",
            14 to "After 14 days",
            30 to "After 30 days",
            60 to "After 60 days",
            90 to "After 90 days",
            PreferencesKeys.NEVER_PURGE to "Never",
        )
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("Auto-purge trash", color = palette.text) },
            text = {
                Column {
                    Text(
                        text = "Choose how long deleted notes stay recoverable.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(10.dp))
                    options.forEach { (days, label) ->
                        val selected = settings.autoPurgeDays == days
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAutoPurgeDays(days)
                                    showPurgeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = NyxBody,
                                color = if (selected) accent else palette.text,
                                modifier = Modifier.weight(1f),
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(accent, CircleShape),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("Close", color = accent)
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all notes?", color = palette.text) },
            text = {
                Text(
                    text = "Every note, including archived and trashed notes, will be permanently erased. " +
                        "Templates and settings stay untouched. This cannot be undone.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            viewModel.clearAllNotes()
                            Toast.makeText(context, "All notes deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("Delete everything", color = NyxError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Keep my notes", color = palette.subtext)
                }
            },
        )
    }
}

@Composable
private fun PrivacyIntroCard() {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Private by design",
                style = NyxHeadline,
                color = palette.text,
            )
            Text(
                text = "Your words stay on this device. NyxDrop sharing is local, optional and off by default.",
                style = NyxBodySmall,
                color = palette.subtext,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalNyxPalette.current
    Column {
        Text(
            text = title.uppercase(Locale.getDefault()),
            style = NyxLabel,
            color = palette.subtext,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(palette.card)
                .border(1.dp, palette.border, RoundedCornerShape(18.dp)),
            content = content,
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = NyxBody, color = palette.text)
            Text(
                text = subtitle,
                style = NyxCaption,
                color = palette.subtext,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NyxBackground,
                checkedTrackColor = accent,
            ),
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String?,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    val palette = LocalNyxPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon, danger = danger)
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = NyxBody,
                color = if (danger) NyxError else palette.text,
            )
            Text(
                text = subtitle,
                style = NyxCaption,
                color = palette.subtext,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (value != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                style = NyxCaption,
                color = palette.subtext,
            )
        }
    }
}

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    danger: Boolean = false,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (danger) {
                    NyxError.copy(alpha = 0.10f)
                } else {
                    accent.copy(alpha = 0.10f)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (danger) NyxError else accent,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun SettingsDivider() {
    val palette = LocalNyxPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 61.dp)
            .height(1.dp)
            .background(palette.divider),
    )
}

@Composable
private fun ThemePreviewCard(
    theme: NyxTheme,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val background = when (theme) {
        NyxTheme.Lumina -> NyxBackground
        NyxTheme.Amoled -> AmoledBackground
        NyxTheme.Midnight -> MidnightBackground
    }
    val card = when (theme) {
        NyxTheme.Lumina -> NyxCard
        NyxTheme.Amoled -> AmoledCard
        NyxTheme.Midnight -> MidnightCard
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceVariant)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accent else palette.border,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(background),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(accent, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(card, RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(3.dp)
                        .background(palette.subtext.copy(alpha = 0.35f), RoundedCornerShape(2.dp)),
                )
            }
        }
        Text(
            text = theme.label,
            style = NyxCaption,
            color = if (selected) accent else palette.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 7.dp, start = 2.dp),
        )
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent.copy(alpha = 0.14f) else palette.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (selected) accent.copy(alpha = 0.65f) else palette.border,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = NyxCaption,
            color = if (selected) accent else palette.subtext,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FontSizeControl(
    settings: NyxSettings,
    onCommit: (Int) -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    var value by remember(settings.fontSize) {
        mutableStateOf(settings.fontSize.toFloat())
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.TextFields,
                contentDescription = null,
                tint = palette.subtext,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "Editor size",
                style = NyxBodySmall,
                color = palette.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${value.roundToInt()} pt",
                style = NyxCaption,
                color = palette.subtext,
            )
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            onValueChangeFinished = { onCommit(value.roundToInt()) },
            valueRange = PreferencesKeys.MIN_FONT_SIZE.toFloat()..
                PreferencesKeys.MAX_FONT_SIZE.toFloat(),
            steps = PreferencesKeys.MAX_FONT_SIZE - PreferencesKeys.MIN_FONT_SIZE - 1,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = palette.border,
            ),
        )
    }
}

@Composable
private fun LineSpacingControl(
    settings: NyxSettings,
    onCommit: (Float) -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    var value by remember(settings.lineSpacing) {
        mutableStateOf(settings.lineSpacing)
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = palette.subtext,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "Line spacing",
                style = NyxBodySmall,
                color = palette.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = String.format(Locale.getDefault(), "%.1f×", value),
                style = NyxCaption,
                color = palette.subtext,
            )
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            onValueChangeFinished = { onCommit(value) },
            valueRange = PreferencesKeys.MIN_LINE_SPACING..PreferencesKeys.MAX_LINE_SPACING,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = palette.border,
            ),
        )
    }
}
