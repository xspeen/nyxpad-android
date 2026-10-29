package com.nyxpad.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxAccentBlue
import com.nyxpad.app.ui.theme.NyxAccentGold
import com.nyxpad.app.ui.theme.NyxAccentGreen
import com.nyxpad.app.ui.theme.NyxAccentPink
import com.nyxpad.app.ui.theme.NyxAccentTeal
import com.nyxpad.app.ui.theme.NyxBackground
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxDisplay
import com.nyxpad.app.ui.theme.NyxError
import com.nyxpad.app.ui.theme.NyxSubtext
import com.nyxpad.app.ui.theme.NyxTitle
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
)

private val slides = listOf(
    OnboardingSlide(
        title = "Write without limits",
        subtitle = "No word caps. No storage warnings. No restrictions. " +
            "Just you and your thoughts.",
    ),
    OnboardingSlide(
        title = "Yours and yours alone",
        subtitle = "Nothing leaves your phone. No servers. No accounts. " +
            "No one can see what you write.",
    ),
    OnboardingSlide(
        title = "Make it feel like you",
        subtitle = "Change themes, colors, fonts and layouts. " +
            "NyxPad adapts to how you think and write.",
    ),
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val isLast = pagerState.currentPage == slides.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        HorizontalPager(
            count = slides.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 128.dp),
            verticalAlignment = Alignment.Top,
        ) { page ->
            OnboardingPage(
                page = page,
                slide = slides[page],
                currentPage = pagerState.currentPage,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                slides.indices.forEach { index ->
                    val active = pagerState.currentPage == index
                    val width by animateFloatAsState(
                        targetValue = if (active) 24f else 8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "dotWidth",
                    )
                    Box(
                        modifier = Modifier
                            .width(width.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (active) accent else palette.subtext.copy(alpha = 0.55f)),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = isLast,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                },
                label = "onboardingButton",
            ) { last ->
                if (last) {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.complete(onFinished)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = NyxBackground,
                        ),
                    ) {
                        Text(text = "Start Writing", style = NyxBody)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        (pagerState.currentPage + 1).coerceAtMost(slides.lastIndex),
                                    )
                                }
                            },
                            modifier = Modifier
                                .width(154.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = NyxBackground,
                            ),
                        ) {
                            Text(text = "Continue", style = NyxBody)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    page: Int,
    slide: OnboardingSlide,
    currentPage: Int,
) {
    val palette = LocalNyxPalette.current
    val isCurrent = page == currentPage
    val iconAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0f,
        animationSpec = tween(350, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "onboardingIcon",
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0f,
        animationSpec = tween(350, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "onboardingTitle",
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0f,
        animationSpec = tween(350, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "onboardingSubtitle",
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .alpha(iconAlpha)
                    .graphicsLayer {
                        scaleX = 0.92f + iconAlpha * 0.08f
                        scaleY = 0.92f + iconAlpha * 0.08f
                        translationX = (1f - iconAlpha) * 30.dp.toPx()
                    },
            ) {
                when (page) {
                    0 -> InkDropIllustration()
                    1 -> LockShieldIllustration()
                    else -> PaletteIllustration()
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = slide.title,
                style = NyxDisplay,
                color = palette.text,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .graphicsLayer { translationX = (1f - titleAlpha) * 34.dp.toPx() },
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = slide.subtitle,
                style = NyxBodySmall.copy(lineHeight = NyxBody.lineHeight),
                color = palette.subtext,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .graphicsLayer { translationX = (1f - subtitleAlpha) * 34.dp.toPx() },
            )
        }
    }
}

@Composable
private fun InkDropIllustration() {
    val accent = LocalNyxAccent.current
    val infinite = rememberInfiniteTransition(label = "inkDrop")
    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "inkDropProgress",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pageWidth = size.width * 0.58f
        val pageHeight = size.height * 0.68f
        val left = (size.width - pageWidth) / 2f
        val top = size.height * 0.17f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.15f), Color.Transparent),
                center = center,
                radius = size.width * 0.45f,
            ),
            radius = size.width * 0.45f,
            center = center,
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.08f),
            topLeft = Offset(left, top),
            size = Size(pageWidth, pageHeight),
            cornerRadius = CornerRadius(16.dp.toPx()),
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.38f + progress * 0.35f),
            topLeft = Offset(left, top),
            size = Size(pageWidth, pageHeight),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        val lineStart = left + pageWidth * 0.16f
        repeat(5) { index ->
            drawLine(
                color = accent.copy(alpha = 0.16f + progress * 0.12f),
                start = Offset(lineStart, top + pageHeight * (0.28f + index * 0.11f)),
                end = Offset(
                    left + pageWidth * if (index == 4) 0.60f else 0.84f,
                    top + pageHeight * (0.28f + index * 0.11f),
                ),
                strokeWidth = 1.3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        val dropX = size.width * 0.5f
        val dropY = top - 22.dp.toPx() + progress * 26.dp.toPx()
        val radius = (13.dp.toPx() * (1f - progress * 0.72f)).coerceAtLeast(2.dp.toPx())
        drawCircle(
            color = accent.copy(alpha = 0.22f),
            radius = radius * 2.4f,
            center = Offset(dropX, dropY),
        )
        drawCircle(color = accent, radius = radius, center = Offset(dropX, dropY))
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius * 0.28f,
            center = Offset(dropX - radius * 0.25f, dropY - radius * 0.25f),
        )
    }
}

