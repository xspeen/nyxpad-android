package com.nyxpad.app.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val editorFormatter = DateTimeFormatter.ofPattern(
        "EEE, d MMM yyyy · HH:mm",
        Locale.getDefault(),
    )
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun formatEditorDate(epochMillis: Long): String =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
            .format(editorFormatter)

    fun formatTime(epochMillis: Long): String =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
            .format(clockFormatter)

    fun formatCardDate(epochMillis: Long): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(epochMillis),
            ZoneId.systemDefault(),
        )
        val date = dateTime.toLocalDate()
        val today = LocalDate.now()
        return when {
            date == today -> "Today · ${dateTime.format(timeFormatter)}"
            date == today.minusDays(1) -> "Yesterday"
            date.year == today.year -> date.format(DateTimeFormatter.ofPattern("d MMM"))
            else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        }
    }

    fun formatMonth(month: YearMonth): String = month.format(monthFormatter)

    fun lastSixMonths(today: LocalDate = LocalDate.now()): List<YearMonth> =
        (5 downTo 0).map { today.minusMonths(it.toLong()).let(YearMonth::from) }

    fun purgeLabel(deletedAt: Long?, purgeDays: Int): String {
        if (purgeDays == PreferencesKeys.NEVER_PURGE) return "Never expires"
        if (deletedAt == null) return "Pending"
        val dayMillis = TimeUnit.DAYS.toMillis(1)
        val expiry = deletedAt + TimeUnit.DAYS.toMillis(purgeDays.toLong())
        val remaining = expiry - System.currentTimeMillis()
        if (remaining <= 0L) return "Deleting soon"
        val days = ((remaining + dayMillis - 1L) / dayMillis).coerceAtLeast(1L)
        return if (days == 1L) "Deletes in 1 day" else "Deletes in $days days"
    }

    fun epochDayToDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun shortWeekday(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
}
