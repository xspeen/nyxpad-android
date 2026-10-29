package com.nyxpad.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyxpad.app.ui.components.AppLockGate
import com.nyxpad.app.ui.navigation.NavGraph
import com.nyxpad.app.ui.navigation.Screen

@Composable
fun NyxPadRoot(
    startDestination: String,
    newNoteRequest: Boolean,
    onNewNoteConsumed: () -> Unit,
    appLocked: Boolean,
    appUnlocked: Boolean,
    onUnlock: () -> Unit,
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            onAppUnlocked = onUnlock,
        )

        LaunchedEffect(newNoteRequest, currentRoute) {
            if (!newNoteRequest || currentRoute == null) return@LaunchedEffect
            when (currentRoute) {
                Screen.Onboarding.route -> Unit
                Screen.Editor.route -> onNewNoteConsumed()
                else -> {
                    navController.navigate(Screen.Editor.createRoute()) {
                        launchSingleTop = true
                    }
                    onNewNoteConsumed()
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
