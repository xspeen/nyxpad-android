package com.nyxpad.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nyxpad.app.ui.screens.about.AboutScreen
import com.nyxpad.app.ui.screens.archive.ArchiveScreen
import com.nyxpad.app.ui.screens.editor.EditorScreen
import com.nyxpad.app.ui.screens.home.HomeScreen
import com.nyxpad.app.ui.screens.onboarding.OnboardingScreen
import com.nyxpad.app.ui.screens.settings.SettingsScreen
import com.nyxpad.app.ui.screens.stats.StatsScreen
import com.nyxpad.app.ui.screens.trash.TrashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    onAppUnlocked: () -> Unit,
    scanQrRequest: Boolean,
    onScanQrConsumed: () -> Unit,
) {
    val openDrawerRoute: (String) -> Unit = { route ->
        when (route) {
            Screen.Home.route -> {
                navController.popBackStack(Screen.Home.route, inclusive = false)
            }

            else -> {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }
    }

    val openEditor: (Long) -> Unit = { noteId ->
        navController.navigate(Screen.Editor.createRoute(noteId)) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(
                    animationSpec = tween(280),
                    initialOffsetX = { fullWidth -> fullWidth / 18 },
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(160))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(
                    animationSpec = tween(260),
                    targetOffsetX = { fullWidth -> fullWidth / 22 },
                )
        },
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                pinnedOnly = false,
                onOpenNote = openEditor,
                onNewNote = { openEditor(0L) },
                onNavigate = openDrawerRoute,
                scanQrRequest = scanQrRequest,
                onScanQrConsumed = onScanQrConsumed,
            )
        }

        composable(Screen.Pinned.route) {
            HomeScreen(
                pinnedOnly = true,
                onOpenNote = openEditor,
                onNewNote = { openEditor(0L) },
                onNavigate = openDrawerRoute,
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument(Screen.Editor.ARG_NOTE_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) {
            EditorScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAbout = {
                    navController.navigate(Screen.About.route) {
                        launchSingleTop = true
                    }
                },
                onReplayOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAppUnlocked = onAppUnlocked,
            )
        }

        composable(Screen.Trash.route) {
            TrashScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Archive.route) {
            ArchiveScreen(
                onBack = { navController.popBackStack() },
                onOpenNote = openEditor,
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
