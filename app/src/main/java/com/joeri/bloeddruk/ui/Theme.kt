package com.joeri.bloeddruk.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// Modern Design System Tokens
val Primary = Color(0xFF1679C5)              // Primaire kleur helder blauw #1679C5
val LightBackground = Color(0xFFF8FAFC)      // Rustig lichtgrijs #F8FAFC
val LightSurface = Color(0xFFFFFFFF)         // Kaarten puur wit #FFFFFF
val LightInk = Color(0xFF1E293B)             // Hoofdtekst donker leisteengrijs #1E293B
val LightInkSoft = Color(0xFF64748B)         // Secundaire tekst grijs #64748B
val LightDivider = Color(0xFFE2E8F0)         // Scheidingslijnen #E2E8F0

// Statusthema Kleuren
val StatusPositiveText = Color(0xFF216E46)  // Positieve status tekst #216E46
val StatusPositiveBg = Color(0xFFE1F3E8)    // Positieve status achtergrond #E1F3E8
val StatusAttentionText = Color(0xFFA9670A) // Aandacht status tekst #A9670A
val StatusAttentionBg = Color(0xFFFFF1D6)   // Aandacht status achtergrond #FFF1D6
val StatusErrorText = Color(0xFFB42318)     // Foutmeldingen / Hoog #B42318

// Dark Mode Colors
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkInk = Color(0xFFF8FAFC)
val DarkInkSoft = Color(0xFF94A3B8)
val DarkDivider = Color(0xFF334155)

// Dynamische systeemkleuren die zich automatisch aanpassen aan Lichte of Donkere modus
val Background: Color @Composable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable get() = MaterialTheme.colorScheme.surface
val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val InkSoft: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
val Divider: Color @Composable get() = MaterialTheme.colorScheme.outline

// Accentkleuren voor status en overzichten
val DateColor = Color(0xFFB42318)     // Accent Red
val TimeColor = Primary               // Accent Blue
val PressureColor: Color @Composable get() = if (MaterialTheme.colorScheme.background == DarkBackground) Color(0xFF334155) else Color(0xFFF1F5F9)
val PulseColor = Color(0xFF216E46)    // Accent Green
val HeartRed = Color(0xFFB42318)      // Accent Red

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PulseColor,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightInk,
    onSurface = LightInk,
    surfaceVariant = Color(0xFFF1F5F9),
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
    surfaceVariant = Color(0xFF334155),
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
