package com.nyxpad.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.ui.NyxPadRoot
import com.nyxpad.app.ui.screens.splash.SplashScreen
import com.nyxpad.app.ui.theme.NyxBackground
import com.nyxpad.app.ui.theme.NyxPadTheme
import com.nyxpad.app.utils.PendingShare
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var newNoteRequest by mutableStateOf(false)
    private var scanQrRequest by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value == null }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        handleIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val appUnlocked by viewModel.appUnlocked.collectAsStateWithLifecycle()
            val settings = uiState?.settings ?: NyxSettings()
            NyxPadTheme(settings = settings) {
                val state = uiState
                if (state == null) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NyxBackground),
                    ) {
                        SplashScreen()
                    }
                } else {
                    NyxPadRoot(
                        startDestination = if (state.onboardingComplete) {
                            com.nyxpad.app.ui.navigation.Screen.Home.route
                        } else {
                            com.nyxpad.app.ui.navigation.Screen.Onboarding.route
                        },
                        newNoteRequest = newNoteRequest,
                        onNewNoteConsumed = { newNoteRequest = false },
                        scanQrRequest = scanQrRequest,
                        onScanQrConsumed = { scanQrRequest = false },
                        appLocked = state.settings.appLock,
                        appUnlocked = appUnlocked,
                        onUnlock = viewModel::markAppUnlocked,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when {
            intent.getBooleanExtra(EXTRA_NEW_NOTE, false) ||
                intent.action == ACTION_NEW_NOTE -> {
                newNoteRequest = true
            }

            intent.action == ACTION_SCAN_QR -> {
                scanQrRequest = true
            }

            intent.action == Intent.ACTION_SEND -> {
                val shared = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
                if (!shared.isNullOrBlank()) {
                    PendingShare.set(null, shared)
                    newNoteRequest = true
                }
            }

            intent.action == Intent.ACTION_PROCESS_TEXT -> {
                val shared = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                if (!shared.isNullOrBlank()) {
                    PendingShare.set(null, shared)
                    newNoteRequest = true
                }
            }
        }
        // The intent may be replayed after a configuration change; consume it
        // so a rotation does not open another editor or scanner.
        intent.action = null
        intent.removeExtra(EXTRA_NEW_NOTE)
        intent.removeExtra(Intent.EXTRA_TEXT)
        intent.removeExtra(Intent.EXTRA_PROCESS_TEXT)
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            viewModel.lockApp()
        }
        super.onStop()
    }

    companion object {
        const val EXTRA_NEW_NOTE = "com.nyxpad.app.extra.NEW_NOTE"
        const val ACTION_NEW_NOTE = "com.nyxpad.app.action.NEW_NOTE"
        const val ACTION_SCAN_QR = "com.nyxpad.app.action.SCAN_QR"
    }
}
