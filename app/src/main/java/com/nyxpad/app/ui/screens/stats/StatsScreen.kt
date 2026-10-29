package com.nyxpad.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.domain.model.WritingStats
import com.nyxpad.app.ui.components.NyxScreenHeader
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxDisplay
import com.nyxpad.app.ui.theme.NyxHeadline
import com.nyxpad.app.ui.theme.NyxLabel
import com.nyxpad.app.ui.theme.NyxTitle
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NyxScreenHeader(
                title = "Writing stats",
                subtitle = "A quiet record of showing up",
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 44.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    StreakCard(
                        current = state.stats.currentStreak,
                        longest = state.stats.longestStreak,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            label = "Notes",
                            value = formatNumber(state.stats.totalNotes),
                            hint = "written",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Words",
                            value = formatNumber(state.stats.totalWords),
                            hint = "in total",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            label = "Average",
                            value = formatNumber(state.stats.averageWords),
                            hint = "words per note",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Best day",
                            value = state.stats.mostProductiveDay,
                            hint = "most notes",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    WeeklyRhythmCard(stats = state.stats)
                }
                item {
                    MonthlyRhythmCard(stats = state.stats)
                }
                item {
                    StreakCalendarCard(history = state.streakHistory)
                }
                item {
                    Text(
                        text = "Keep showing up. The numbers follow.",
                        style = NyxCaption,
                        color = palette.subtext.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakCard(current: Int, longest: Int) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CURRENT STREAK",
                style = NyxLabel,
                color = palette.subtext,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = current.toString(),
                    style = NyxDisplay.copy(fontSize = 42.sp, lineHeight = 46.sp),
                    color = palette.text,
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = if (current == 1) "day" else "days",
                    style = NyxBody,
                    color = palette.subtext,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                text = if (current == 0) {
                    "Write today to begin a new streak."
                } else {
                    "Longest streak: $longest ${if (longest == 1) "day" else "days"}"
                },
                style = NyxCaption,
                color = palette.subtext,
            )
        }
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(accent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 15.dp, vertical = 14.dp),
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = NyxLabel,
            color = palette.subtext,
        )
        Text(
            text = value,
            style = NyxTitle.copy(fontSize = 24.sp, lineHeight = 29.sp),
            color = palette.text,
            modifier = Modifier.padding(top = 5.dp),
        )
        Text(
            text = hint,
            style = NyxCaption,
            color = palette.subtext,
        )
    }
}

@Composable
private fun WeeklyRhythmCard(stats: WritingStats) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val maxCount = max(1, stats.dayOfWeekCounts.maxOrNull() ?: 0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text(
            text = "Weekly rhythm",
            style = NyxHeadline,
            color = palette.text,
        )
        Text(
            text = "Notes created on each day of the week",
            style = NyxCaption,
            color = palette.subtext,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            stats.dayOfWeekCounts.forEachIndexed { index, count ->
                val barHeight = if (count == 0) {
                    4.dp
                } else {
                    (18f + (count.toFloat() / maxCount) * 72f).dp
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = count.toString(),
                        style = NyxCaption,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (count == 0) palette.border else accent.copy(alpha = 0.78f),
                            ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = DayOfWeek.of(index + 1)
                            .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = NyxCaption.copy(fontSize = 10.sp),
                        color = palette.subtext,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyRhythmCard(stats: WritingStats) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val maxCount = max(1, stats.monthlyCounts.maxOrNull() ?: 0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text(
            text = "Last six months",
            style = NyxHeadline,
            color = palette.text,
        )
        Text(
            text = "How much you wrote, month by month",
            style = NyxCaption,
            color = palette.subtext,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            stats.monthlyCounts.forEachIndexed { index, count ->
                val barHeight = if (count == 0) {
                    4.dp
                } else {
                    (18f + (count.toFloat() / maxCount) * 62f).dp
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = count.toString(),
                        style = NyxCaption,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (count == 0) palette.border else accent.copy(alpha = 0.62f),
                            ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stats.monthlyLabels.getOrElse(index) { "" },
                        style = NyxCaption.copy(fontSize = 10.sp),
                        color = palette.subtext,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakCalendarCard(history: List<Long>) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val writingDays = history.map { LocalDate.ofEpochDay(it) }.toSet()
    val today = LocalDate.now()
    val start = today.minusDays(89)
    val weeks = (0 until 13).map { week ->
        (0 until 7).map { day ->
            start.plusDays((week * 7 + day).toLong())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Last 90 days",
                style = NyxHeadline,
                color = palette.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${writingDays.size} writing ${if (writingDays.size == 1) "day" else "days"}",
                style = NyxCaption,
                color = palette.subtext,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { date ->
                        val active = writingDays.contains(date)
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (active) accent.copy(alpha = 0.82f) else palette.border,
                                ),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Less",
                style = NyxCaption.copy(fontSize = 10.sp),
                color = palette.subtext,
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.border),
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.copy(alpha = 0.35f)),
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.copy(alpha = 0.82f)),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "More",
                style = NyxCaption.copy(fontSize = 10.sp),
                color = palette.subtext,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Every square is a day you wrote.",
                style = NyxCaption.copy(fontSize = 10.sp),
                color = palette.subtext.copy(alpha = 0.8f),
            )
        }
    }
}

private fun formatNumber(value: Int): String =
    NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)
