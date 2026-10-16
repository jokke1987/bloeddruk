package com.joeri.bloeddruk.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// FASE 1 — Professionele Visuele Stijl (Design System Tokens)
val Primary = Color(0xFF1679C5)              // Primaire kleur blauw
val LightBackground = Color(0xFFF3F5F8)      // Achtergrond lichtgrijs
val LightSurface = Color(0xFFFFFFFF)         // Kaarten puur wit
val LightInk = Color(0xFF25364A)             // Hoofdtekst donkerblauw
val LightInkSoft = Color(0xFF667085)         // Secundaire tekst grijs
val LightDivider = Color(0xFFE9EDF2)         // Scheidingslijnen #E9EDF2

// Statusthema Kleuren
val StatusPositiveText = Color(0xFF216E46)  // Positieve status tekst
val StatusPositiveBg = Color(0xFFE1F3E8)    // Positieve status achtergrond
val StatusAttentionText = Color(0xFFA9670A) // Aandacht status tekst
val StatusAttentionBg = Color(0xFFFFF1D6)   // Aandacht status achtergrond
val StatusErrorText = Color(0xFFB42318)     // Foutmeldingen / Hoog

// Dark Mode Colors
val DarkBackground = Color(0xFF141D26)
val DarkSurface = Color(0xFF1D2936)
val DarkInk = Color(0xFFF3F5F8)
val DarkInkSoft = Color(0xFF98A2B3)
val DarkDivider = Color(0xFF2E3D4E)

// Dynamische systeemkleuren die zich automatisch aanpassen aan Lichte of Donkere modus
val Background: Color @Composable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable get() = MaterialTheme.colorScheme.surface
val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val InkSoft: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
val Divider: Color @Composable get() = MaterialTheme.colorScheme.outline

// Tabelkoppen & status
val DateColor = Color(0xFFB42318)     // Accent Red
val TimeColor = Primary               // Accent Blue
val PressureColor: Color @Composable get() = if (MaterialTheme.colorScheme.background == DarkBackground) Color(0xFF2E3D4E) else Color(0xFFEFEFF4)
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
    surfaceVariant = Color(0xFFE9EDF2),
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
    surfaceVariant = Color(0xFF2E3D4E),
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
