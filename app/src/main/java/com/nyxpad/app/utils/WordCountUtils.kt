package com.nyxpad.app.utils

import kotlin.math.max

object WordCountUtils {
    private val wordPattern = Regex("\\S+")

    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return wordPattern.findAll(text).count()
    }

    fun countCharacters(text: String): Int = text.length

    fun formatWordCount(count: Int): String =
        if (count == 1) "1 word" else "$count words"

    fun formatCharacterCount(count: Int): String =
        if (count == 1) "1 character" else "$count characters"

    fun readingMinutes(wordCount: Int): Int = max(1, (wordCount + 199) / 200)
}
