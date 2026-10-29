package com.nyxpad.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.nyxpad.app.data.preferences.NyxSettings
import com.nyxpad.app.data.preferences.NyxTheme

@Immutable
data class NyxPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val card: Color,
    val cardPressed: Color,
    val border: Color,
    val divider: Color,
    val text: Color,
    val subtext: Color,
    val placeholder: Color,
    val glow: Color,
    val glowBlue: Color,
    val tagDefault: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
)

private val LuminaPalette = NyxPalette(
    background = NyxBackground,
    surface = NyxGlassScrim,
    surfaceVariant = NyxGlassSurface,
    card = NyxGlassSurface,
    cardPressed = NyxGlassSurfaceStrong,
    border = NyxGlassBorder,
    divider = NyxGlassDivider,
    text = NyxText,
    subtext = NyxSubtext,
    placeholder = NyxPlaceholder,
    glow = NyxGlow,
    glowBlue = NyxGlowBlue,
    tagDefault = NyxGlassSurface,
    glassBorder = NyxGlassBorder,
    glassHighlight = NyxGlassHighlight,
)

private val AmoledPalette = LuminaPalette.copy(
    background = AmoledBackground,
    surface = NyxGlassAmoledScrim,
    surfaceVariant = NyxGlassSurface,
    card = NyxGlassSurface,
    cardPressed = NyxGlassSurfaceStrong,
    border = NyxGlassBorder,
    divider = NyxGlassDivider,
    tagDefault = NyxGlassSurface,
)

private val MidnightPalette = LuminaPalette.copy(
    background = MidnightBackground,
    surface = NyxGlassMidnightScrim,
    surfaceVariant = NyxGlassSurface,
    card = NyxGlassSurface,
    cardPressed = NyxGlassSurfaceStrong,
    border = NyxGlassBorder,
    divider = NyxGlassDivider,
    tagDefault = NyxGlassSurface,
)

val LocalNyxPalette = staticCompositionLocalOf { LuminaPalette }
val LocalNyxSettings = staticCompositionLocalOf { NyxSettings() }
val LocalNyxAccent = staticCompositionLocalOf { NyxAccent }
val LocalNyxEditorFontFamily = staticCompositionLocalOf<FontFamily> { InterFontFamily }

@Composable
fun NyxPadTheme(
    settings: NyxSettings = NyxSettings(),
    content: @Composable () -> Unit,
) {
    val base = when (settings.theme) {
        NyxTheme.Lumina -> LuminaPalette
        NyxTheme.Amoled -> AmoledPalette
        NyxTheme.Midnight -> MidnightPalette
    }
    val duration = 400
    val background by animateColorAsState(base.background, tween(duration), label = "background")
    val surface by animateColorAsState(base.surface, tween(duration), label = "surface")
    val surfaceVariant by animateColorAsState(
        base.surfaceVariant,
        tween(duration),
        label = "surfaceVariant",
    )
    val card by animateColorAsState(base.card, tween(duration), label = "card")
    val cardPressed by animateColorAsState(base.cardPressed, tween(duration), label = "cardPressed")
    val border by animateColorAsState(base.border, tween(duration), label = "border")
    val divider by animateColorAsState(base.divider, tween(duration), label = "divider")
    val text by animateColorAsState(base.text, tween(duration), label = "text")
    val subtext by animateColorAsState(base.subtext, tween(duration), label = "subtext")
    val placeholder by animateColorAsState(base.placeholder, tween(duration), label = "placeholder")
    val glassBorder by animateColorAsState(
        base.glassBorder,
        tween(duration),
        label = "glassBorder",
    )
    val glassHighlight by animateColorAsState(
        base.glassHighlight,
        tween(duration),
        label = "glassHighlight",
    )
    val accent by animateColorAsState(accentFor(settings.accent), tween(duration), label = "accent")
    val palette = base.copy(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        card = card,
        cardPressed = cardPressed,
        border = border,
        divider = divider,
        text = text,
        subtext = subtext,
        placeholder = placeholder,
        glassBorder = glassBorder,
        glassHighlight = glassHighlight,
    )
    val colorScheme = darkColorScheme(
        primary = accent,
        onPrimary = NyxBackground,
        primaryContainer = accent.copy(alpha = 0.18f),
        onPrimaryContainer = accent,
        secondary = NyxAccentBlue,
        onSecondary = NyxBackground,
        secondaryContainer = NyxAccentBlue.copy(alpha = 0.16f),
        onSecondaryContainer = NyxAccentBlue,
        tertiary = accent,
        onTertiary = NyxBackground,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = subtext,
        outline = border,
        outlineVariant = divider,
        error = NyxError,
        onError = NyxBackground,
        errorContainer = NyxTagRed,
        onErrorContainer = NyxError,
        scrim = NyxScrim,
        inverseSurface = text,
        inverseOnSurface = background,
        inversePrimary = accent,
        surfaceTint = accent,
    )

    CompositionLocalProvider(
        LocalNyxPalette provides palette,
        LocalNyxSettings provides settings,
        LocalNyxAccent provides accent,
        LocalNyxEditorFontFamily provides fontFamilyFor(settings.fontStyle),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NyxTypography,
            content = content,
        )
    }
}
