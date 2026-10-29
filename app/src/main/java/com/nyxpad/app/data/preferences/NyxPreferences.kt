package com.nyxpad.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nyxpad.app.utils.PreferencesKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.nyxDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PreferencesKeys.DATASTORE_NAME,
)

enum class NyxTheme(val storageKey: String, val label: String) {
    Lumina("lumina", "Lumina Dark"),
    Amoled("amoled", "Pure AMOLED"),
    Midnight("midnight", "Midnight Blue");

    companion object {
        fun fromKey(key: String?): NyxTheme = entries.firstOrNull { it.storageKey == key } ?: Lumina
    }
}

enum class AccentChoice(val storageKey: String, val label: String, val argb: Long) {
    Purple("purple", "Purple", 0xFF9D6FFF),
    Blue("blue", "Blue", 0xFF4FC3F7),
    Teal("teal", "Teal", 0xFF26C6A6),
    Green("green", "Green", 0xFF4CAF82),
    Pink("pink", "Pink", 0xFFFF6B9D),
    Red("red", "Red", 0xFFFF6B6B),
    Gold("gold", "Gold", 0xFFFFB347),
    White("white", "White", 0xFFE8E8F0);

    companion object {
        fun fromKey(key: String?): AccentChoice =
            entries.firstOrNull { it.storageKey == key } ?: Purple
    }
}

enum class NyxFontStyle(val storageKey: String, val label: String) {
    Inter("inter", "Inter"),
    Mono("mono", "JetBrains Mono"),
    System("system", "System");

    companion object {
        fun fromKey(key: String?): NyxFontStyle =
            entries.firstOrNull { it.storageKey == key } ?: Inter
    }
}

enum class ViewMode(val storageKey: String) {
    Grid("grid"),
    List("list");

    companion object {
        fun fromKey(key: String?): ViewMode =
            entries.firstOrNull { it.storageKey == key } ?: Grid
    }
}

enum class SortOption(val storageKey: String, val label: String) {
    LastEdited("last_edited", "Last edited"),
    DateCreated("date_created", "Date created"),
    TitleAZ("title_az", "Title A–Z"),
    TitleZA("title_za", "Title Z–A");

    companion object {
        fun fromKey(key: String?): SortOption =
            entries.firstOrNull { it.storageKey == key } ?: LastEdited
    }
}

data class NyxSettings(
    val theme: NyxTheme = NyxTheme.Lumina,
    val accent: AccentChoice = AccentChoice.Purple,
    val fontSize: Int = PreferencesKeys.DEFAULT_FONT_SIZE,
    val fontStyle: NyxFontStyle = NyxFontStyle.Inter,
    val autoSave: Boolean = true,
    val lineSpacing: Float = PreferencesKeys.DEFAULT_LINE_SPACING,
    val showWordCount: Boolean = true,
    val focusModeDefault: Boolean = false,
    val appLock: Boolean = false,
    val noteLock: Boolean = false,
    val autoPurgeDays: Int = PreferencesKeys.DEFAULT_PURGE_DAYS,
    val viewMode: ViewMode = ViewMode.Grid,
    val sortOption: SortOption = SortOption.LastEdited,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val streakHistory: List<Long> = emptyList(),
    val lastWriteEpochDay: Long = -1L,
) {
    val autoPurgeLabel: String
        get() = if (autoPurgeDays == PreferencesKeys.NEVER_PURGE) "Never" else "$autoPurgeDays days"
}

