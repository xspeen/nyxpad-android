package com.nyxpad.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.nyxpad.app.ui.theme.LocalNyxAccent
import kotlin.math.roundToInt

@Composable
fun PullRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val accent = LocalNyxAccent.current
    val maxPull = with(density) { 60.dp.toPx() }
    val threshold = with(density) { 44.dp.toPx() }
    val indicatorSize = with(density) { 34.dp.toPx() }
    val currentRefreshing by rememberUpdatedState(refreshing)
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    var pullOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    suspend fun animatePull(target: Float) {
        animate(
            initialValue = pullOffset,
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) { value, _ ->
            pullOffset = value
        }
    }

    val connection = remember(maxPull, threshold) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && available.y < 0f && pullOffset > 0f) {
                    val updated = (pullOffset + available.y).coerceAtLeast(0f)
                    val consumed = updated - pullOffset
                    pullOffset = updated
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.Drag &&
                    available.y > 0f &&
                    !currentRefreshing
                ) {
                    pullOffset = (pullOffset + available.y * 0.5f).coerceIn(0f, maxPull)
                    isDragging = true
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!isDragging) return Velocity.Zero
                isDragging = false
                if (pullOffset >= threshold) {
                    currentOnRefresh()
                    animatePull(threshold)
                } else {
                    animatePull(0f)
                }
                return available
            }
        }
    }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            animatePull(threshold)
        } else if (!isDragging) {
            animatePull(0f)
        }
    }

    val infinite = rememberInfiniteTransition(label = "refreshSpin")
    val spinning by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(820, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "refreshSpinValue",
    )
    val progress = (pullOffset / threshold).coerceIn(0f, 1f)
    val rotation = if (refreshing) spinning else progress * 270f
    val indicatorAlpha = if (refreshing) 1f else progress

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullOffset.roundToInt()) },
        ) {
            content()
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        0,
                        (pullOffset - indicatorSize - with(density) { 12.dp.toPx() })
                            .roundToInt(),
                    )
                }
                .size(34.dp)
                .then(
                    if (indicatorAlpha > 0.01f) Modifier else Modifier,
                ),
        ) {
            if (indicatorAlpha <= 0.01f) return@Canvas
            val strokeWidth = 3.dp.toPx()
            drawCircle(
                color = accent.copy(alpha = 0.10f * indicatorAlpha),
                radius = (size.minDimension - strokeWidth) / 2f,
                style = Stroke(width = strokeWidth),
            )
            drawArc(
                color = accent.copy(alpha = 0.18f * indicatorAlpha),
                startAngle = rotation - 90f,
                sweepAngle = 285f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth * 2.4f, cap = StrokeCap.Round),
            )
            drawArc(
                color = accent.copy(alpha = 0.95f * indicatorAlpha),
                startAngle = rotation - 90f,
                sweepAngle = 285f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}
