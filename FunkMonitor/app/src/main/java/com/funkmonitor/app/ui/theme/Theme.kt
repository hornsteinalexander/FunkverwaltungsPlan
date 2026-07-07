package com.funkmonitor.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF0B0E14)
val Panel = Color(0xFF131824)
val PanelBorder = Color(0xFF232B3B)
val Amber = Color(0xFFFFB300)
val AmberDim = Color(0xFF7A5A10)
val Cyan = Color(0xFF4FD1C5)
val TextPrimary = Color(0xFFE8ECF1)
val Muted = Color(0xFF6B7688)
val Danger = Color(0xFFFF5C5C)
val Ok = Color(0xFF4FD16B)

private val FunkMonitorColors = darkColorScheme(
    primary = Amber,
    secondary = Cyan,
    background = BgDark,
    surface = Panel,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Danger
)

@Composable
fun FunkMonitorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FunkMonitorColors,
        content = content
    )
}
