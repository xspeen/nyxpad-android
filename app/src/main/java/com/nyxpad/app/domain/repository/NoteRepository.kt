package com.nyxpad.app.domain.repository

import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteTemplate
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getDeletedNotes(): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesForStats(): Flow<List<Note>>

    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun softDeleteNote(id: Long)
    suspend fun restoreNote(id: Long)
    suspend fun permanentlyDeleteNote(note: Note)
    suspend fun purgeOldDeletedNotes(cutoffTime: Long)
    suspend fun updatePinStatus(id: Long, isPinned: Boolean)
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)
    suspend fun updateLockStatus(id: Long, isLocked: Boolean)
    suspend fun updateColorTag(id: Long, colorTag: String)
    suspend fun updateMoodTag(id: Long, moodTag: String)
    suspend fun updateTags(id: Long, tags: List<String>)
    suspend fun bulkPin(ids: List<Long>, isPinned: Boolean)
    suspend fun bulkArchive(ids: List<Long>, isArchived: Boolean)
    suspend fun bulkDelete(ids: List<Long>)
    suspend fun bulkRestore(ids: List<Long>)
    suspend fun bulkTag(ids: List<Long>, tags: List<String>)
    suspend fun getTotalNoteCount(): Int
    suspend fun getTotalWordCount(): Int
    suspend fun getAllNotesForExport(): List<Note>
    suspend fun getAllNotesForBackup(): List<Note>
    suspend fun saveNotes(notes: List<Note>)
    suspend fun clearAllNotes()

    fun getTemplates(): Flow<List<NoteTemplate>>
    suspend fun getTemplatesSnapshot(): List<NoteTemplate>
    suspend fun saveTemplate(template: NoteTemplate): Long
    suspend fun clearAllTemplates()
    suspend fun deleteTemplate(id: Long)
}
