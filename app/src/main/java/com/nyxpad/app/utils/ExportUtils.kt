package com.nyxpad.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.nyxpad.app.domain.model.Note
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExportUtils {
    fun shareText(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share note"))
    }

    suspend fun shareAsFile(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String,
    ) {
        val file = withContext(Dispatchers.IO) {
            val exportDirectory = File(context.cacheDir, "exports").apply { mkdirs() }
            File(exportDirectory, sanitizeFileName(fileName)).apply {
                writeText(content, Charsets.UTF_8)
            }
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export note"))
    }

    fun plainText(note: Note): String {
        val title = note.title.ifBlank { "Untitled note" }
        return buildString {
            appendLine(title)
            appendLine("Created ${DateUtils.formatEditorDate(note.createdAt)}")
            appendLine("Updated ${DateUtils.formatEditorDate(note.updatedAt)}")
            appendLine()
            append(note.content)
        }
    }

    fun markdown(note: Note): String {
        val title = note.title.ifBlank { "Untitled note" }
        return buildString {
            appendLine("# $title")
            appendLine()
            append(note.content)
        }
    }

    fun allNotesAsText(notes: List<Note>): String = buildString {
        appendLine("NyxPad export")
        appendLine("Generated ${DateUtils.formatEditorDate(System.currentTimeMillis())}")
        appendLine("Notes: ${notes.size}")
        appendLine()
        notes.forEachIndexed { index, note ->
            appendLine("============================================================")
            appendLine("${index + 1}. ${note.title.ifBlank { "Untitled note" }}")
            appendLine("Updated: ${DateUtils.formatEditorDate(note.updatedAt)}")
            appendLine("------------------------------------------------------------")
            appendLine(note.content)
            appendLine()
        }
    }

    private fun sanitizeFileName(name: String): String {
        val safe = name.replace(Regex("[^A-Za-z0-9._-]"), "_").trim('_')
        return if (safe.isBlank()) "nyxpad_note.txt" else safe
    }
}
