package com.nyxpad.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyxpad.app.ui.components.AppLockGate
import com.nyxpad.app.ui.components.NyxBackdrop
import com.nyxpad.app.ui.navigation.NavGraph
import com.nyxpad.app.ui.navigation.Screen

@Composable
fun NyxPadRoot(
    startDestination: String,
    newNoteRequest: Boolean,
    onNewNoteConsumed: () -> Unit,
    scanQrRequest: Boolean,
    onScanQrConsumed: () -> Unit,
    appLocked: Boolean,
    appUnlocked: Boolean,
    onUnlock: () -> Unit,
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    NyxBackdrop(modifier = Modifier.fillMaxSize()) {
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            onAppUnlocked = onUnlock,
            scanQrRequest = scanQrRequest,
            onScanQrConsumed = onScanQrConsumed,
        )

        LaunchedEffect(newNoteRequest, currentRoute) {
            if (!newNoteRequest || currentRoute == null) return@LaunchedEffect
            when (currentRoute) {
                Screen.Onboarding.route -> Unit
                // Keep the note already open in the back stack so its unsaved
                // text is not lost, and push a fresh editor on top.
                Screen.Editor.route -> {
                    onNewNoteConsumed()
                    navController.navigate(Screen.Editor.createRoute())
                }

                else -> {
                    onNewNoteConsumed()
                    navController.navigate(Screen.Editor.createRoute()) {
                        launchSingleTop = true
                    }
                }
            }
        }

        LaunchedEffect(scanQrRequest, currentRoute) {
            if (!scanQrRequest || currentRoute == null) return@LaunchedEffect
            if (currentRoute == Screen.Onboarding.route) return@LaunchedEffect
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }

        AnimatedVisibility(
            visible = appLocked && !appUnlocked,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(350)),
        ) {
            AppLockGate(onUnlocked = onUnlock)
        }
    }
}
