package com.joeri.bloeddruk.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// Windows 11 Fluent Design System Mica / Light Theme Colors
val Background = Color(0xFFF3F3F4)
val Surface = Color(0xFFFFFFFF)
val Ink = Color(0xFF1A1A1A)
val InkSoft = Color(0xFF5D5D5D)
val Divider = Color(0xFFE5E5E5)
val Primary = Color(0xFF0078D4) // Windows 11 Fluent Blue Accent

// Dark Mode Colors
val DarkBackground = Color(0xFF202020)
val DarkSurface = Color(0xFF2D2D2D)
val DarkInk = Color(0xFFFFFFFF)
val DarkInkSoft = Color(0xFFA0A0A0)
val DarkDivider = Color(0xFF383838)

// Windows 11 Fluent Accent Colors voor de tabelkoppen & status
val DateColor = Color(0xFFE81123)     // Fluent Red
val TimeColor = Color(0xFF0078D4)     // Fluent Blue
val PressureColor = Color(0xFFEAEAEA)  // Fluent Soft Neutral
val PulseColor = Color(0xFF107C10)    // Fluent Green
val HeartRed = Color(0xFFE81123)      // Fluent Accent Red

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PulseColor,
    background = Background,
    surface = Surface,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3F3F4),
    outline = Divider
)

private val DarkScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PulseColor,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkInk,
    onSurface = DarkInk,
    surfaceVariant = Color(0xFF383838),
    outline = DarkDivider
)

@Composable
fun BloeddrukTheme(
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.toMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun two(n: Int) = n.toString().padStart(2, '0')
