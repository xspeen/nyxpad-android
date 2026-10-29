package com.nyxpad.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.WritingStats
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val stats: WritingStats = WritingStats(),
    val settings: NyxSettings = NyxSettings(),
    val streakHistory: List<Long> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: NoteRepository,
    preferences: NyxPreferences,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getNotesForStats(),
        preferences.settingsFlow,
    ) { notes, settings ->
        StatsUiState(
            stats = buildStats(notes, settings),
            settings = settings,
            streakHistory = settings.streakHistory,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    private fun buildStats(notes: List<Note>, settings: NyxSettings): WritingStats {
        val activeNotes = notes.filterNot { it.isDeleted }
        val totalWords = activeNotes.sumOf { it.wordCount }
        val averageWords = if (activeNotes.isEmpty()) 0 else totalWords / activeNotes.size
        val dayCounts = MutableList(7) { 0 }
        activeNotes.forEach { note ->
            val day = note.createdAt.asLocalDate().dayOfWeek.value - 1
            if (day in 0..6) dayCounts[day] += 1
        }
        val months = DateUtils.lastSixMonths()
        val monthlyCounts = months.map { month ->
            activeNotes.count { YearMonth.from(it.createdAt.asLocalDate()) == month }
        }
        val monthlyLabels = months.map { DateUtils.formatMonth(it) }
        val mostProductiveIndex = dayCounts.indices.maxByOrNull { dayCounts[it] } ?: 0
        val mostProductiveDay = if (dayCounts[mostProductiveIndex] == 0) {
            "—"
        } else {
            DayOfWeek.of(mostProductiveIndex + 1)
                .getDisplayName(TextStyle.FULL, Locale.getDefault())
        }
        return WritingStats(
            totalNotes = activeNotes.size,
            totalWords = totalWords,
            averageWords = averageWords,
            dayOfWeekCounts = dayCounts,
            monthlyCounts = monthlyCounts,
            monthlyLabels = monthlyLabels,
            mostProductiveDay = mostProductiveDay,
            longestStreak = settings.longestStreak,
            currentStreak = settings.currentStreak,
        )
    }

    private fun Long.asLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
