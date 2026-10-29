package com.nyxpad.app.ui.navigation

/**
 * Every destination in NyxPad. Routes are stable strings so drawer navigation,
 * widget deep links and process restoration all agree on the same addresses.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Pinned : Screen("pinned")
    data object Settings : Screen("settings")
    data object Trash : Screen("trash")
    data object Archive : Screen("archive")
    data object Stats : Screen("stats")
    data object About : Screen("about")

    data object Editor : Screen("editor?noteId={noteId}") {
        const val ARG_NOTE_ID = "noteId"
        const val BASE_ROUTE = "editor"

        fun createRoute(noteId: Long = 0L): String = "$BASE_ROUTE?$ARG_NOTE_ID=$noteId"
    }
}
