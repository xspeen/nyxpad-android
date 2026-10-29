package com.nyxpad.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(
        "SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 " +
            "ORDER BY updatedAt DESC",
    )
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes WHERE isPinned = 1 AND isDeleted = 0 AND isArchived = 0 " +
            "ORDER BY updatedAt DESC",
    )
    fun getPinnedNotes(): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR " +
            "content LIKE '%' || :query || '%') AND isDeleted = 0 AND isArchived = 0 " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotesForStats(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteNote(id: Long, deletedAt: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedAt = null WHERE id = :id")
    suspend fun restoreNote(id: Long)

    @Delete
    suspend fun permanentlyDeleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :cutoffTime")
    suspend fun purgeOldDeletedNotes(cutoffTime: Long)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean)

    @Query("UPDATE notes SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)

    @Query("UPDATE notes SET isLocked = :isLocked WHERE id = :id")
    suspend fun updateLockStatus(id: Long, isLocked: Boolean)

    @Query("UPDATE notes SET colorTag = :colorTag WHERE id = :id")
    suspend fun updateColorTag(id: Long, colorTag: String)

    @Query("UPDATE notes SET moodTag = :moodTag WHERE id = :id")
    suspend fun updateMoodTag(id: Long, moodTag: String)

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    suspend fun getTotalNoteCount(): Int

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM notes WHERE isDeleted = 0")
    suspend fun getTotalWordCount(): Int

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getAllNotesForExport(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}
