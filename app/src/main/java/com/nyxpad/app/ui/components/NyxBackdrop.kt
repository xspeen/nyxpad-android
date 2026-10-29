package com.nyxpad.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.nyxpad.app.data.preferences.BackgroundStyle
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxEmberDeep
import com.nyxpad.app.ui.theme.NyxEmberGlow
import com.nyxpad.app.ui.theme.NyxGraphiteDeep
import com.nyxpad.app.ui.theme.NyxGraphiteGlow
import com.nyxpad.app.ui.theme.NyxNebulaDeep
import com.nyxpad.app.ui.theme.NyxNebulaGlow
import com.nyxpad.app.ui.theme.NyxOceanDeep
import com.nyxpad.app.ui.theme.NyxOceanGlow
import com.nyxpad.app.ui.theme.LocalNyxSettings
import com.nyxpad.app.utils.BackgroundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The glass stage behind every screen. Presets are pure gradients, so they cost
 * nothing to render; a user photo is decoded once and dimmed behind a scrim.
 */
@Composable
fun NyxBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val settings = LocalNyxSettings.current
    val image by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = settings.customBackgroundPath,
    ) {
        value = if (settings.backgroundStyle == BackgroundStyle.Custom) {
            withContext(Dispatchers.IO) {
                BackgroundManager.load(settings.customBackgroundPath)?.asImageBitmap()
            }
        } else {
            null
        }
    }
    val (glowA, glowB) = when (settings.backgroundStyle) {
        BackgroundStyle.Aurora -> palette.glow to palette.glowBlue
        BackgroundStyle.Nebula -> NyxNebulaGlow to NyxNebulaDeep
        BackgroundStyle.Ember -> NyxEmberGlow to NyxEmberDeep
        BackgroundStyle.Ocean -> NyxOceanGlow to NyxOceanDeep
        BackgroundStyle.Graphite -> NyxGraphiteGlow to NyxGraphiteDeep
        BackgroundStyle.Custom -> accent.copy(alpha = 0.7f) to palette.glowBlue
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.42f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.08f),
                    radius = size.minDimension * 0.85f,
                ),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.88f, size.height * 0.08f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowB.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(size.width * 0.05f, size.height * 0.72f),
                    radius = size.minDimension * 0.95f,
                ),
                radius = size.minDimension * 0.95f,
                center = Offset(size.width * 0.05f, size.height * 0.72f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.45f, size.height * 0.48f),
                    radius = size.minDimension * 0.75f,
                ),
                radius = size.minDimension * 0.75f,
                center = Offset(size.width * 0.45f, size.height * 0.48f),
            )
        }

        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.38f,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.background.copy(alpha = 0.18f),
                            palette.background.copy(alpha = 0.68f),
                            palette.background.copy(alpha = 0.90f),
                        ),
                    ),
                ),
        )

        content()
    }
}
