package com.nyxpad.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxLabel
import com.nyxpad.app.ui.theme.moodColor

@Composable
fun WritingMoodBadge(
    mood: WritingMood,
    modifier: Modifier = Modifier,
    showNone: Boolean = false,
) {
    if (mood == WritingMood.None && !showNone) return
    val palette = LocalNyxPalette.current
    val color = moodColor(mood)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.09f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = mood.label,
            style = NyxLabel,
            color = if (mood == WritingMood.None) palette.subtext else color,
        )
    }
}
