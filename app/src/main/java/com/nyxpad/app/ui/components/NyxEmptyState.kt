package com.nyxpad.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBackground
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxTitle

@Composable
fun NyxEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val pageLeft = size.width * 0.23f
                val pageTop = size.height * 0.13f
                val pageWidth = size.width * 0.54f
                val pageHeight = size.height * 0.72f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(size.width * 0.52f, size.height * 0.48f),
                        radius = size.width * 0.42f,
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.52f, size.height * 0.48f),
                )

                drawRoundRect(
                    color = palette.subtext.copy(alpha = 0.55f),
                    topLeft = Offset(pageLeft, pageTop),
                    size = Size(pageWidth, pageHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
                drawRoundRect(
                    color = palette.subtext.copy(alpha = 0.15f),
                    topLeft = Offset(pageLeft, pageTop),
                    size = Size(pageWidth, pageHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )

                val lineStart = pageLeft + pageWidth * 0.16f
                val lineEnd = pageLeft + pageWidth * 0.84f
                repeat(4) { index ->
                    val y = pageTop + pageHeight * (0.27f + index * 0.13f)
                    drawLine(
                        color = palette.subtext.copy(alpha = 0.30f),
                        start = Offset(lineStart, y),
                        end = Offset(
                            if (index == 3) lineEnd - pageWidth * 0.28f else lineEnd,
                            y,
                        ),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }

                rotate(degrees = -38f, pivot = Offset(size.width * 0.70f, size.height * 0.64f)) {
                    val pen = Path().apply {
                        moveTo(size.width * 0.66f, size.height * 0.72f)
                        lineTo(size.width * 0.78f, size.height * 0.39f)
                        lineTo(size.width * 0.83f, size.height * 0.42f)
                        lineTo(size.width * 0.71f, size.height * 0.75f)
                        close()
                    }
                    drawPath(pen, color = accent.copy(alpha = 0.95f))
                    drawLine(
                        color = accent.copy(alpha = 0.45f),
                        start = Offset(size.width * 0.66f, size.height * 0.72f),
                        end = Offset(size.width * 0.71f, size.height * 0.75f),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = NyxTitle,
            color = palette.text,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = NyxBodySmall,
            color = palette.subtext,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = NyxBackground,
                ),
            ) {
                Text(text = actionLabel, style = NyxBody)
            }
        }
    }
}