@Singleton
class NyxPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.nyxDataStore

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .distinctUntilChanged()

    val onboardingComplete: Flow<Boolean> = preferences
        .map { it[Keys.OnboardingComplete] ?: false }
        .distinctUntilChanged()

    val settingsFlow: Flow<NyxSettings> = preferences
        .map(::toSettings)
        .distinctUntilChanged()

    val currentStreak: Flow<Int> = settingsFlow
        .map { it.currentStreak }
        .distinctUntilChanged()

    val longestStreak: Flow<Int> = settingsFlow
        .map { it.longestStreak }
        .distinctUntilChanged()

    val streakHistory: Flow<List<Long>> = settingsFlow
        .map { it.streakHistory }
        .distinctUntilChanged()

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.OnboardingComplete] = complete }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { it[Keys.ViewMode] = mode.storageKey }
    }

    suspend fun setSortOption(option: SortOption) {
        dataStore.edit { it[Keys.SortOption] = option.storageKey }
    }

    suspend fun setTheme(theme: NyxTheme) {
        dataStore.edit { it[Keys.Theme] = theme.storageKey }
    }

    suspend fun setAccent(accent: AccentChoice) {
        dataStore.edit { it[Keys.Accent] = accent.storageKey }
    }

    suspend fun setFontSize(size: Int) {
        dataStore.edit {
            it[Keys.FontSize] = size.coerceIn(
                PreferencesKeys.MIN_FONT_SIZE,
                PreferencesKeys.MAX_FONT_SIZE,
            )
        }
    }

    suspend fun setFontStyle(style: NyxFontStyle) {
        dataStore.edit { it[Keys.FontStyle] = style.storageKey }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { it[Keys.AutoSave] = enabled }
    }

    suspend fun setLineSpacing(spacing: Float) {
        dataStore.edit {
            it[Keys.LineSpacing] = spacing.coerceIn(
                PreferencesKeys.MIN_LINE_SPACING,
                PreferencesKeys.MAX_LINE_SPACING,
            )
        }
    }

    suspend fun setShowWordCount(enabled: Boolean) {
        dataStore.edit { it[Keys.ShowWordCount] = enabled }
    }

    suspend fun setFocusModeDefault(enabled: Boolean) {
        dataStore.edit { it[Keys.FocusModeDefault] = enabled }
    }

    suspend fun setAppLock(enabled: Boolean) {
        dataStore.edit { it[Keys.AppLock] = enabled }
    }

    suspend fun setNoteLock(enabled: Boolean) {
        dataStore.edit { it[Keys.NoteLock] = enabled }
    }

    suspend fun setAutoPurgeDays(days: Int) {
        val safe = if (days == PreferencesKeys.NEVER_PURGE) days else days.coerceIn(1, 365)
        dataStore.edit { it[Keys.AutoPurgeDays] = safe }
    }

    /**
     * Records one writing day. Calling this repeatedly on the same calendar day
     * is intentionally a no-op, so typing hundreds of words still advances the
     * streak exactly once.
     */
    suspend fun recordWritingDay(today: LocalDate = LocalDate.now()) {
        dataStore.edit { prefs ->
            val todayEpochDay = today.toEpochDay()
            val lastWrite = prefs[Keys.LastWriteDate] ?: -1L
            if (lastWrite == todayEpochDay) return@edit

            val previousStreak = prefs[Keys.StreakCount] ?: 0
            val newStreak = if (lastWrite == todayEpochDay - 1L) {
                previousStreak.coerceAtLeast(0) + 1
            } else {
                1
            }
            val previousLongest = prefs[Keys.LongestStreak] ?: 0
            val history = parseHistory(prefs[Keys.StreakHistory])
                .plus(todayEpochDay)
                .distinct()
                .filter { it >= todayEpochDay - 90L }
                .sorted()
                .takeLast(90)

            prefs[Keys.LastWriteDate] = todayEpochDay
            prefs[Keys.StreakCount] = newStreak
            prefs[Keys.LongestStreak] = maxOf(previousLongest, newStreak)
            prefs[Keys.StreakHistory] = history.joinToString(",")
        }
    }

    suspend fun getSettingsSnapshot(): NyxSettings = toSettings(
        dataStore.data
            .catch { throwable ->
                if (throwable is IOException) emit(emptyPreferences()) else throw throwable
            }
            .first(),
    )

    private fun toSettings(prefs: Preferences): NyxSettings {
        val today = LocalDate.now().toEpochDay()
        val lastWrite = prefs[Keys.LastWriteDate] ?: -1L
        val storedStreak = prefs[Keys.StreakCount] ?: 0
        val activeStreak = when {
            lastWrite == today || lastWrite == today - 1L -> storedStreak
            else -> 0
        }
        return NyxSettings(
            theme = NyxTheme.fromKey(prefs[Keys.Theme]),
            accent = AccentChoice.fromKey(prefs[Keys.Accent]),
            fontSize = (prefs[Keys.FontSize] ?: PreferencesKeys.DEFAULT_FONT_SIZE)
                .coerceIn(PreferencesKeys.MIN_FONT_SIZE, PreferencesKeys.MAX_FONT_SIZE),
            fontStyle = NyxFontStyle.fromKey(prefs[Keys.FontStyle]),
            autoSave = prefs[Keys.AutoSave] ?: true,
            lineSpacing = (prefs[Keys.LineSpacing] ?: PreferencesKeys.DEFAULT_LINE_SPACING)
                .coerceIn(PreferencesKeys.MIN_LINE_SPACING, PreferencesKeys.MAX_LINE_SPACING),
            showWordCount = prefs[Keys.ShowWordCount] ?: true,
            focusModeDefault = prefs[Keys.FocusModeDefault] ?: false,
            appLock = prefs[Keys.AppLock] ?: false,
            noteLock = prefs[Keys.NoteLock] ?: false,
            autoPurgeDays = prefs[Keys.AutoPurgeDays] ?: PreferencesKeys.DEFAULT_PURGE_DAYS,
            viewMode = ViewMode.fromKey(prefs[Keys.ViewMode]),
            sortOption = SortOption.fromKey(prefs[Keys.SortOption]),
            currentStreak = activeStreak,
            longestStreak = prefs[Keys.LongestStreak] ?: 0,
            streakHistory = parseHistory(prefs[Keys.StreakHistory]),
            lastWriteEpochDay = lastWrite,
        )
    }

    private fun parseHistory(raw: String?): List<Long> = raw
        ?.split(',')
        ?.mapNotNull { it.trim().toLongOrNull() }
        ?.sorted()
        ?.takeLast(90)
        .orEmpty()

    private object Keys {
        val OnboardingComplete = booleanPreferencesKey(PreferencesKeys.ONBOARDING_COMPLETE)
        val ViewMode = stringPreferencesKey(PreferencesKeys.VIEW_MODE)
        val SortOption = stringPreferencesKey(PreferencesKeys.SORT_OPTION)
        val Theme = stringPreferencesKey(PreferencesKeys.THEME)
        val Accent = stringPreferencesKey(PreferencesKeys.ACCENT)
        val FontSize = intPreferencesKey(PreferencesKeys.FONT_SIZE)
        val FontStyle = stringPreferencesKey(PreferencesKeys.FONT_STYLE)
        val AutoSave = booleanPreferencesKey(PreferencesKeys.AUTO_SAVE)
        val LineSpacing = floatPreferencesKey(PreferencesKeys.LINE_SPACING)
        val ShowWordCount = booleanPreferencesKey(PreferencesKeys.SHOW_WORD_COUNT)
        val FocusModeDefault = booleanPreferencesKey(PreferencesKeys.FOCUS_MODE_DEFAULT)
        val AppLock = booleanPreferencesKey(PreferencesKeys.APP_LOCK)
        val NoteLock = booleanPreferencesKey(PreferencesKeys.NOTE_LOCK)
        val AutoPurgeDays = intPreferencesKey(PreferencesKeys.AUTO_PURGE_DAYS)
        val LastWriteDate = longPreferencesKey(PreferencesKeys.LAST_WRITE_DATE)
        val StreakCount = intPreferencesKey(PreferencesKeys.STREAK_COUNT)
        val LongestStreak = intPreferencesKey(PreferencesKeys.LONGEST_STREAK)
        val StreakHistory = stringPreferencesKey(PreferencesKeys.STREAK_HISTORY)
    }
}
