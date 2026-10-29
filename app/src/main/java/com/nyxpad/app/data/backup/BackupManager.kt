package com.nyxpad.app.data.backup

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nyxpad.app.data.preferences.AccentChoice
import com.nyxpad.app.data.preferences.BackgroundStyle
import com.nyxpad.app.data.preferences.NyxFontStyle
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.data.preferences.NyxTheme
import com.nyxpad.app.data.preferences.SortOption
import com.nyxpad.app.data.preferences.ViewMode
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.utils.WordCountUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads and writes NyxPad's own portable backup format. Everything is handled
 * through the Storage Access Framework, so no storage permission is needed and
 * the user always chooses where the file lives.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NoteRepository,
    private val preferences: NyxPreferences,
) {

    suspend fun exportTo(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val notes = repository.getAllNotesForBackup()
            val templates = repository.getTemplatesSnapshot()
            val settings = preferences.getSettingsSnapshot()
            val root = JSONObject().apply {
                put("app", "nyxpad")
                put("format", 1)
                put("exportedAt", System.currentTimeMillis())
                put("notes", notesToJson(notes))
                put("templates", templatesToJson(templates))
                put("settings", settingsToJson(settings))
            }
            openOutput(uri).use { stream ->
                stream.writer(Charsets.UTF_8).use { it.write(root.toString(2)) }
            }
            notes.size
        }
    }

    suspend fun restoreFrom(uri: Uri, replace: Boolean): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = openInput(uri).use { it.reader(Charsets.UTF_8).readText() }
                val root = JSONObject(raw)
                require(root.optString("app") == "nyxpad") { "Not a NyxPad backup" }
                val notesArray = root.optJSONArray("notes") ?: JSONArray()
                val notes = jsonToNotes(notesArray)
                if (replace) {
                    repository.clearAllNotes()
                    repository.clearAllTemplates()
                }
                repository.saveNotes(notes)
                val templates = jsonToTemplates(root.optJSONArray("templates"))
                templates.forEach { repository.saveTemplate(it) }
                root.optJSONObject("settings")?.let { settings ->
                    preferences.applySettings(jsonToSettings(settings))
                }
                notes.size
            }
        }

    suspend fun importTextFiles(uris: List<Uri>): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            var imported = 0
            uris.forEach { uri ->
                val name = displayName(uri)
                val text = openInput(uri).use { input ->
                    input.reader(Charsets.UTF_8).readText()
                }
                if (text.isNotBlank()) {
                    val now = System.currentTimeMillis()
                    val title = name
                        .substringBeforeLast('.')
                        .ifBlank { "Imported note" }
                    repository.saveNote(
                        Note(
                            title = title,
                            content = text,
                            createdAt = now,
                            updatedAt = now,
                            wordCount = WordCountUtils.countWords(text),
                            charCount = text.length,
                            tags = listOf("imported"),
                        ),
                    )
                    imported++
                }
            }
            imported
        }
    }

    suspend fun exportMarkdownTo(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val notes = repository.getAllNotesForBackup()
            val body = buildString {
                appendLine("# NyxPad export")
                appendLine()
                appendLine("Exported ${notes.size} notes.")
                appendLine()
                notes.forEach { note ->
                    appendLine("## ${note.displayTitle}")
                    if (note.tags.isNotEmpty()) {
                        appendLine()
                        appendLine(note.tags.joinToString(" ") { "#$it" })
                    }
                    appendLine()
                    appendLine(note.content)
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
            }
            openOutput(uri).use { stream ->
                stream.writer(Charsets.UTF_8).use { it.write(body) }
            }
            notes.size
        }
    }

    private fun notesToJson(notes: List<Note>): JSONArray = JSONArray().apply {
        notes.forEach { note ->
            put(
                JSONObject().apply {
                    put("title", note.title)
                    put("content", note.content)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                    put("isPinned", note.isPinned)
                    put("isArchived", note.isArchived)
                    put("isLocked", note.isLocked)
                    put("colorTag", note.colorTag.wireValue)
                    put("moodTag", note.moodTag.wireValue)
                    put("wordCount", note.wordCount)
                    put("charCount", note.charCount)
                    put("tags", JSONArray(note.tags))
                },
            )
        }
    }

    private fun templatesToJson(templates: List<NoteTemplate>): JSONArray =
        JSONArray().apply {
            templates.forEach { template ->
                put(
                    JSONObject().apply {
                        put("name", template.name)
                        put("titleTemplate", template.titleTemplate)
                        put("contentTemplate", template.contentTemplate)
                        put("createdAt", template.createdAt)
                    },
                )
            }
        }

    private fun settingsToJson(settings: NyxSettings): JSONObject = JSONObject().apply {
        put("theme", settings.theme.storageKey)
        put("accent", settings.accent.storageKey)
        put("fontSize", settings.fontSize)
        put("fontStyle", settings.fontStyle.storageKey)
        put("autoSave", settings.autoSave)
        put("lineSpacing", settings.lineSpacing.toDouble())
        put("showWordCount", settings.showWordCount)
        put("focusModeDefault", settings.focusModeDefault)
        put("appLock", settings.appLock)
        put("noteLock", settings.noteLock)
        put("autoPurgeDays", settings.autoPurgeDays)
        put("viewMode", settings.viewMode.storageKey)
        put("sortOption", settings.sortOption.storageKey)
        put("backgroundStyle", settings.backgroundStyle.storageKey)
        put("customBackgroundPath", settings.customBackgroundPath)
        put("onlineFeatures", settings.onlineFeatures)
    }

    private fun jsonToNotes(array: JSONArray): List<Note> = buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val content = item.optString("content")
            add(
                Note(
                    title = item.optString("title"),
                    content = content,
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
                    isPinned = item.optBoolean("isPinned"),
                    isArchived = item.optBoolean("isArchived"),
                    isLocked = item.optBoolean("isLocked"),
                    colorTag = NoteColorTag.fromWireValue(item.optString("colorTag")),
                    moodTag = WritingMood.fromWireValue(item.optString("moodTag")),
                    wordCount = item.optInt(
                        "wordCount",
                        WordCountUtils.countWords(content),
                    ),
                    charCount = item.optInt("charCount", content.length),
                    tags = item.optJSONArray("tags")?.let { tags ->
                        buildList {
                            for (tagIndex in 0 until tags.length()) {
                                tags.optString(tagIndex).takeIf { it.isNotBlank() }
                                    ?.let { add(it) }
                            }
                        }
                    }.orEmpty(),
                ),
            )
        }
    }

    private fun jsonToTemplates(array: JSONArray?): List<NoteTemplate> = buildList {
        if (array == null) return@buildList
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            add(
                NoteTemplate(
                    name = item.optString("name").ifBlank { "Imported template" },
                    titleTemplate = item.optString("titleTemplate"),
                    contentTemplate = item.optString("contentTemplate"),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                ),
            )
        }
    }

    private fun jsonToSettings(item: JSONObject): NyxSettings = NyxSettings(
        theme = NyxTheme.fromKey(item.optString("theme")),
        accent = AccentChoice.fromKey(item.optString("accent")),
        fontSize = item.optInt("fontSize", 15),
        fontStyle = NyxFontStyle.fromKey(item.optString("fontStyle")),
        autoSave = item.optBoolean("autoSave", true),
        lineSpacing = item.optDouble("lineSpacing", 1.5).toFloat(),
        showWordCount = item.optBoolean("showWordCount", true),
        focusModeDefault = item.optBoolean("focusModeDefault", false),
        appLock = item.optBoolean("appLock", false),
        noteLock = item.optBoolean("noteLock", false),
        autoPurgeDays = item.optInt("autoPurgeDays", 30),
        viewMode = ViewMode.fromKey(item.optString("viewMode")),
        sortOption = SortOption.fromKey(item.optString("sortOption")),
        backgroundStyle = BackgroundStyle.fromKey(item.optString("backgroundStyle")),
        customBackgroundPath = item.optString("customBackgroundPath"),
        onlineFeatures = item.optBoolean("onlineFeatures", false),
    )

    private fun openInput(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: error("Unable to open the selected file")

    private fun openOutput(uri: Uri): OutputStream =
        context.contentResolver.openOutputStream(uri, "wt")
            ?: error("Unable to create the selected file")

    private fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }
}
