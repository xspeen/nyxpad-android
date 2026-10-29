package com.nyxpad.app.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    val uiState: StateFlow<ArchiveUiState> = repository.getArchivedNotes()
        .map { notes -> ArchiveUiState(notes = notes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchiveUiState(),
        )

    fun unarchive(note: Note) {
        viewModelScope.launch {
            repository.updateArchiveStatus(note.id, false)
        }
    }

    fun moveToTrash(note: Note) {
        viewModelScope.launch {
            repository.softDeleteNote(note.id)
        }
    }
}
