package com.joeri.bloeddruk.ui

import androidx.compose.ui.graphics.Color
import com.joeri.bloeddruk.data.UserProfile

/** FASE 2 & 4: Vaste 4 Bloeddruk Categorieën voor het Gezondheidsdashboard */
enum class BpCategory(
    val label: String,
    val color: Color,
    val bgContainer: Color,
    val pointerFraction: Float
) {
    LOW("Lage bloeddruk", Color(0xFF2563EB), Color(0xFFE0F2FE), 0.125f),      // 1. Blauwe zone (Lage bloeddruk)
    NORMAL("Normale bloeddruk", Color(0xFF216E46), Color(0xFFE1F3E8), 0.375f), // 2. Groene zone (Normale bloeddruk)
    ELEVATED("Licht verhoogd", Color(0xFFA9670A), Color(0xFFFFF1D6), 0.625f), // 3. Oranje zone (Licht verhoogde bloeddruk)
    HIGH("Hoge bloeddruk", Color(0xFFB42318), Color(0xFFFEE2E2), 0.875f)      // 4. Rode zone (Hoge bloeddruk)
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
 * Centrale, medisch verantwoorde beoordelingsfunctie.
 * Beoordeelt ZOVEL de systolische (bovendruk) als diastolische (onderdruk) waarde,
 * en kent de categorie toe aan het HOOGSTE risiconiveau van beide.
 */
fun classifyBloodPressure(
    systolic: Int,
    diastolic: Int,
    userProfile: UserProfile = UserProfile()
): BpClassification {
    val sysNormalMax = userProfile.sysNormalMax.coerceAtLeast(100)
    val diaNormalMax = userProfile.diaNormalMax.coerceAtLeast(60)
    val sysElevatedMax = userProfile.sysElevatedMax.coerceAtLeast(sysNormalMax + 1)
    val diaElevatedMax = userProfile.diaElevatedMax.coerceAtLeast(diaNormalMax + 1)

    // Bepaal de categorie voor systolisch
    val sysCat = when {
        systolic < 90 -> BpCategory.LOW
        systolic <= sysNormalMax -> BpCategory.NORMAL
        systolic <= sysElevatedMax -> BpCategory.ELEVATED
        else -> BpCategory.HIGH
    }

    // Bepaal de categorie voor diastolisch
    val diaCat = when {
        diastolic < 60 -> BpCategory.LOW
        diastolic <= diaNormalMax -> BpCategory.NORMAL
        diastolic <= diaElevatedMax -> BpCategory.ELEVATED
        else -> BpCategory.HIGH
    }

    // Kies het hoogste risiconiveau van beide
    val highestCategory = maxOf(sysCat, diaCat, compareBy { it.ordinal })

    // Bereken een vloeiende pointerFraction voor de 4-kleurenbalk
    val baseFraction = when (highestCategory) {
        BpCategory.LOW -> 0.05f + 0.18f * ((systolic.coerceIn(50, 90) - 50) / 40f)
        BpCategory.NORMAL -> 0.25f + 0.22f * ((systolic.coerceIn(90, sysNormalMax) - 90) / (sysNormalMax - 90f).coerceAtLeast(1f))
        BpCategory.ELEVATED -> 0.50f + 0.22f * ((systolic.coerceIn(sysNormalMax + 1, sysElevatedMax) - sysNormalMax) / (sysElevatedMax - sysNormalMax).toFloat().coerceAtLeast(1f))
        BpCategory.HIGH -> (0.75f + 0.20f * ((systolic.coerceIn(sysElevatedMax + 1, 220) - sysElevatedMax) / 80f)).coerceAtMost(0.95f)
    }

    return BpClassification(
        category = highestCategory,
        label = highestCategory.label,
        color = highestCategory.color,
        bgContainer = highestCategory.bgContainer,
        pointerFraction = baseFraction
    )
}


