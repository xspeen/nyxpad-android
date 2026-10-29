package com.nyxpad.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nyxpad.app.R
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.NyxAccentBlue
import com.nyxpad.app.ui.theme.NyxBackground
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxSubtext

@Composable
fun SplashScreen(onFinished: () -> Unit = {}) {
    val progress = remember { Animatable(0f) }
    val accent = LocalNyxAccent.current
    val infinite = rememberInfiniteTransition(label = "splashGlow")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashGlowPulse",
    )

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxBackground),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(320.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = glowPulse),
                        NyxAccentBlue.copy(alpha = glowPulse * 0.45f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * 0.5f,
                ),
                radius = size.minDimension * 0.5f,
                center = center,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_nyxpad_logo),
                contentDescription = "NyxPad",
                modifier = Modifier
                    .width(216.dp)
                    .height(58.dp)
                    .alpha(progress.value)
                    .scale(0.94f + (progress.value * 0.06f)),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Write without limits",
                style = NyxCaption,
                color = NyxSubtext,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(progress.value),
            )
        }

        Text(
            text = "PRIVATE BY DESIGN",
            style = com.nyxpad.app.ui.theme.NyxLabel,
            color = NyxSubtext.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp),
        )
    }
}
