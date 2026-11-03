package com.joeri.bloeddruk.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.data.UserProfile
import com.joeri.bloeddruk.ui.toLocalDateTime
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

/**
 * FASE 5: Beheer van Maandelijkse PDF-Rapporten per Kalendermaand.
 * Slaat op in: Downloads/Bloeddruk/Rapporten/{Jaar}/{Maandnaam}/Bloeddrukmetingen_{maand}_{jaar}.pdf
 */
object MonthlyReportManager {

    private const val TAG = "MonthlyReportManager"

    private val DutchLocale = Locale("nl", "NL")

    /**
     * Genereert of hernieuwt het complete maandrapport voor een specifieke kalendermaand.
     */
    fun generateMonthlyPdf(
        context: Context,
        year: Int,
        monthValue: Int,
        entries: List<BloodPressureEntry>,
        userProfile: UserProfile = UserProfile()
    ): File? {
        val monthEntries = entries.filter { e ->
            val dt = e.timestamp.toLocalDateTime()
            dt.year == year && dt.monthValue == monthValue
        }.sortedBy { it.timestamp }

        if (monthEntries.isEmpty()) {
            Log.d(TAG, "Geen metingen voor $monthValue-$year. Slaat generatie over.")
            return null
        }

        val monthName = Month.of(monthValue).getDisplayName(TextStyle.FULL, DutchLocale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(DutchLocale) else it.toString() }

        val fileName = "Bloeddrukmetingen_${monthName.lowercase()}_$year.pdf"
        val tempPdfFile = Exporter.exportPdf(context, monthEntries, userProfile)

        // Sla op in Downloads/Bloeddruk/Rapporten/{Jaar}/{Maandnaam}/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val relativePath = "Download/Bloeddruk/Rapporten/$year/$monthName"
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                }
                val resolver = context.contentResolver
                val targetUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (targetUri != null) {
                    resolver.openOutputStream(targetUri)?.use { out ->
                        tempPdfFile.inputStream().use { it.copyTo(out) }
                    }
                    Log.d(TAG, "Maandrapport opgeslagen in $relativePath/$fileName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fout bij opslaan maandrapport via MediaStore", e)
            }
        }

        return tempPdfFile
    }

    /**
     * Automatische update: Hergenereert alle maandrapporten die in de database voorkomen.
     */
    fun autoUpdateAllMonthlyReports(
        context: Context,
        entries: List<BloodPressureEntry>,
        userProfile: UserProfile = UserProfile()
    ) {
        val groups = entries.groupBy { e ->
            val dt = e.timestamp.toLocalDateTime()
            dt.year to dt.monthValue
        }

        groups.forEach { (yearMonth, monthEntries) ->
            generateMonthlyPdf(context, yearMonth.first, yearMonth.second, monthEntries, userProfile)
        }
    }
}
