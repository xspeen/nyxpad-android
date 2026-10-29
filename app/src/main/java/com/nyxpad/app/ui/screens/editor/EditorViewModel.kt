package com.nyxpad.app.ui.screens.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.ui.navigation.Screen
import com.nyxpad.app.utils.PendingShare
import com.nyxpad.app.utils.WordCountUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val colorTag: NoteColorTag = NoteColorTag.Default,
    val moodTag: WritingMood = WritingMood.None,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val lastSavedAt: Long? = null,
    val focusMode: Boolean = false,
    val settings: NyxSettings = NyxSettings(),
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
    private val preferences: NyxPreferences,
) : ViewModel() {

    private val noteIdArgument: Long =
        savedStateHandle.get<Long>(Screen.Editor.ARG_NOTE_ID) ?: 0L

    private val sharedContent = if (noteIdArgument == 0L) {
        PendingShare.consume()
    } else {
        null
    }

    private val _state = MutableStateFlow(
        EditorUiState(
            noteId = noteIdArgument,
            title = sharedContent?.first.orEmpty(),
            content = sharedContent?.second.orEmpty(),
            wordCount = WordCountUtils.countWords(sharedContent?.second.orEmpty()),
            charCount = sharedContent?.second.orEmpty().length,
            isLoading = noteIdArgument != 0L,
        ),
    )
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val saveMutex = Mutex()
    private var saveJob: Job? = null
    private var settingsLoaded = false

    init {
        viewModelScope.launch {
            preferences.settingsFlow.collect { settings ->
                if (!settingsLoaded) {
                    settingsLoaded = true
                    _state.update { current ->
                        current.copy(
                            settings = settings,
                            focusMode = if (noteIdArgument == 0L) {
                                settings.focusModeDefault
                            } else {
                                current.focusMode
                            },
                        )
                    }
                } else {
                    _state.update { it.copy(settings = settings) }
                }
            }
        }
        if (noteIdArgument != 0L) {
            loadNote()
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            val note = repository.getNoteById(noteIdArgument)
            _state.update { current ->
                if (note == null) {
                    current.copy(noteId = 0L, isLoading = false)
                } else {
                    current.copy(
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        createdAt = note.createdAt,
                        updatedAt = note.updatedAt,
                        isPinned = note.isPinned,
                        isArchived = note.isArchived,
                        isLocked = note.isLocked,
                        colorTag = note.colorTag,
                        moodTag = note.moodTag,
                        wordCount = note.wordCount,
                        charCount = note.charCount,
                        tags = note.tags,
                        isLoading = false,
                        lastSavedAt = note.updatedAt,
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        if (_state.value.isLoading) return
        _state.update {
            it.copy(
                title = value,
                updatedAt = System.currentTimeMillis(),
            )
        }
        scheduleAutoSave()
    }

    fun onContentChange(value: String) {
        if (_state.value.isLoading) return
        _state.update {
            it.copy(
                content = value,
                wordCount = WordCountUtils.countWords(value),
                charCount = value.length,
                updatedAt = System.currentTimeMillis(),
            )
        }
        scheduleAutoSave()
    }

    fun toggleFocusMode() {
        _state.update { it.copy(focusMode = !it.focusMode) }
    }

    fun togglePin() {
        _state.update { it.copy(isPinned = !it.isPinned) }
        persistMetadata()
    }

    fun setColor(tag: NoteColorTag) {
        _state.update { it.copy(colorTag = tag) }
        persistMetadata()
    }

    fun setMood(mood: WritingMood) {
        _state.update { it.copy(moodTag = mood) }
        persistMetadata()
    }

    fun setTags(tags: List<String>) {
        val clean = tags
            .map { it.replace(',', ' ').trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(12)
        _state.update { it.copy(tags = clean) }
        persistMetadata()
    }

    fun setLocked(locked: Boolean) {
        _state.update { it.copy(isLocked = locked) }
        persistMetadata()
    }

    fun saveNow() {
        saveJob?.cancel()
        viewModelScope.launch {
            saveInternal()
        }
    }

    fun saveAndExit(onDone: () -> Unit) {
        saveJob?.cancel()
        viewModelScope.launch {
            saveInternal()
            onDone()
        }
    }

    fun archiveAndExit(onDone: () -> Unit) {
        saveJob?.cancel()
        viewModelScope.launch {
            saveInternal()
            val id = _state.value.noteId
            if (id != 0L) {
                repository.updateArchiveStatus(id, true)
                _events.emit("Note archived")
            }
            onDone()
        }
    }

    fun deleteAndExit(onDone: () -> Unit) {
        saveJob?.cancel()
        viewModelScope.launch {
            saveInternal()
            val id = _state.value.noteId
            if (id != 0L) {
                repository.softDeleteNote(id)
                _events.emit("Moved to trash")
            }
            onDone()
        }
    }

    fun saveAsTemplate(name: String) {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank()) {
            _events.tryEmit("Write something first")
            return
        }
        viewModelScope.launch {
            repository.saveTemplate(
                NoteTemplate(
                    name = name.ifBlank { "Untitled template" },
                    titleTemplate = current.title,
                    contentTemplate = current.content,
                ),
            )
            _events.emit("Template saved")
        }
    }

    private fun persistMetadata() {
        saveJob?.cancel()
        viewModelScope.launch {
            saveInternal()
        }
    }

    private fun scheduleAutoSave() {
        if (!_state.value.settings.autoSave) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(700)
            saveInternal()
        }
    }

    private suspend fun saveInternal() {
        val snapshot = _state.value
        if (snapshot.isLoading) return
        // A brand-new note with no text has nothing to persist. An existing note
        // must still be saved when its text is cleared, otherwise deleting all
        // content would silently restore the old text on the next launch.
        // Metadata (tags, colour, mood, pin, lock) is worth persisting on its own.
        if (snapshot.noteId == 0L && snapshot.title.isBlank() &&
            snapshot.content.isBlank() && !snapshot.hasMetadata()
        ) {
            return
        }

        saveMutex.withLock {
            val latest = _state.value
            if (latest.noteId == 0L && latest.title.isBlank() &&
                latest.content.isBlank() && !latest.hasMetadata()
            ) {
                return@withLock
            }
            _state.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val note = Note(
                id = latest.noteId,
                title = latest.title,
                content = latest.content,
                createdAt = latest.createdAt,
                updatedAt = now,
                isPinned = latest.isPinned,
                isArchived = latest.isArchived,
                isLocked = latest.isLocked,
                colorTag = latest.colorTag,
                moodTag = latest.moodTag,
                wordCount = WordCountUtils.countWords(latest.content),
                charCount = latest.content.length,
                tags = latest.tags,
            )
            val savedId = repository.saveNote(note)
            if (latest.content.isNotBlank()) {
                preferences.recordWritingDay()
            }
            _state.update { current ->
                current.copy(
                    noteId = if (current.noteId == 0L) savedId else current.noteId,
                    updatedAt = now,
                    wordCount = note.wordCount,
                    charCount = note.charCount,
                    isSaving = false,
                    lastSavedAt = now,
                )
            }
        }
    }

    private fun EditorUiState.hasMetadata(): Boolean =
        isPinned || isArchived || isLocked ||
            colorTag != NoteColorTag.Default ||
            moodTag != WritingMood.None ||
            tags.isNotEmpty()
}
