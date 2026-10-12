package com.joeri.bloeddruk.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// Windows 11 Fluent Design System Light Colors
val LightBackground = Color(0xFFF3F3F4)
val LightSurface = Color(0xFFFFFFFF)
val LightInk = Color(0xFF1A1A1A)
val LightInkSoft = Color(0xFF5D5D5D)
val LightDivider = Color(0xFFE5E5E5)
val Primary = Color(0xFF0078D4) // Windows 11 Fluent Blue Accent

// Dark Mode Colors
val DarkBackground = Color(0xFF181818)
val DarkSurface = Color(0xFF242424)
val DarkInk = Color(0xFFF5F5F5)
val DarkInkSoft = Color(0xFFA5A5A5)
val DarkDivider = Color(0xFF383838)

// Dynamische systeemkleuren die zich automatisch aanpassen aan Lichte of Donkere modus
val Background: Color @Composable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable get() = MaterialTheme.colorScheme.surface
val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val InkSoft: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
val Divider: Color @Composable get() = MaterialTheme.colorScheme.outline

// Windows 11 Fluent Accent Colors voor de tabelkoppen & status
val DateColor = Color(0xFFE81123)     // Fluent Red
val TimeColor = Color(0xFF0078D4)     // Fluent Blue
val PressureColor: Color @Composable get() = if (MaterialTheme.colorScheme.background == DarkBackground) Color(0xFF383838) else Color(0xFFEAEAEA)
val PulseColor = Color(0xFF107C10)    // Fluent Green
val HeartRed = Color(0xFFE81123)      // Fluent Accent Red

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PulseColor,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightInk,
    onSurface = LightInk,
    surfaceVariant = Color(0xFFF3F3F4),
    outline = LightDivider
)

private val DarkScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PulseColor,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkInk,
    onSurface = DarkInk,
    surfaceVariant = Color(0xFF2A2A2A),
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
