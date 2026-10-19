package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color
import com.joeri.bloeddruk.data.UserProfile

/**
 * Centrale Kleurendefinities voor Bloeddrukcategorieën.
 * Bevat de exacte goedgekeurde kleurwaarden zonder bruine tint of ongewenste blending.
 */
object BloodPressureColors {
    val Low = Color(0xFF3987D7)          // Blauw #3987D7 voor Lage bloeddruk
    val Normal = Color(0xFF4CAF50)       // Groen #4CAF50 voor Normaal
    val Elevated = Color(0xFFFB8C00)     // Oranje #FB8C00 voor Verhoogde bloeddruk
    val High = Color(0xFFE53935)         // Rood #E53935 voor Hoge bloeddruk
    val CriticalHigh = Color(0xFFE53935) // Zichtbaar rood #E53935 voor Hoog (met interne waarschuwing)

    val LowBg = Color(0xFFE0F2FE)
    val NormalBg = Color(0xFFE1F3E8)
    val ElevatedBg = Color(0xFFFFF1D6)
    val HighBg = Color(0xFFFEE2E2)
}

/**
 * EXCLUSIEF 4 ZICHTBARE BLOEDDRUKCATEGORIEËN
 */
enum class BpCategory(
    val label: String,
    val color: Color,
    val bgContainer: Color,
    val pointerFraction: Float
) {
    LOW("Laag", BloodPressureColors.Low, BloodPressureColors.LowBg, 0.125f),
    NORMAL("Normaal", BloodPressureColors.Normal, BloodPressureColors.NormalBg, 0.375f),
    ELEVATED("Verhoogd", BloodPressureColors.Elevated, BloodPressureColors.ElevatedBg, 0.625f),
    HIGH("Hoog", BloodPressureColors.High, BloodPressureColors.HighBg, 0.875f)
}

/** Geharmoniseerd overkoepelend beoordelingsresultaat */
data class BpClassification(
    val category: BpCategory,
    val label: String = category.label,
    val color: Color = category.color,
    val bgContainer: Color = category.bgContainer,
    val pointerFraction: Float = category.pointerFraction,
    val isCritical: Boolean = false // Interne vlag voor veiligheidswaarschuwing
) {
    val ink: Color get() = color
}

/**
 * Centrale, medisch verantwoorde beoordelingsfunctie voor THUISMETINGEN bij volwassenen.
 * Beoordeelt ZOVEL de systolische (bovendruk) als diastolische (onderdruk) waarde.
 * Bij verschillende categorieën krijgt altijd de HOOGSTE toepasselijke risicocategorie voorrang.
 *
 * Exclusief 4 Zichtbare Categorieën (Laag, Normaal, Verhoogd, Hoog):
 * 1. Ernstig verhoogde waarden (SYS >= 180 OF DIA >= 110): Zichtbare categorie 'Hoog' (Rood #E53935) + isCritical vlag voor waarschuwing.
 * 2. Hoge bloeddruk (SYS >= 135 OF DIA >= 85): Zichtbare categorie 'Hoog' (Rood #E53935).
 * 3. Verhoogde bloeddruk (SYS >= 120 OF DIA >= 80): Zichtbare categorie 'Verhoogd' (Oranje #FB8C00).
 * 4. Normale bloeddruk (SYS < 120 EN DIA < 80): Zichtbare categorie 'Normaal' (Groen #4CAF50).
 * 5. Lage bloeddruk (SYS < 90 EN DIA < 60): Zichtbare categorie 'Laag' (Blauw #3987D7).
 */
fun classifyBloodPressure(
    systolic: Int,
    diastolic: Int,
    userProfile: UserProfile = UserProfile()
): BpClassification {
    // 1. Controleer ERNSTIG VERHOOGDE WAARDEN (SYS >= 180 OF DIA >= 110)
    if (systolic >= 180 || diastolic >= 110) {
        return BpClassification(
            category = BpCategory.HIGH,
            label = "Hoog",
            color = BloodPressureColors.High,
            bgContainer = BloodPressureColors.HighBg,
            pointerFraction = 0.92f,
            isCritical = true
        )
    }

    // 2. Controleer HOGE BLOEDDRUK voor thuismetingen (SYS >= 135 OF DIA >= 85)
    if (systolic >= 135 || diastolic >= 85) {
        val frac = 0.75f + 0.15f * (((systolic - 135).coerceAtLeast(0)) / 45f)
        return BpClassification(
            category = BpCategory.HIGH,
            label = "Hoog",
            color = BloodPressureColors.High,
            bgContainer = BloodPressureColors.HighBg,
            pointerFraction = frac.coerceIn(0.75f, 0.90f),
            isCritical = false
        )
    }

    // 3. Controleer VERHOOGDE BLOEDDRUK voor thuismetingen (SYS >= 120 OF DIA >= 80)
    if (systolic >= 120 || diastolic >= 80) {
        val sysRatio = if (systolic >= 120) (systolic - 120) / 15f else 0f
        val diaRatio = if (diastolic >= 80) (diastolic - 80) / 5f else 0f
        val maxRatio = maxOf(sysRatio, diaRatio).coerceIn(0f, 1f)
        val frac = 0.50f + 0.23f * maxRatio
        return BpClassification(
            category = BpCategory.ELEVATED,
            label = "Verhoogd",
            color = BloodPressureColors.Elevated,
            bgContainer = BloodPressureColors.ElevatedBg,
            pointerFraction = frac,
            isCritical = false
        )
    }

    // 4. Controleer LAGE BLOEDDRUK (SYS < 90 EN DIA < 60)
    if (systolic < 90 && diastolic < 60) {
        return BpClassification(
            category = BpCategory.LOW,
            label = "Laag",
            color = BloodPressureColors.Low,
            bgContainer = BloodPressureColors.LowBg,
            pointerFraction = 0.125f,
            isCritical = false
        )
    }

    // 5. Anders: NORMAAL (SYS < 120 EN DIA < 80)
    val sysFrac = (systolic.coerceIn(90, 119) - 90) / 29f
    val frac = 0.25f + 0.23f * sysFrac
    return BpClassification(
        category = BpCategory.NORMAL,
        label = "Normaal",
        color = BloodPressureColors.Normal,
        bgContainer = BloodPressureColors.NormalBg,
        pointerFraction = frac,
        isCritical = false
    )
}
