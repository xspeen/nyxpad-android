package com.nyxpad.app.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

object QrCodeUtils {
    const val MAX_QR_CHARS = 1500

    fun payload(title: String, content: String): String = JSONObject().apply {
        put("app", "nyxpad")
        put("format", 1)
        put("title", title)
        put("content", content)
    }.toString()

    fun decode(raw: String): Pair<String?, String> {
        return runCatching {
            val json = JSONObject(raw)
            if (json.optString("app") == "nyxpad") {
                json.optString("title").takeIf { it.isNotBlank() } to
                    json.optString("content")
            } else {
                null to raw
            }
        }.getOrElse { null to raw }
    }

    fun generate(content: String, size: Int = 760): Bitmap? = runCatching {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val offset = y * size
            for (x in 0 until size) {
                pixels[offset + x] = if (matrix[x, y]) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }.getOrNull()
}
