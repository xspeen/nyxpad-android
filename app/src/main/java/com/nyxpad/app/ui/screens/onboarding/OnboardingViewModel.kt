package com.nyxpad.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: NyxPreferences,
) : ViewModel() {
    fun complete(onFinished: () -> Unit) {
        viewModelScope.launch {
            preferences.setOnboardingComplete(true)
            withContext(Dispatchers.Main) { onFinished() }
        }
    }
}
