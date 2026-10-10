package com.joeri.bloeddruk.export

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.ui.BpLevel
import com.joeri.bloeddruk.ui.diastolicLevel
import com.joeri.bloeddruk.ui.overallLevel
import com.joeri.bloeddruk.ui.systolicLevel
import com.joeri.bloeddruk.ui.tagList
import com.joeri.bloeddruk.ui.toLocalDateTime
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val INK = 0xFF2B2F36.toInt()
private val SOFT = 0xFF6B7280.toInt()
private val GRID = 0xFFD9DCE1.toInt()
private val ZEBRA = 0xFFF5F6F8.toInt()
private val SYS = 0xFF4C5BD4.toInt()
private val DIA = 0xFF1E9E9E.toInt()
private val WHITE = 0xFFFFFFFF.toInt()

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val axisFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

object Exporter {

    // ---------- CSV ----------

    fun exportCsv(context: Context, entries: List<BloodPressureEntry>): File {
        val file = File(exportDir(context), "bloeddruk_${stamp()}.csv")
        val sb = StringBuilder("﻿") // BOM, zodat Excel de accenten goed leest
        sb.append("Datum;Tijd;Moment;Bovendruk (mmHg);Onderdruk (mmHg);Pols (BPM);Beoordeling;Labels;Notitie\r\n")
        entries.sortedBy { it.timestamp }.forEach { e ->
            val dt = e.timestamp.toLocalDateTime()
            val row = listOf(
                dt.format(dateFmt),
                dt.format(timeFmt),
                if (e.isMorning) "Ochtend" else "Avond",
                e.systolic.toString(),
                e.diastolic.toString(),
                e.pulse.toString(),
                overallLevel(e.systolic, e.diastolic).label,
                e.tags.tagList().joinToString(", "),
                e.note
            )
            sb.append(row.joinToString(";") { csv(it) }).append("\r\n")
        }
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    private fun csv(s: String): String =
        if (s.any { it == ';' || it == '"' || it == '\n' || it == '\r' })
            "\"" + s.replace("\"", "\"\"") + "\""
        else s

    // ---------- PDF ----------

    fun exportPdf(context: Context, entries: List<BloodPressureEntry>): File {
        val sorted = entries.sortedBy { it.timestamp }
        val file = File(exportDir(context), "bloeddruk_rapport_${stamp()}.pdf")

        val w = PdfWriter(595, 842, 40f)
        val contentW = w.pageW - 2 * w.margin

        val titleP = paint(20f, bold = true)
        val bodyP = paint(10f)
        val boldP = paint(10f, bold = true)
        val smallP = paint(8.5f, SOFT)
        val valueP = paint(10f, bold = true)
        val headP = paint(9f, WHITE, bold = true)
        val headDarkP = paint(9f, INK, bold = true)
        val fillP = Paint().apply { style = Paint.Style.FILL }
        val gridP = Paint().apply { color = GRID; strokeWidth = 0.6f }

        w.newPage()
        w.canvas.drawText("Bloeddruk-rapport", w.margin, w.y + 18f, titleP)
        w.y += 30f

        if (sorted.isEmpty()) {
            w.canvas.drawText("Geen metingen in deze periode.", w.margin, w.y + 12f, bodyP)
            w.finish(file)
            return file
        }

        val first = sorted.first().timestamp.toLocalDateTime().format(dateFmt)
        val last = sorted.last().timestamp.toLocalDateTime().format(dateFmt)
        w.canvas.drawText(
            "Periode: $first t/m $last  ·  Aangemaakt op ${LocalDate.now().format(dateFmt)}",
            w.margin, w.y + 10f, smallP
        )
        w.y += 24f

        // Samenvatting
        val am = sorted.filter { it.isMorning }
        val pm = sorted.filter { !it.isMorning }
        val highest = sorted.maxByOrNull { it.systolic }!!
        val lowest = sorted.minByOrNull { it.systolic }!!
        val counts = BpLevel.entries.associateWith { lvl ->
            sorted.count { overallLevel(it.systolic, it.diastolic) == lvl }
        }
        fun at(e: BloodPressureEntry) = e.timestamp.toLocalDateTime().format(dateFmt)
        val summary = listOf(
            "Aantal metingen" to "${sorted.size} (ochtend ${am.size}, avond ${pm.size})",
            "Gemiddelde" to avgText(sorted),
            "Gemiddelde ochtend" to avgText(am),
            "Gemiddelde avond" to avgText(pm),
            "Hoogste bovendruk" to "${highest.systolic}/${highest.diastolic} mmHg op ${at(highest)}",
            "Laagste bovendruk" to "${lowest.systolic}/${lowest.diastolic} mmHg op ${at(lowest)}",
            "Verdeling" to BpLevel.entries.joinToString("  ·  ") { "${it.label} ${counts[it]}" }
        )
        summary.forEach { (k, v) ->
            w.canvas.drawText(k, w.margin, w.y + 10f, boldP)
            w.canvas.drawText(v, w.margin + 125f, w.y + 10f, bodyP)
            w.y += 15f
        }
        w.y += 8f

        // Grafiek
        if (sorted.size >= 2) {
            drawChart(w.canvas, w.margin, w.y, contentW, 150f, sorted, smallP, gridP)
            w.y += 150f
            // legenda
            val ly = w.y + 10f
            fillP.color = SYS; w.canvas.drawCircle(w.margin + 30f, ly - 3f, 3f, fillP)
            w.canvas.drawText("Bovendruk", w.margin + 37f, ly, smallP)
            fillP.color = DIA; w.canvas.drawCircle(w.margin + 100f, ly - 3f, 3f, fillP)
            w.canvas.drawText("Onderdruk", w.margin + 107f, ly, smallP)
            w.canvas.drawText("┄ grens 140 / 90", w.margin + 175f, ly, smallP)
            w.y += 26f
        }

        // Tabel
        val xs = floatArrayOf(0f, 62f, 100f, 148f, 190f, 232f, 272f, 352f, contentW)
        val rowH = 16f

        fun header() {
            val labels = listOf("Datum", "Tijd", "Moment", "Boven", "Onder", "Pols", "Beoordeling", "Labels / notitie")
            val colors = listOf(
                0xFFE0605C, 0xFF3A9FD8, 0xFF3A9FD8, 0xFFE9E8F6, 0xFFE9E8F6, 0xFF5BA573, 0xFFE7E9ED, 0xFFE7E9ED
            ).map { it.toInt() }
            val dark = setOf(3, 4, 6, 7)
            for (i in labels.indices) {
                fillP.color = colors[i]
                w.canvas.drawRect(w.margin + xs[i], w.y, w.margin + xs[i + 1], w.y + rowH + 2f, fillP)
                w.canvas.drawText(
                    labels[i], w.margin + xs[i] + 4f, w.y + 12f,
                    if (i in dark) headDarkP else headP
                )
            }
            w.y += rowH + 2f
        }

        header()
        sorted.forEachIndexed { idx, e ->
            if (w.y + rowH > w.pageH - 50f) {
                w.newPage()
                header()
            }
            val dt = e.timestamp.toLocalDateTime()
            if (idx % 2 == 1) {
                fillP.color = ZEBRA
                w.canvas.drawRect(w.margin, w.y, w.margin + contentW, w.y + rowH, fillP)
            }
            val ty = w.y + 11.5f
            w.canvas.drawText(dt.format(dateFmt), w.margin + xs[0] + 4f, ty, bodyP)
            w.canvas.drawText(dt.format(timeFmt), w.margin + xs[1] + 4f, ty, bodyP)
            w.canvas.drawText(if (e.isMorning) "Ochtend" else "Avond", w.margin + xs[2] + 4f, ty, bodyP)

            valueP.color = systolicLevel(e.systolic).ink.toArgb()
            w.canvas.drawText(e.systolic.toString(), w.margin + xs[3] + 4f, ty, valueP)
            valueP.color = diastolicLevel(e.diastolic).ink.toArgb()
            w.canvas.drawText(e.diastolic.toString(), w.margin + xs[4] + 4f, ty, valueP)

            w.canvas.drawText(e.pulse.toString(), w.margin + xs[5] + 4f, ty, bodyP)

            val lvl = overallLevel(e.systolic, e.diastolic)
            valueP.color = lvl.ink.toArgb()
            w.canvas.drawText(lvl.label, w.margin + xs[6] + 4f, ty, valueP)

            val extra = (e.tags.tagList() + listOfNotNull(e.note.trim().ifEmpty { null }))
                .joinToString(" · ")
            if (extra.isNotEmpty()) {
                val maxW = xs[8] - xs[7] - 8f
                val text = TextUtils.ellipsize(extra, TextPaint(smallP), maxW, TextUtils.TruncateAt.END).toString()
                w.canvas.drawText(text, w.margin + xs[7] + 4f, ty, smallP)
            }
            w.canvas.drawLine(w.margin, w.y + rowH, w.margin + contentW, w.y + rowH, gridP)
            w.y += rowH
        }

        w.finish(file)
        return file
    }

    private fun avgText(list: List<BloodPressureEntry>): String =
        if (list.isEmpty()) "–"
        else "${list.map { it.systolic }.average().roundToInt()}/" +
            "${list.map { it.diastolic }.average().roundToInt()} mmHg, " +
            "pols ${list.map { it.pulse }.average().roundToInt()}"

    private fun paint(size: Float, color: Int = INK, bold: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }

    private fun drawChart(
        c: Canvas, x0: Float, y0: Float, width: Float, height: Float,
        data: List<BloodPressureEntry>, label: Paint, grid: Paint
    ) {
        val left = x0 + 26f
        val top = y0 + 6f
        val cw = width - 26f - 6f
        val ch = height - 6f - 18f
        val minT = data.first().timestamp
        val maxT = data.last().timestamp
        val yMin = (((data.minOf { it.diastolic } - 10) / 20) * 20).coerceAtLeast(40).coerceAtMost(60)
        val yMax = ((((data.maxOf { it.systolic } + 10) + 19) / 20) * 20).coerceAtLeast(160)

        fun xAt(t: Long): Float =
            if (maxT == minT) left + cw / 2f
            else left + cw * (t - minT).toFloat() / (maxT - minT).toFloat()

        fun yAt(v: Int): Float = top + ch * (1f - (v - yMin).toFloat() / (yMax - yMin).toFloat())

        var g = yMin
        while (g <= yMax) {
            c.drawLine(left, yAt(g), left + cw, yAt(g), grid)
            c.drawText(g.toString(), x0, yAt(g) + 3f, label)
            g += 20
        }

        val dashed = Paint(grid).apply {
            color = BpLevel.HIGH.color.toArgb()
            pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
            strokeWidth = 0.8f
        }
        listOf(140, 90).forEach { if (it in yMin..yMax) c.drawLine(left, yAt(it), left + cw, yAt(it), dashed) }

        fun series(value: (BloodPressureEntry) -> Int, color: Int) {
            val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = 1.6f
                strokeJoin = Paint.Join.ROUND
            }
            val path = Path()
            data.forEachIndexed { i, e ->
                val px = xAt(e.timestamp)
                val py = yAt(value(e))
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            c.drawPath(path, line)
            val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
            data.forEach { e -> c.drawCircle(xAt(e.timestamp), yAt(value(e)), 2f, dot) }
        }
        series({ it.systolic }, SYS)
        series({ it.diastolic }, DIA)

        listOf(minT, minT + (maxT - minT) / 2, maxT).distinct().forEach { t ->
            val txt = t.toLocalDateTime().format(axisFmt)
            c.drawText(txt, xAt(t) - label.measureText(txt) / 2f, top + ch + 12f, label)
        }
    }

    // ---------- Delen ----------

    fun share(context: Context, uri: Uri, mime: String, subject: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            clipData = ClipData.newRawUri("Bloeddruk", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Delen of opslaan"))
    }

    // ---------- Helpers ----------

    /**
     * Exportmap. Alleen bestanden ouder dan een dag worden opgeruimd, zodat een mail-app
     * een net gedeeld bestand nog kan uitlezen terwijl je alweer iets anders exporteert.
     */
    private fun exportDir(context: Context): File {
        val dir = File(context.cacheDir, "export").apply { mkdirs() }
        val limit = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        dir.listFiles()?.filter { it.lastModified() < limit }?.forEach { it.delete() }
        return dir
    }

    private fun stamp(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"))

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Bewaart een kopie in Downloads/Bloeddruk (Android 10+). Geeft true als dat gelukt is. */
    fun saveCopy(context: Context, file: File, mime: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/Bloeddruk")
            }
            val resolver = context.contentResolver
            val target = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return false
            val out = resolver.openOutputStream(target) ?: return false
            out.use { o -> file.inputStream().use { it.copyTo(o) } }
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Schrijft het bestand naar een plek die de gebruiker zelf koos (bestandskiezer). */
    fun copyTo(context: Context, file: File, target: Uri): Boolean =
        try {
            context.contentResolver.openOutputStream(target)
                ?.use { o -> file.inputStream().use { it.copyTo(o) } } != null
        } catch (e: Exception) {
            false
        }

    /** Opent het bestand direct in een PDF-lezer of spreadsheet-app. Geeft false als er geen app is. */
    fun open(context: Context, file: File, mime: String): Boolean {
        val uri = uriFor(context, file)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            clipData = ClipData.newRawUri("Bloeddruk", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            context.startActivity(view)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}

/** Kleine hulpklasse voor pagina's in een PDF. */
private class PdfWriter(val pageW: Int, val pageH: Int, val margin: Float) {
    private val doc = PdfDocument()
    private var page: PdfDocument.Page? = null
    private var pageNo = 0
    private val footerP = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8.5f; color = SOFT }

    lateinit var canvas: Canvas
    var y = 0f

    fun newPage() {
        closePage()
        pageNo++
        val p = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
        page = p
        canvas = p.canvas
        y = margin
    }

    private fun closePage() {
        val p = page ?: return
        canvas.drawText(
            "Indicatief overzicht van thuismetingen – geen medische diagnose.",
            margin, pageH - 24f, footerP
        )
        val t = "Pagina $pageNo"
        canvas.drawText(t, pageW - margin - footerP.measureText(t), pageH - 24f, footerP)
        doc.finishPage(p)
        page = null
    }

    fun finish(file: File) {
        closePage()
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }
}
