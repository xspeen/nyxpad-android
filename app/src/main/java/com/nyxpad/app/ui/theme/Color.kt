package com.nyxpad.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.nyxpad.app.data.preferences.AccentChoice
import com.nyxpad.app.domain.model.NoteColorTag
import com.nyxpad.app.domain.model.WritingMood

// Lumina Dark — the canonical NyxPad palette.
val NyxBackground = Color(0xFF050508)
val NyxSurface = Color(0xFF0D0D14)
val NyxSurfaceVariant = Color(0xFF111118)
val NyxCard = Color(0xFF13131C)
val NyxCardPressed = Color(0xFF1A1A26)
val NyxBorder = Color(0xFF1E1E2E)
val NyxDivider = Color(0xFF16161F)
val NyxAccent = Color(0xFF9D6FFF)
val NyxAccentDim = Color(0xFF6B47CC)
val NyxAccentBlue = Color(0xFF4FC3F7)
val NyxGlow = Color(0xFF7B5EA7)
val NyxGlowBlue = Color(0xFF2A6B8A)
val NyxText = Color(0xFFE8E8F0)
val NyxSubtext = Color(0xFF7A7A9A)
val NyxPlaceholder = Color(0xFF3A3A5A)
val NyxSuccess = Color(0xFF4CAF82)
val NyxError = Color(0xFFFF6B6B)
val NyxWarning = Color(0xFFFFB347)
val NyxScrim = Color(0xFF000000)
val NyxWhite = Color(0xFFFFFFFF)
val NyxBlack = Color(0xFF000000)
val NyxPureAmoled = Color(0xFF000000)
val NyxMidnight = Color(0xFF050A14)

// Note color tags.
val NyxTagDefault = Color(0xFF13131C)
val NyxTagPurple = Color(0xFF1E1530)
val NyxTagBlue = Color(0xFF0F1E2E)
val NyxTagGreen = Color(0xFF0F2018)
val NyxTagRed = Color(0xFF2E0F0F)
val NyxTagGold = Color(0xFF2E2410)

// Accent swatches from Settings.
val NyxAccentPurple = NyxAccent
val NyxAccentBlueSwatch = NyxAccentBlue
val NyxAccentTeal = Color(0xFF26C6A6)
val NyxAccentGreen = NyxSuccess
val NyxAccentPink = Color(0xFFFF6B9D)
val NyxAccentRed = NyxError
val NyxAccentGold = NyxWarning
val NyxAccentWhite = NyxText

val MoodFocused = NyxSubtext
val MoodCreative = NyxAccent
val MoodRandom = NyxAccentBlue
val MoodUrgent = NyxError
val MoodPersonal = NyxWarning

fun accentFor(choice: AccentChoice): Color = when (choice) {
    AccentChoice.Purple -> NyxAccentPurple
    AccentChoice.Blue -> NyxAccentBlueSwatch
    AccentChoice.Teal -> NyxAccentTeal
    AccentChoice.Green -> NyxAccentGreen
    AccentChoice.Pink -> NyxAccentPink
    AccentChoice.Red -> NyxAccentRed
    AccentChoice.Gold -> NyxAccentGold
    AccentChoice.White -> NyxAccentWhite
}

fun tagColor(tag: NoteColorTag): Color = when (tag) {
    NoteColorTag.Default -> NyxTagDefault
    NoteColorTag.Purple -> NyxTagPurple
    NoteColorTag.Blue -> NyxTagBlue
    NoteColorTag.Green -> NyxTagGreen
    NoteColorTag.Red -> NyxTagRed
    NoteColorTag.Gold -> NyxTagGold
}

fun moodColor(mood: WritingMood): Color = when (mood) {
    WritingMood.None -> NyxSubtext
    WritingMood.Focused -> MoodFocused
    WritingMood.Creative -> MoodCreative
    WritingMood.Random -> MoodRandom
    WritingMood.Urgent -> MoodUrgent
    WritingMood.Personal -> MoodPersonal
}

// Theme variants.
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF08080A)
val AmoledSurfaceVariant = Color(0xFF0D0D10)
val AmoledCard = Color(0xFF101014)
val AmoledCardPressed = Color(0xFF17171D)
val AmoledBorder = Color(0xFF1C1C22)
val AmoledDivider = Color(0xFF141418)

val MidnightBackground = Color(0xFF050A14)
val MidnightSurface = Color(0xFF0A1120)
val MidnightSurfaceVariant = Color(0xFF0D1628)
val MidnightCard = Color(0xFF101A2E)
val MidnightCardPressed = Color(0xFF16223A)
val MidnightBorder = Color(0xFF1B2942)
val MidnightDivider = Color(0xFF132038)
