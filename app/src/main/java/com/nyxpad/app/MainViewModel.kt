package com.nyxpad.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    val onboardingComplete: Boolean,
    val settings: NyxSettings,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: NyxPreferences,
) : ViewModel() {
    val uiState: StateFlow<MainUiState?> = combine(
        preferences.onboardingComplete,
        preferences.settingsFlow,
    ) { onboardingComplete, settings ->
        MainUiState(onboardingComplete = onboardingComplete, settings = settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val _appUnlocked = MutableStateFlow(false)
    val appUnlocked: StateFlow<Boolean> = _appUnlocked.asStateFlow()

    fun markAppUnlocked() {
        _appUnlocked.value = true
    }

    fun lockApp() {
        _appUnlocked.value = false
    }
}
