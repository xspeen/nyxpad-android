package com.nyxpad.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxpad.app.R
import com.nyxpad.app.data.preferences.ViewMode
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxLabel

@Composable
fun NyxTopBar(
    streak: Int,
    viewMode: ViewMode,
    searchActive: Boolean,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleView: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(palette.background)
            .drawBehind {
                drawLine(
                    color = palette.border,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Open navigation drawer",
                tint = palette.text,
                modifier = Modifier.size(23.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_nyxpad_logo),
                contentDescription = "NyxPad",
                modifier = Modifier
                    .width(88.dp)
                    .height(23.dp),
            )
            if (streak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = if (streak == 1) "1 day streak" else "$streak day streak",
                        style = NyxLabel.copy(fontSize = 10.sp),
                        color = accent.copy(alpha = 0.85f),
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search notes",
                    tint = if (searchActive) accent else palette.subtext,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = onToggleView,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.Grid) {
                        Icons.Outlined.ViewAgenda
                    } else {
                        Icons.Outlined.GridView
                    },
                    contentDescription = if (viewMode == ViewMode.Grid) {
                        "Switch to list view"
                    } else {
                        "Switch to grid view"
                    },
                    tint = palette.subtext,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = onSortClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sort,
                    contentDescription = "Sort notes",
                    tint = palette.subtext,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
