package com.nyxpad.app.domain.model

/**
 * A note as understood by the app. The persistence layer is kept deliberately
 * unaware of UI concerns, while these enums provide a safe bridge between the
 * stable string values stored in Room and the interface.
 */
data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isLocked: Boolean = false,
    val colorTag: NoteColorTag = NoteColorTag.Default,
    val moodTag: WritingMood = WritingMood.None,
    val wordCount: Int = 0,
    val charCount: Int = 0,
) {
    val displayTitle: String
        get() = title.ifBlank { "Untitled note" }

    val preview: String
        get() = content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}

enum class NoteColorTag(val wireValue: String, val label: String) {
    Default("default", "Default"),
    Purple("purple", "Purple"),
    Blue("blue", "Blue"),
    Green("green", "Green"),
    Red("red", "Red"),
    Gold("gold", "Gold");

    companion object {
        fun fromWireValue(value: String?): NoteColorTag =
            entries.firstOrNull { it.wireValue == value } ?: Default
    }
}

enum class WritingMood(val wireValue: String, val label: String) {
    None("none", "No mood"),
    Focused("focused", "Focused"),
    Creative("creative", "Creative"),
    Random("random", "Random"),
    Urgent("urgent", "Urgent"),
    Personal("personal", "Personal");

    companion object {
        fun fromWireValue(value: String?): WritingMood =
            entries.firstOrNull { it.wireValue == value } ?: None

        val selectable: List<WritingMood> = listOf(
            Focused,
            Creative,
            Random,
            Urgent,
            Personal,
        )
    }
}
