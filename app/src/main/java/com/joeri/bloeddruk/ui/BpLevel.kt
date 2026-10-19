package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color

/**
 * BpLevel enum hergebruikt voor achterwaartse compatibiliteit.
 * Gebruikt centraal de exacte goedgekeurde kleuren en classifyBloodPressure beoordeling.
 */
enum class BpLevel(val label: String, val color: Color, val ink: Color) {
    LOW("Lage bloeddruk", BloodPressureColors.Low, BloodPressureColors.Low),
    NORMAL("Optimaal", BloodPressureColors.Normal, BloodPressureColors.Normal),
    ELEVATED("Verhoogd", BloodPressureColors.Elevated, BloodPressureColors.Elevated),
    HIGH("Hoge bloeddruk", BloodPressureColors.High, BloodPressureColors.High),
    CRITICAL_HIGH("Ernstig verhoogd", BloodPressureColors.CriticalHigh, BloodPressureColors.CriticalHigh)
}

fun systolicLevel(v: Int): BpLevel = when {
    v < 90 -> BpLevel.LOW
    v < 120 -> BpLevel.NORMAL
    v < 135 -> BpLevel.ELEVATED
    v < 180 -> BpLevel.HIGH
    else -> BpLevel.CRITICAL_HIGH
}

fun diastolicLevel(v: Int): BpLevel = when {
    v < 60 -> BpLevel.LOW
    v < 80 -> BpLevel.NORMAL
    v < 85 -> BpLevel.ELEVATED
    v < 110 -> BpLevel.HIGH
    else -> BpLevel.CRITICAL_HIGH
}

/** Eindoordeel voor een meting op basis van de centrale classifyBloodPressure functie */
fun overallLevel(sys: Int, dia: Int): BpLevel {
    val cat = classifyBloodPressure(sys, dia).category
    return when (cat) {
        BpCategory.LOW -> BpLevel.LOW
        BpCategory.NORMAL -> BpLevel.NORMAL
        BpCategory.ELEVATED -> BpLevel.ELEVATED
        BpCategory.HIGH -> BpLevel.HIGH
        BpCategory.CRITICAL_HIGH -> BpLevel.CRITICAL_HIGH
    }
}
