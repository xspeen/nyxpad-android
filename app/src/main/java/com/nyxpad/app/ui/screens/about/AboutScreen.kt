package com.nyxpad.app.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxpad.app.BuildConfig
import com.nyxpad.app.R
import com.nyxpad.app.ui.components.NyxScreenHeader
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxHeadline
import com.nyxpad.app.ui.theme.NyxLabel

private const val GITHUB_URL = "https://github.com/xspeen/nyxpad-android"

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val context = LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }

    fun openGithub() {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
        }.onFailure {
            Toast.makeText(context, "No browser available.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NyxScreenHeader(
                title = "About NyxPad",
                subtitle = "Write without limits",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_nyxpad_logo),
                    contentDescription = "NyxPad",
                    modifier = Modifier
                        .width(220.dp)
                        .height(59.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "A private, offline home for your words.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = NyxCaption,
                        color = accent,
                    )
                }

                Spacer(Modifier.height(26.dp))

                AboutCard(
                    icon = Icons.Outlined.Shield,
                    title = "Private by design",
                    body = "NyxPad works fully offline. The internet permission exists only " +
                        "for optional NyxDrop sharing, which serves a note to a browser on " +
                        "your own Wi-Fi and is off by default. There are no accounts, " +
                        "no analytics, no trackers, no ads and no cloud backup. Everything " +
                        "you write stays in this app's private storage on this device.",
                )

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    icon = Icons.Outlined.Code,
                    title = "Free and open source",
                    body = "NyxPad is free software, released under the MIT License. " +
                        "The complete source code is available on GitHub, and the APK is " +
                        "built by GitHub Actions from that source.",
                    actionLabel = "View source on GitHub",
                    onAction = ::openGithub,
                )

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    icon = Icons.Outlined.Lock,
                    title = "Your keys, your notes",
                    body = "Optional app and note locks use your device's biometric or PIN " +
                        "prompt. NyxPad never sees or stores your biometric data. Deleted " +
                        "notes stay in Trash until you remove them or an auto-purge runs.",
                )

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    icon = Icons.Outlined.Storage,
                    title = "No cloud, no surprises",
                    body = "There is no sync and no remote account to lose. Use the export " +
                        "tools in Settings whenever you want a portable Markdown copy of " +
                        "your notes.",
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.card)
                        .border(1.dp, palette.border, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = "BUILT WITH",
                        style = NyxLabel,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(10.dp))
                    BuiltWithRow("Kotlin & Jetpack Compose", "The interface and app logic")
                    BuiltWithRow("Room", "Local, on-device note storage")
                    BuiltWithRow("Hilt", "Dependency injection")
                    BuiltWithRow("DataStore", "Settings and streaks")
                    BuiltWithRow("WorkManager", "Trash auto-purge")
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.card)
                        .border(1.dp, palette.border, RoundedCornerShape(18.dp))
                        .clickable { showLicenses = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Typography & licenses",
                            style = NyxHeadline.copy(fontSize = 16.sp),
                            color = palette.text,
                        )
                        Text(
                            text = "Inter and JetBrains Mono under the SIL Open Font License",
                            style = NyxCaption,
                            color = palette.subtext,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = palette.subtext,
                        modifier = Modifier.size(17.dp),
                    )
                }

                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Made for writers, by people who believe words should belong to " +
                        "the person who wrote them.",
                    style = NyxCaption,
                    color = palette.subtext,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "© 2026 NyxPad contributors · MIT License",
                    style = NyxCaption.copy(fontSize = 10.sp),
                    color = palette.subtext.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text("Typography & licenses", color = palette.text) },
            text = {
                Column {
                    Text(
                        text = "NyxPad's interface uses Inter by Rasmus Andersson and " +
                            "JetBrains Mono by JetBrains. Both fonts are distributed under " +
                            "the SIL Open Font License, version 1.1.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "The app source is released under the MIT License. " +
                            "Dependency licenses are available in the project's licenses " +
                            "directory on GitHub.",
                        style = NyxBodySmall,
                        color = palette.subtext,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) {
                    Text("Close", color = accent)
                }
            },
        )
    }
}

@Composable
private fun AboutCard(
    icon: ImageVector,
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(Modifier.width(11.dp))
            Text(
                text = title,
                style = NyxHeadline.copy(fontSize = 16.sp),
                color = palette.text,
            )
        }
        Text(
            text = body,
            style = NyxBodySmall,
            color = palette.subtext,
            modifier = Modifier.padding(top = 10.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = actionLabel,
                    style = NyxBodySmall,
                    color = accent,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

@Composable
private fun BuiltWithRow(name: String, role: String) {
    val palette = LocalNyxPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(palette.subtext.copy(alpha = 0.5f), CircleShape),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = name,
            style = NyxBody,
            color = palette.text,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = role,
            style = NyxCaption,
            color = palette.subtext,
        )
    }
}
