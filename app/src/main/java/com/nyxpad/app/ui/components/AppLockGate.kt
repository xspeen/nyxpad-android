package com.nyxpad.app.ui.components

import androidx.biometric.BiometricManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nyxpad.app.R
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxTitle
import com.nyxpad.app.utils.BiometricUtils
import com.nyxpad.app.utils.findFragmentActivity

@Composable
fun AppLockGate(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val activity = context.findFragmentActivity()
    val canAuthenticate = remember { BiometricUtils.canAuthenticate(context) }
    var error by remember { mutableStateOf<String?>(null) }
    var hasPrompted by remember { mutableStateOf(false) }

    fun prompt() {
        if (activity == null) {
            error = "This device cannot show a security prompt."
            return
        }
        error = null
        hasPrompted = true
        BiometricUtils.authenticate(
            activity = activity,
            title = "Unlock NyxPad",
            subtitle = "Use your biometrics or device PIN",
            onSuccess = onUnlocked,
            onError = { error = it },
        )
    }

    LaunchedEffect(Unit) {
        if (canAuthenticate && !hasPrompted) prompt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_nyxpad_logo),
                contentDescription = "NyxPad",
                modifier = Modifier.width(190.dp),
            )
            Spacer(Modifier.height(36.dp))
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "NyxPad is locked",
                style = NyxTitle,
                color = palette.text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (canAuthenticate) {
                    "Unlock with your fingerprint, face, or device PIN."
                } else {
                    "Device security is unavailable. Set a screen lock in Android settings to protect NyxPad."
                },
                style = NyxBodySmall,
                color = palette.subtext,
                textAlign = TextAlign.Center,
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    style = NyxCaption,
                    color = com.nyxpad.app.ui.theme.NyxError,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    if (canAuthenticate) prompt() else onUnlocked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = palette.background,
                ),
            ) {
                Text(
                    text = if (canAuthenticate) "Unlock NyxPad" else "Continue",
                    style = NyxBody,
                )
            }
            if (!canAuthenticate) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Biometric and PIN fallback are unavailable on this device.",
                    style = NyxCaption,
                    color = palette.subtext,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
