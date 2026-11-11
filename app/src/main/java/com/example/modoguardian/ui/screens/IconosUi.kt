package com.example.modoguardian.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object IconosUi {
    const val AccessTime = "◷"
    const val BatteryAlert = "▣"
    const val CalendarMonth = "▦"
    const val CameraAlt = "◎"
    const val CheckCircle = "✓"
    const val Description = "▤"
    const val EventNote = "☷"
    const val FlashlightOn = "▰"
    const val Fullscreen = "⛶"
    const val HelpOutline = "?"
    const val Home = "⌂"
    const val History = "↶"
    const val Lock = "♙"
    const val MicOff = "◉"
    const val Notifications = "♧"
    const val Person = "◯"
    const val Place = "⌖"
    const val Security = "⛨"
    const val Search = "⌕"
    const val Settings = "⚙"
    const val Shield = "⬡"
    const val Videocam = "▣"
    const val Visibility = "◉"
    const val VisibilityOff = "⊘"
    const val Warning = "⚠"
    const val Wifi = "◔"
    const val WifiOff = "×"
}

@Composable
fun IconoUi(
    simbolo: String,
    contentDescription: String?,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Text(
        text = simbolo,
        color = tint,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        maxLines = 1
    )
}
