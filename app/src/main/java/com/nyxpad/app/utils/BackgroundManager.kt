package com.nyxpad.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

object BackgroundManager {
    private const val DIRECTORY = "backgrounds"
    private const val FILE_NAME = "custom_background.jpg"

    fun copyToAppStorage(context: Context, uri: Uri): String? = runCatching {
        val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
        val target = File(directory, FILE_NAME)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    }.getOrNull()

    fun load(path: String): Bitmap? = runCatching {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = 2
        }
        BitmapFactory.decodeFile(path, options)
    }.getOrNull()
}
