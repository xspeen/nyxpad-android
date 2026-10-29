package com.nyxpad.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxLabel

@Composable
fun WordCountBadge(
    wordCount: Int,
    modifier: Modifier = Modifier,
) {
    val accent = LocalNyxAccent.current
    val palette = LocalNyxPalette.current
    Box(
        modifier = modifier
            .background(accent.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = if (wordCount == 1) "1 word" else "$wordCount words",
            style = NyxLabel,
            color = palette.subtext,
        )
    }
}

@Composable
fun WordCountRow(
    wordCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(1.dp))
        WordCountBadge(wordCount = wordCount)
    }
}