@Composable
private fun LockShieldIllustration() {
    val accent = LocalNyxAccent.current
    val blue = NyxAccentBlue
    val infinite = rememberInfiniteTransition(label = "lockShield")
    val pulse by infinite.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lockGlow",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.49f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = pulse), Color.Transparent),
                center = Offset(cx, cy),
                radius = size.width * 0.43f,
            ),
            radius = size.width * 0.43f,
            center = Offset(cx, cy),
        )
        val shield = Path().apply {
            moveTo(cx, cy - size.height * 0.28f)
            lineTo(cx + size.width * 0.24f, cy - size.height * 0.18f)
            lineTo(cx + size.width * 0.20f, cy + size.height * 0.10f)
            quadraticBezierTo(
                cx + size.width * 0.12f,
                cy + size.height * 0.25f,
                cx,
                cy + size.height * 0.31f,
            )
            quadraticBezierTo(
                cx - size.width * 0.12f,
                cy + size.height * 0.25f,
                cx - size.width * 0.20f,
                cy + size.height * 0.10f,
            )
            lineTo(cx - size.width * 0.24f, cy - size.height * 0.18f)
            close()
        }
        drawPath(shield, color = accent.copy(alpha = 0.08f))
        drawPath(
            shield,
            color = accent.copy(alpha = 0.55f + pulse * 0.2f),
            style = Stroke(width = 1.6.dp.toPx()),
        )
        val bodyWidth = size.width * 0.25f
        val bodyHeight = size.height * 0.20f
        val bodyTop = cy - size.height * 0.02f
        drawRoundRect(
            color = blue.copy(alpha = 0.20f),
            topLeft = Offset(cx - bodyWidth / 2f, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(9.dp.toPx()),
        )
        drawRoundRect(
            color = blue,
            topLeft = Offset(cx - bodyWidth / 2f, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(9.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawArc(
            color = blue,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - bodyWidth * 0.33f, bodyTop - bodyHeight * 0.62f),
            size = Size(bodyWidth * 0.66f, bodyHeight * 1.10f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(color = accent, radius = 3.4.dp.toPx(), center = Offset(cx, bodyTop + bodyHeight * 0.48f))
        drawLine(
            color = accent,
            start = Offset(cx, bodyTop + bodyHeight * 0.52f),
            end = Offset(cx, bodyTop + bodyHeight * 0.72f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PaletteIllustration() {
    val accent = LocalNyxAccent.current
    val palette = LocalNyxPalette.current
    val infinite = rememberInfiniteTransition(label = "paletteOrbs")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "palettePhase",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                center = center,
                radius = size.width * 0.46f,
            ),
            radius = size.width * 0.46f,
            center = center,
        )
        val board = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - size.width * 0.29f,
                    top = cy - size.height * 0.23f,
                    right = cx + size.width * 0.29f,
                    bottom = cy + size.height * 0.23f,
                    radiusX = size.width * 0.12f,
                    radiusY = size.height * 0.12f,
                ),
            )
        }
        drawPath(board, color = accent.copy(alpha = 0.09f))
        drawPath(
            board,
            color = accent.copy(alpha = 0.55f),
            style = Stroke(width = 1.6.dp.toPx()),
        )
        val colors = listOf(
            accent,
            NyxAccentBlue,
            NyxAccentTeal,
            NyxAccentGreen,
            NyxAccentPink,
            NyxAccentGold,
        )
        colors.forEachIndexed { index, color ->
            val angle = phase + index * (6.28318f / colors.size)
            val orbit = size.width * (0.17f + (index % 2) * 0.035f)
            val x = cx + cos(angle) * orbit
            val y = cy + sin(angle) * orbit * 0.72f
            drawCircle(color = color.copy(alpha = 0.16f), radius = 18.dp.toPx(), center = Offset(x, y))
            drawCircle(color = color, radius = 8.dp.toPx(), center = Offset(x, y))
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = 2.4.dp.toPx(),
                center = Offset(x - 2.dp.toPx(), y - 2.dp.toPx()),
            )
        }
        drawCircle(
            color = palette.background,
            radius = size.width * 0.055f,
            center = Offset(cx + size.width * 0.14f, cy + size.height * 0.13f),
        )
        drawCircle(
            color = accent.copy(alpha = 0.5f),
            radius = size.width * 0.055f,
            center = Offset(cx + size.width * 0.14f, cy + size.height * 0.13f),
            style = Stroke(width = 1.4.dp.toPx()),
        )
    }
}
