package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color
import com.joeri.bloeddruk.data.UserProfile

/**
 * Centrale Kleurendefinities voor Bloeddrukcategorieën.
 * Bevat de exacte goedgekeurde kleurwaarden zonder bruine tint of ongewenste blending.
 */
object BloodPressureColors {
    val Low = Color(0xFF3987D7)      // Blauw #3987D7 voor Lage bloeddruk
    val Normal = Color(0xFF4CAF50)   // Groen #4CAF50 voor Normaal (Ideaal / Goed < 130/< 85)
    val Elevated = Color(0xFFFB8C00) // Oranje #FB8C00 voor Verhoogd (Hoog-normaal 130-139 / 85-89)
    val High = Color(0xFFE53935)     // Rood #E53935 voor Hoge bloeddruk (Hypertensie >= 140 / >= 90)

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
 * Nieuwe Gewenste Grenswaarden:
 * 1. Ernstige hypertensie (SYS >= 180 OF DIA >= 110): Zichtbare categorie 'Hoog' (Rood #E53935) + isCritical vlag voor waarschuwing.
 * 2. Hoge bloeddruk / Hypertensie (SYS >= 140 OF DIA >= 90): Zichtbare categorie 'Hoog' (Rood #E53935).
 * 3. Hoog-normaal / Verhoogd (SYS 130..139 OF DIA 85..89): Zichtbare categorie 'Verhoogd' (Oranje #FB8C00).
 * 4. Normaal / Goed / Ideaal (SYS < 130 EN DIA < 85): Zichtbare categorie 'Normaal' (Groen #4CAF50).
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

    // 2. Controleer HOGE BLOEDDRUK / HYPERTENSIE (SYS >= 140 OF DIA >= 90)
    if (systolic >= 140 || diastolic >= 90) {
        val frac = 0.75f + 0.15f * (((systolic - 140).coerceAtLeast(0)) / 40f)
        return BpClassification(
            category = BpCategory.HIGH,
            label = "Hoog",
            color = BloodPressureColors.High,
            bgContainer = BloodPressureColors.HighBg,
            pointerFraction = frac.coerceIn(0.75f, 0.90f),
            isCritical = false
        )
    }

    // 3. Controleer HOOG-NORMAAL / VERHOOGD (SYS >= 130 OF DIA >= 85)
    if (systolic >= 130 || diastolic >= 85) {
        val sysRatio = if (systolic >= 130) (systolic - 130) / 9f else 0f
        val diaRatio = if (diastolic >= 85) (diastolic - 85) / 4f else 0f
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

    // 5. Anders: NORMAAL / GOED / IDEAAL (SYS < 130 EN DIA < 85)
    val sysFrac = (systolic.coerceIn(90, 129) - 90) / 39f
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
