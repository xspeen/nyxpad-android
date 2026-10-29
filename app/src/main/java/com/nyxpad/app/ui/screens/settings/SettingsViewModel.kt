package com.nyxpad.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.AccentChoice
import com.nyxpad.app.data.preferences.NyxFontStyle
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.data.preferences.NyxTheme
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: NyxPreferences,
    private val repository: NoteRepository,
) : ViewModel() {

    val settings: StateFlow<NyxSettings> = preferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NyxSettings(),
    )

    fun setTheme(theme: NyxTheme) {
        viewModelScope.launch { preferences.setTheme(theme) }
    }

    fun setAccent(accent: AccentChoice) {
        viewModelScope.launch { preferences.setAccent(accent) }
    }

    fun setFontStyle(style: NyxFontStyle) {
        viewModelScope.launch { preferences.setFontStyle(style) }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch { preferences.setFontSize(size) }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch { preferences.setLineSpacing(spacing) }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoSave(enabled) }
    }

    fun setShowWordCount(enabled: Boolean) {
        viewModelScope.launch { preferences.setShowWordCount(enabled) }
    }

    fun setFocusModeDefault(enabled: Boolean) {
        viewModelScope.launch { preferences.setFocusModeDefault(enabled) }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch { preferences.setAppLock(enabled) }
    }

    fun setNoteLock(enabled: Boolean) {
        viewModelScope.launch { preferences.setNoteLock(enabled) }
    }

    fun setAutoPurgeDays(days: Int) {
        viewModelScope.launch { preferences.setAutoPurgeDays(days) }
    }

    suspend fun allNotesForExport(): List<Note> = repository.getAllNotesForExport()

    suspend fun clearAllNotes() {
        repository.clearAllNotes()
    }

    fun replayOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferences.setOnboardingComplete(false)
            onDone()
        }
    }
}
