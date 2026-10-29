package com.nyxpad.app.data.repository

import com.nyxpad.app.data.local.NoteEntity
import com.nyxpad.app.data.local.TemplateEntity
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.model.WritingMood

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isPinned = isPinned,
    isArchived = isArchived,
    isDeleted = isDeleted,
    isLocked = isLocked,
    colorTag = NoteColorTag.fromWireValue(colorTag),
    moodTag = WritingMood.fromWireValue(moodTag),
    wordCount = wordCount,
    charCount = charCount,
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isPinned = isPinned,
    isArchived = isArchived,
    isDeleted = isDeleted,
    isLocked = isLocked,
    colorTag = colorTag.wireValue,
    moodTag = moodTag.wireValue,
    wordCount = wordCount,
    charCount = charCount,
)

fun TemplateEntity.toDomain(): NoteTemplate = NoteTemplate(
    id = id,
    name = name,
    titleTemplate = titleTemplate,
    contentTemplate = contentTemplate,
    createdAt = createdAt,
)

fun NoteTemplate.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    titleTemplate = titleTemplate,
    contentTemplate = contentTemplate,
    createdAt = createdAt,
)
