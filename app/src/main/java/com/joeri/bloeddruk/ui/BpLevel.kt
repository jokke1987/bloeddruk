package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color

/**
 * Kleurcodering van bloeddrukwaarden.
 *
 * De grenzen staan hieronder op één plek en zijn eenvoudig aan te passen.
 * Indicatief (algemene richtwaarden), geen medische diagnose.
 */
enum class BpLevel(val label: String, val color: Color, val ink: Color) {
    LOW("Laag", Color(0xFF3A9FD8), Color(0xFF1F78AE)),
    NORMAL("Normaal", Color(0xFF3E9B5F), Color(0xFF2A7A47)),
    ELEVATED("Licht verhoogd", Color(0xFFE98A15), Color(0xFFB86400)),
    HIGH("Hoog", Color(0xFFD64545), Color(0xFFB92F2F))
}

// Grenzen in mmHg
private const val SYS_LOW = 90
private const val SYS_ELEVATED = 130
private const val SYS_HIGH = 140
private const val DIA_LOW = 60
private const val DIA_ELEVATED = 85
private const val DIA_HIGH = 90

fun systolicLevel(v: Int): BpLevel = when {
    v < SYS_LOW -> BpLevel.LOW
    v < SYS_ELEVATED -> BpLevel.NORMAL
    v < SYS_HIGH -> BpLevel.ELEVATED
    else -> BpLevel.HIGH
}

fun diastolicLevel(v: Int): BpLevel = when {
    v < DIA_LOW -> BpLevel.LOW
    v < DIA_ELEVATED -> BpLevel.NORMAL
    v < DIA_HIGH -> BpLevel.ELEVATED
    else -> BpLevel.HIGH
}

/** Eindoordeel voor een meting: de slechtste van boven- en onderdruk. */
fun overallLevel(sys: Int, dia: Int): BpLevel {
    val a = systolicLevel(sys)
    val b = diastolicLevel(dia)
    return when {
        a == BpLevel.HIGH || b == BpLevel.HIGH -> BpLevel.HIGH
        a == BpLevel.ELEVATED || b == BpLevel.ELEVATED -> BpLevel.ELEVATED
        a == BpLevel.LOW || b == BpLevel.LOW -> BpLevel.LOW
        else -> BpLevel.NORMAL
    }
}
