package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color
import com.joeri.bloeddruk.data.UserProfile

/**
 * Centrale Kleurendefinities voor Bloeddrukcategorieën.
 * Bevat de exacte goedgekeurde kleurwaarden zonder bruine tint of ongewenste blending.
 */
object BloodPressureColors {
    val Low = Color(0xFF3987D7)          // Blauw #3987D7 voor Lage bloeddruk
    val Normal = Color(0xFF4CAF50)       // Groen #4CAF50 voor Optimaal / Normaal
    val Elevated = Color(0xFFFB8C00)     // Oranje #FB8C00 voor Verhoogde bloeddruk
    val High = Color(0xFFE53935)         // Rood #E53935 voor Hoge bloeddruk
    val CriticalHigh = Color(0xFFB71C1C) // Donkerrood #B71C1C voor Ernstig verhoogd

    val LowBg = Color(0xFFE0F2FE)
    val NormalBg = Color(0xFFE1F3E8)
    val ElevatedBg = Color(0xFFFFF1D6)
    val HighBg = Color(0xFFFEE2E2)
    val CriticalHighBg = Color(0xFFFFEBEE)
}

/**
 * Vaste 5 Bloeddruk Categorieën voor thuismetingen.
 */
enum class BpCategory(
    val label: String,
    val color: Color,
    val bgContainer: Color,
    val pointerFraction: Float
) {
    LOW("Lage bloeddruk", BloodPressureColors.Low, BloodPressureColors.LowBg, 0.125f),
    NORMAL("Optimaal", BloodPressureColors.Normal, BloodPressureColors.NormalBg, 0.375f),
    ELEVATED("Verhoogd", BloodPressureColors.Elevated, BloodPressureColors.ElevatedBg, 0.625f),
    HIGH("Hoge bloeddruk", BloodPressureColors.High, BloodPressureColors.HighBg, 0.825f),
    CRITICAL_HIGH("Ernstig verhoogd", BloodPressureColors.CriticalHigh, BloodPressureColors.CriticalHighBg, 0.95f)
}

/** Geharmoniseerd overkoepelend beoordelingsresultaat */
data class BpClassification(
    val category: BpCategory,
    val label: String = category.label,
    val color: Color = category.color,
    val bgContainer: Color = category.bgContainer,
    val pointerFraction: Float = category.pointerFraction
) {
    val ink: Color get() = color
}

/**
 * Centrale, medisch verantwoorde beoordelingsfunctie voor THUISMETINGEN bij volwassenen.
 * Beoordeelt ZOVEL de systolische (bovendruk) als diastolische (onderdruk) waarde.
 * Bij verschillende categorieën krijgt altijd de HOOGSTE toepasselijke risicocategorie voorrang.
 *
 * Thuismeting Grenswaarden:
 * 1. Ernstig verhoogd: SYS >= 180 mmHg OF DIA >= 110 mmHg (Donkerrood #B71C1C)
 * 2. Hoge bloeddruk: SYS >= 135 mmHg OF DIA >= 85 mmHg (Rood #E53935)
 * 3. Verhoogd: SYS >= 120 mmHg OF DIA >= 80 mmHg (Oranje #FB8C00)
 * 4. Optimaal: SYS < 120 mmHg EN DIA < 80 mmHg (Groen #4CAF50)
 * 5. Lage bloeddruk: SYS < 90 mmHg EN DIA < 60 mmHg (Blauw #3987D7)
 */
fun classifyBloodPressure(
    systolic: Int,
    diastolic: Int,
    userProfile: UserProfile = UserProfile()
): BpClassification {
    // 1. Controleer ERNSTIG VERHOOGD (SYS >= 180 OF DIA >= 110) EERST!
    if (systolic >= 180 || diastolic >= 110) {
        return BpClassification(
            category = BpCategory.CRITICAL_HIGH,
            label = "Ernstig verhoogd",
            color = BloodPressureColors.CriticalHigh,
            bgContainer = BloodPressureColors.CriticalHighBg,
            pointerFraction = 0.95f
        )
    }

    // 2. Controleer HOGE BLOEDDRUK voor thuismetingen (SYS >= 135 OF DIA >= 85)
    if (systolic >= 135 || diastolic >= 85) {
        val frac = 0.75f + 0.15f * (((systolic - 135).coerceAtLeast(0)) / 45f)
        return BpClassification(
            category = BpCategory.HIGH,
            label = "Hoge bloeddruk",
            color = BloodPressureColors.High,
            bgContainer = BloodPressureColors.HighBg,
            pointerFraction = frac.coerceIn(0.75f, 0.90f)
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
            pointerFraction = frac
        )
    }

    // 4. Controleer LAGE BLOEDDRUK (SYS < 90 EN DIA < 60)
    if (systolic < 90 && diastolic < 60) {
        return BpClassification(
            category = BpCategory.LOW,
            label = "Lage bloeddruk",
            color = BloodPressureColors.Low,
            bgContainer = BloodPressureColors.LowBg,
            pointerFraction = 0.125f
        )
    }

    // 5. Anders: OPTIMAAL (SYS < 120 EN DIA < 80)
    val sysFrac = (systolic.coerceIn(90, 119) - 90) / 29f
    val frac = 0.25f + 0.23f * sysFrac
    return BpClassification(
        category = BpCategory.NORMAL,
        label = "Optimaal",
        color = BloodPressureColors.Normal,
        bgContainer = BloodPressureColors.NormalBg,
        pointerFraction = frac
    )
}
