package com.nyxpad.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nyxpad.app.R
import com.nyxpad.app.data.preferences.NyxFontStyle

val InterFontFamily = FontFamily(
    Font(R.font.nyx_inter_regular, FontWeight.Normal),
    Font(R.font.nyx_inter_medium, FontWeight.Medium),
    Font(R.font.nyx_inter_semibold, FontWeight.SemiBold),
    Font(R.font.nyx_inter_bold, FontWeight.Bold),
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.nyx_mono_regular, FontWeight.Normal),
)

val SystemFontFamily = FontFamily.Default

fun fontFamilyFor(style: NyxFontStyle): FontFamily = when (style) {
    NyxFontStyle.Inter -> InterFontFamily
    NyxFontStyle.Mono -> JetBrainsMonoFontFamily
    NyxFontStyle.System -> SystemFontFamily
}

val NyxDisplay = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 39.sp,
    letterSpacing = (-0.5).sp,
)

val NyxTitle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.3).sp,
)

val NyxHeadline = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
)

val NyxBody = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

val NyxBodySmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val NyxCaption = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val NyxMono = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 23.sp,
)

val NyxLabel = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp,
)

val NyxTypography = Typography(
    displayLarge = NyxDisplay,
    displayMedium = NyxDisplay.copy(fontSize = 28.sp),
    displaySmall = NyxDisplay.copy(fontSize = 24.sp),
    headlineLarge = NyxTitle,
    headlineMedium = NyxHeadline,
    headlineSmall = NyxHeadline.copy(fontSize = 16.sp),
    titleLarge = NyxTitle,
    titleMedium = NyxHeadline,
    titleSmall = NyxHeadline.copy(fontSize = 15.sp),
    bodyLarge = NyxBody,
    bodyMedium = NyxBodySmall,
    bodySmall = NyxCaption,
    labelLarge = NyxLabel.copy(fontSize = 13.sp, letterSpacing = 0.2.sp),
    labelMedium = NyxLabel,
    labelSmall = NyxLabel.copy(fontSize = 10.sp),
)
