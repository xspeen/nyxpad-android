package com.nyxpad.app.domain.model

data class NoteTemplate(
    val id: Long = 0L,
    val name: String = "",
    val titleTemplate: String = "",
    val contentTemplate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

data class WritingStats(
    val totalNotes: Int = 0,
    val totalWords: Int = 0,
    val averageWords: Int = 0,
    val dayOfWeekCounts: List<Int> = List(7) { 0 },
    val monthlyCounts: List<Int> = List(6) { 0 },
    val monthlyLabels: List<String> = List(6) { "" },
    val mostProductiveDay: String = "—",
    val longestStreak: Int = 0,
    val currentStreak: Int = 0,
)
