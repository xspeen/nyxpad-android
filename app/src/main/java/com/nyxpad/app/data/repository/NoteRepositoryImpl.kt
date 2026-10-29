package com.nyxpad.app.data.repository

import com.nyxpad.app.data.local.NoteDao
import com.nyxpad.app.data.local.TemplateDao
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val templateDao: TemplateDao,
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().mapEntities().flowOn(Dispatchers.IO)

    override fun getArchivedNotes(): Flow<List<Note>> =
        noteDao.getArchivedNotes().mapEntities().flowOn(Dispatchers.IO)

    override fun getDeletedNotes(): Flow<List<Note>> =
        noteDao.getDeletedNotes().mapEntities().flowOn(Dispatchers.IO)

    override fun getPinnedNotes(): Flow<List<Note>> =
        noteDao.getPinnedNotes().mapEntities().flowOn(Dispatchers.IO)

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).mapEntities().flowOn(Dispatchers.IO)

    override fun getNotesForStats(): Flow<List<Note>> =
        noteDao.getNotesForStats().mapEntities().flowOn(Dispatchers.IO)

    override suspend fun getNoteById(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO) {
        if (note.id == 0L) {
            noteDao.insertNote(note.toEntity())
        } else {
            noteDao.updateNote(note.toEntity())
            note.id
        }
    }

    override suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun softDeleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.softDeleteNote(id, System.currentTimeMillis())
    }

    override suspend fun restoreNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.restoreNote(id)
    }

    override suspend fun permanentlyDeleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.permanentlyDeleteNote(note.toEntity())
    }

    override suspend fun purgeOldDeletedNotes(cutoffTime: Long) = withContext(Dispatchers.IO) {
        noteDao.purgeOldDeletedNotes(cutoffTime)
    }

    override suspend fun updatePinStatus(id: Long, isPinned: Boolean) =
        withContext(Dispatchers.IO) {
            noteDao.updatePinStatus(id, isPinned)
        }

    override suspend fun updateArchiveStatus(id: Long, isArchived: Boolean) =
        withContext(Dispatchers.IO) {
            noteDao.updateArchiveStatus(id, isArchived)
        }

    override suspend fun updateLockStatus(id: Long, isLocked: Boolean) =
        withContext(Dispatchers.IO) {
            noteDao.updateLockStatus(id, isLocked)
        }

    override suspend fun updateColorTag(id: Long, colorTag: String) =
        withContext(Dispatchers.IO) {
            noteDao.updateColorTag(id, colorTag)
        }

    override suspend fun updateMoodTag(id: Long, moodTag: String) =
        withContext(Dispatchers.IO) {
            noteDao.updateMoodTag(id, moodTag)
        }

    override suspend fun updateTags(id: Long, tags: List<String>) =
        withContext(Dispatchers.IO) {
            noteDao.updateTags(id, normalizeTags(tags))
        }

    override suspend fun bulkPin(ids: List<Long>, isPinned: Boolean) =
        withContext(Dispatchers.IO) {
            if (ids.isNotEmpty()) noteDao.updatePinStatusBulk(ids, isPinned)
        }

    override suspend fun bulkArchive(ids: List<Long>, isArchived: Boolean) =
        withContext(Dispatchers.IO) {
            if (ids.isNotEmpty()) noteDao.updateArchiveStatusBulk(ids, isArchived)
        }

    override suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
        if (ids.isNotEmpty()) {
            noteDao.softDeleteNotesBulk(ids, System.currentTimeMillis())
        }
    }

    override suspend fun bulkRestore(ids: List<Long>) = withContext(Dispatchers.IO) {
        if (ids.isNotEmpty()) noteDao.restoreNotesBulk(ids)
    }

    override suspend fun bulkTag(ids: List<Long>, tags: List<String>) =
        withContext(Dispatchers.IO) {
            if (ids.isNotEmpty()) noteDao.updateTagsBulk(ids, normalizeTags(tags))
        }

    override suspend fun getTotalNoteCount(): Int = withContext(Dispatchers.IO) {
        noteDao.getTotalNoteCount()
    }

    override suspend fun getTotalWordCount(): Int = withContext(Dispatchers.IO) {
        noteDao.getTotalWordCount()
    }

    override suspend fun getAllNotesForExport(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotesForExport().map { it.toDomain() }
    }

    override suspend fun getAllNotesForBackup(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotesForBackup().map { it.toDomain() }
    }

    override suspend fun saveNotes(notes: List<Note>) = withContext(Dispatchers.IO) {
        noteDao.insertNotes(notes.map { it.copy(id = 0L).toEntity() })
        Unit
    }

    override suspend fun clearAllNotes() = withContext(Dispatchers.IO) {
        noteDao.clearAllNotes()
    }

    override fun getTemplates(): Flow<List<NoteTemplate>> =
        templateDao.getTemplates().mapTemplates().flowOn(Dispatchers.IO)

    override suspend fun getTemplatesSnapshot(): List<NoteTemplate> =
        withContext(Dispatchers.IO) {
            templateDao.getAllTemplates().map { it.toDomain() }
        }

    override suspend fun saveTemplate(template: NoteTemplate): Long =
        withContext(Dispatchers.IO) {
            templateDao.insertTemplate(template.toEntity())
        }

    override suspend fun clearAllTemplates() = withContext(Dispatchers.IO) {
        templateDao.clearAllTemplates()
    }

    override suspend fun deleteTemplate(id: Long) = withContext(Dispatchers.IO) {
        templateDao.deleteTemplate(id)
    }
}

private fun normalizeTags(tags: List<String>): String = tags
    .map { it.replace(',', ' ').trim() }
    .filter { it.isNotEmpty() }
    .distinct()
    .joinToString(",")

private fun Flow<List<com.nyxpad.app.data.local.NoteEntity>>.mapEntities(): Flow<List<Note>> =
    map { entities -> entities.map { it.toDomain() } }

private fun Flow<List<com.nyxpad.app.data.local.TemplateEntity>>.mapTemplates():
    Flow<List<NoteTemplate>> = map { entities -> entities.map { it.toDomain() } }
