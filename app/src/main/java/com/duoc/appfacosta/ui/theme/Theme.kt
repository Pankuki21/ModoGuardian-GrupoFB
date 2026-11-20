package com.duoc.appfacosta.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val InsertCodeColors = darkColorScheme(
    primary = Accent,
    background = BgDark,
    surface = BgCard,
    surfaceVariant = BgElevated,
    onBackground = TextMain,
    onSurface = TextMain,
    onSurfaceVariant = TextMuted,
    outline = Stroke,
    error = Danger
)

@Composable
fun AppFACOSTATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InsertCodeColors,
        typography = Typography,
        content = content
    )
}
