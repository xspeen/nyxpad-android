package com.nyxpad.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nyxpad.app.BuildConfig
import com.nyxpad.app.R
import com.nyxpad.app.ui.navigation.Screen
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxDivider
import com.nyxpad.app.ui.theme.NyxScrim
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class DrawerEntry(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun NyxDrawer(
    open: Boolean,
    currentRoute: String?,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    onScanQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val drawerWidth = 280.dp
    val drawerWidthPx = with(density) { drawerWidth.toPx() }
    val offsetX = remember(drawerWidthPx) { Animatable(-drawerWidthPx) }
    var edgeDrag by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(open) {
        offsetX.animateTo(
            targetValue = if (open) 0f else -drawerWidthPx,
            animationSpec = tween(
                durationMillis = if (open) 280 else 240,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    BackHandler(enabled = open) { onClose() }

    val scrimAlpha = ((1f + offsetX.value / drawerWidthPx).coerceIn(0f, 1f)) * 0.55f
    val entries = remember {
        listOf(
            DrawerEntry("Notes", Icons.Outlined.Notes, Screen.Home.route),
            DrawerEntry("Pinned", Icons.Outlined.PushPin, Screen.Pinned.route),
            DrawerEntry("Archive", Icons.Outlined.Inventory2, Screen.Archive.route),
            DrawerEntry("Trash", Icons.Outlined.DeleteOutline, Screen.Trash.route),
            DrawerEntry("Writing Stats", Icons.Outlined.BarChart, Screen.Stats.route),
            DrawerEntry("Scan QR note", Icons.Outlined.QrCodeScanner, "scan_qr"),
            DrawerEntry("Settings", Icons.Outlined.Settings, Screen.Settings.route),
            DrawerEntry("About NyxPad", Icons.Outlined.Info, Screen.About.route),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f),
    ) {
        if (scrimAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NyxScrim.copy(alpha = scrimAlpha))
                    .pointerInput(open) {
                        if (open) {
                            detectTapGestures { onClose() }
                        }
                    },
            )
        }

        if (!open) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(20.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { edgeDrag = 0f },
                            onDragEnd = { edgeDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                edgeDrag += dragAmount
                                if (edgeDrag > with(density) { 22.dp.toPx() }) {
                                    change.consume()
                                    onOpen()
                                }
                            },
                        )
                    },
            )
        }

        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .width(drawerWidth)
                .fillMaxHeight()
                .statusBarsPadding()
                .background(palette.surface.copy(alpha = 0.92f))
                .drawBehind {
                    drawLine(
                        color = palette.border,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                .pointerInput(open) {
                    if (open) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX.value < -drawerWidthPx * 0.28f) {
                                    onClose()
                                } else {
                                    scope.launch {
                                        offsetX.animateTo(0f, tween(200))
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount)
                                            .coerceIn(-drawerWidthPx, 0f),
                                    )
                                }
                            },
                        )
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_nyxpad_logo),
                    contentDescription = "NyxPad",
                    modifier = Modifier
                        .width(140.dp)
                        .height(37.dp),
                )
                Text(
                    text = "Write without limits",
                    style = NyxCaption,
                    color = palette.subtext,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.divider),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                entries.forEach { entry ->
                    val active = currentRoute == entry.route
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                color = if (active) accent.copy(alpha = 0.14f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable {
                                if (entry.route == "scan_qr") {
                                    onScanQr()
                                } else {
                                    onNavigate(entry.route)
                                }
                                onClose()
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = null,
                            tint = if (active) accent else palette.subtext,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = entry.label,
                            style = NyxBody,
                            color = if (active) accent else palette.text,
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.divider),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Text(
                    text = "NyxPad v${BuildConfig.VERSION_NAME}",
                    style = NyxCaption,
                    color = palette.subtext,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(40.dp)
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/xspeen/nyxpad-android"),
                                    ),
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open source on GitHub",
                        style = NyxCaption,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = accent.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}
