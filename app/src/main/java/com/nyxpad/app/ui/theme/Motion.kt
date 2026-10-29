package com.nyxpad.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

val NyxSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium,
)

val NyxLayoutSpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

val NyxFastOutSlowIn = FastOutSlowInEasing

val NyxSheetEnter = slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(320, easing = FastOutSlowInEasing),
) + fadeIn(tween(180))

val NyxSheetExit = slideOutVertically(
    targetOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(250, easing = FastOutSlowInEasing),
) + fadeOut(tween(160))
