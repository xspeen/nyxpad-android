package com.nyxpad.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.data.preferences.SortOption
import com.nyxpad.app.data.preferences.ViewMode
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.NoteTemplate
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.utils.WordCountUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val pinnedNotes: List<Note> = emptyList(),
    val settings: NyxSettings = NyxSettings(),
    val query: String = "",
    val searchActive: Boolean = false,
    val refreshing: Boolean = false,
    val isLoading: Boolean = true,
    val allTags: List<String> = emptyList(),
    val selectedTag: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val preferences: NyxPreferences,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val searchActive = MutableStateFlow(false)
    private val refreshing = MutableStateFlow(false)
    private val selectedTag = MutableStateFlow<String?>(null)

    private val notesFlow: Flow<List<Note>> = query
        .debounce(120)
        .flatMapLatest { rawQuery ->
            val trimmed = rawQuery.trim()
            if (trimmed.isEmpty()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(trimmed)
            }
        }

    private val baseState: Flow<HomeUiState> = combine(
        notesFlow,
        preferences.settingsFlow,
        query,
        searchActive,
        refreshing,
    ) { notes, settings, currentQuery, isSearching, isRefreshing ->
        val sorted = sortNotes(notes, settings.sortOption)
        HomeUiState(
            notes = sorted,
            pinnedNotes = sorted.filter { it.isPinned },
            settings = settings,
            query = currentQuery,
            searchActive = isSearching,
            refreshing = isRefreshing,
            isLoading = false,
            allTags = sorted.flatMap { it.tags }.distinct().sorted(),
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseState,
        selectedTag,
    ) { state, tag ->
        val filtered = if (tag == null) {
            state.notes
        } else {
            state.notes.filter { note -> note.tags.any { it.equals(tag, ignoreCase = true) } }
        }
        state.copy(
            notes = filtered,
            pinnedNotes = filtered.filter { it.isPinned },
            selectedTag = tag,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    val templates: StateFlow<List<NoteTemplate>> = repository.getTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun openSearch() {
        searchActive.value = true
    }

    fun closeSearch() {
        searchActive.value = false
        query.value = ""
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setSelectedTag(tag: String?) {
        selectedTag.value = tag
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            preferences.setViewMode(mode)
        }
    }

    fun setSortOption(option: SortOption) {
        viewModelScope.launch {
            preferences.setSortOption(option)
        }
    }

    fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            delay(550)
            refreshing.value = false
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updatePinStatus(note.id, !note.isPinned)
        }
    }

    fun archive(note: Note) {
        viewModelScope.launch {
            repository.updateArchiveStatus(note.id, true)
        }
    }

    fun moveToTrash(note: Note) {
        viewModelScope.launch {
            repository.softDeleteNote(note.id)
        }
    }

    fun setColor(note: Note, tag: NoteColorTag) {
        viewModelScope.launch {
            repository.updateColorTag(note.id, tag.wireValue)
        }
    }

    fun setMood(note: Note, mood: WritingMood) {
        viewModelScope.launch {
            repository.updateMoodTag(note.id, mood.wireValue)
        }
    }

    fun setLocked(note: Note, locked: Boolean) {
        viewModelScope.launch {
            repository.updateLockStatus(note.id, locked)
        }
    }

    fun bulkPin(ids: List<Long>, pinned: Boolean) {
        viewModelScope.launch { repository.bulkPin(ids, pinned) }
    }

    fun bulkArchive(ids: List<Long>) {
        viewModelScope.launch { repository.bulkArchive(ids, true) }
    }

    fun bulkDelete(ids: List<Long>) {
        viewModelScope.launch { repository.bulkDelete(ids) }
    }

    fun bulkRestore(ids: List<Long>) {
        viewModelScope.launch { repository.bulkRestore(ids) }
    }

    fun bulkTag(ids: List<Long>, tags: List<String>) {
        viewModelScope.launch { repository.bulkTag(ids, tags) }
    }

    fun restoreNotes(ids: List<Long>) {
        viewModelScope.launch { repository.bulkRestore(ids) }
    }

    fun createNoteFromText(title: String?, text: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = repository.saveNote(
                Note(
                    title = title.orEmpty(),
                    content = text,
                    createdAt = now,
                    updatedAt = now,
                    wordCount = WordCountUtils.countWords(text),
                    charCount = text.length,
                    tags = listOf("shared"),
                ),
            )
            onCreated(id)
        }
    }

    fun duplicate(note: Note, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val title = if (note.title.isBlank()) {
                "Untitled copy"
            } else {
                "${note.title} (copy)"
            }
            val copy = note.copy(
                id = 0L,
                title = title,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                isPinned = false,
                isArchived = false,
                isDeleted = false,
            )
            val id = repository.saveNote(copy)
            onCreated(id)
        }
    }

    fun createFromTemplate(template: NoteTemplate, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val content = template.contentTemplate
            val note = Note(
                title = template.titleTemplate.ifBlank { template.name },
                content = content,
                createdAt = now,
                updatedAt = now,
                wordCount = WordCountUtils.countWords(content),
                charCount = WordCountUtils.countCharacters(content),
            )
            val id = repository.saveNote(note)
            onCreated(id)
        }
    }

    fun deleteTemplate(template: NoteTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template.id)
        }
    }

    private fun sortNotes(notes: List<Note>, option: SortOption): List<Note> {
        val comparator: Comparator<Note> = when (option) {
            SortOption.LastEdited -> compareByDescending { it.updatedAt }
            SortOption.DateCreated -> compareByDescending { it.createdAt }
            SortOption.TitleAZ -> compareBy { it.displayTitle.lowercase(Locale.getDefault()) }
            SortOption.TitleZA -> compareByDescending {
                it.displayTitle.lowercase(Locale.getDefault())
            }
        }
        return notes.sortedWith(
            compareByDescending<Note> { it.isPinned }.then(comparator),
        )
    }
}
