package com.nyxpad.app.ui.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.utils.PreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrashUiState(
    val notes: List<Note> = emptyList(),
    val purgeDays: Int = PreferencesKeys.DEFAULT_PURGE_DAYS,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: NoteRepository,
    preferences: NyxPreferences,
) : ViewModel() {

    val uiState: StateFlow<TrashUiState> = combine(
        repository.getDeletedNotes(),
        preferences.settingsFlow,
    ) { notes, settings ->
        TrashUiState(
            notes = notes,
            purgeDays = settings.autoPurgeDays,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrashUiState(),
    )

    fun restore(note: Note) {
        viewModelScope.launch {
            repository.restoreNote(note.id)
        }
    }

    fun deleteForever(note: Note) {
        viewModelScope.launch {
            repository.permanentlyDeleteNote(note)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            uiState.value.notes.forEach { note ->
                repository.permanentlyDeleteNote(note)
            }
        }
    }
}
