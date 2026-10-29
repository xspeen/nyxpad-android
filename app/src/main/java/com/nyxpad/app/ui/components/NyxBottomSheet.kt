package com.nyxpad.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxScrim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NyxBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalNyxPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.text,
        scrimColor = NyxScrim.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(palette.border, CircleShape),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
            content = content,
        )
    }
}

@Composable
fun SheetHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = com.nyxpad.app.ui.theme.NyxHeadline,
            color = palette.text,
        )
        if (subtitle != null) {
            androidx.compose.material3.Text(
                text = subtitle,
                style = com.nyxpad.app.ui.theme.NyxCaption,
                color = palette.subtext,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}
