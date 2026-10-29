package com.nyxpad.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["isDeleted", "isArchived"]),
        Index(value = ["updatedAt"]),
        Index(value = ["deletedAt"]),
    ],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isLocked: Boolean = false,
    val colorTag: String = "default",
    val moodTag: String = "none",
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val tags: String = "",
)
