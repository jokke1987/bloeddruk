package com.joeri.bloeddruk.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

private val SysColor = Color(0xFF4C5BD4)
private val DiaColor = Color(0xFF1E9E9E)
private val axisFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
private val detailFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm")

private data class Avg(val sys: Int, val dia: Int, val pulse: Int)

private fun List<BloodPressureEntry>.avg(): Avg? =
    if (isEmpty()) null
    else Avg(
        map { it.systolic }.average().roundToInt(),
        map { it.diastolic }.average().roundToInt(),
        map { it.pulse }.average().roundToInt()
    )

@Composable
fun ChartTab(entries: List<BloodPressureEntry>) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var range by rememberSaveable { mutableStateOf(1) }
    var trendWindow by rememberSaveable { mutableStateOf(0) } // 0 = Geen, 1 = 7d Trend, 2 = 30d Trend
    var selected by remember { mutableStateOf<Int?>(null) }
    val labels = listOf("7 d", "30 d", "90 d", "Alles")
    val trendLabels = listOf("Losse M.", "7d Trend", "30d Trend")

    val data = remember(entries, range) {
        val days = when (range) { 0 -> 7; 1 -> 30; 2 -> 90; else -> null }
        val cutoff = if (days == null) 0L else System.currentTimeMillis() - (days * 86_400_000L)
        entries.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    }
    LaunchedEffect(data) { selected = null }

    if (isLandscape) {
        // Automatisch Schermvullend in Liggende Stand (Landscape Mode)
        FullscreenLandscapeChart(
            data = data,
            selected = selected,
            range = range,
            trendWindow = trendWindow,
            labels = labels,
            trendLabels = trendLabels,
            onRangeChange = { range = it },
            onTrendChange = { trendWindow = it },
            onSelect = { selected = it }
        )
    } else {
        // Normale Staande Stand (Portrait Mode)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                labels.forEachIndexed { i, l ->
                    SegmentedButton(
                        selected = range == i,
                        onClick = { range = i },
                        shape = SegmentedButtonDefaults.itemShape(i, labels.size)
                    ) { Text(l) }
                }
            }

            // Trend / Voortschrijdend Gemiddelde Knoppen
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                trendLabels.forEachIndexed { i, l ->
                    SegmentedButton(
                        selected = trendWindow == i,
                        onClick = { trendWindow = i },
                        shape = SegmentedButtonDefaults.itemShape(i, trendLabels.size)
                    ) { Text(l, fontSize = 12.sp) }
                }
            }

            if (data.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Text("Nog geen metingen in deze periode.", color = InkSoft)
                }
            } else {
                StatsCard(data)
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        SelectionInfo(selected?.let { data.getOrNull(it) })
                        BpLineChart(data, selected, trendWindow, modifier = Modifier.fillMaxWidth().height(260.dp)) { selected = it }
                        ChartLegend()
                    }
                }
            }
            Box(Modifier.height(8.dp))
        }
    }
}

/** Automatisch Schermvullende Grafiek in Liggende Stand (Landscape Mode) */
@Composable
private fun FullscreenLandscapeChart(
    data: List<BloodPressureEntry>,
    selected: Int?,
    range: Int,
    trendWindow: Int,
    labels: List<String>,
    trendLabels: List<String>,
    onRangeChange: (Int) -> Unit,
    onTrendChange: (Int) -> Unit,
    onSelect: (Int?) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bloeddruk Trends (Schermvullend)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleChoiceSegmentedButtonRow {
                    labels.forEachIndexed { i, l ->
                        SegmentedButton(
                            selected = range == i,
                            onClick = { onRangeChange(i) },
                            shape = SegmentedButtonDefaults.itemShape(i, labels.size)
                        ) { Text(l, fontSize = 11.sp) }
                    }
                }

                SingleChoiceSegmentedButtonRow {
                    trendLabels.forEachIndexed { i, l ->
                        SegmentedButton(
                            selected = trendWindow == i,
                            onClick = { onTrendChange(i) },
                            shape = SegmentedButtonDefaults.itemShape(i, trendLabels.size)
                        ) { Text(l, fontSize = 11.sp) }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                SelectionInfo(selected?.let { data.getOrNull(it) })
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    BpLineChart(data, selected, trendWindow, modifier = Modifier.fillMaxSize(), onSelect = onSelect)
                }
                ChartLegend()
            }
        }
    }
}

@Composable
private fun StatsCard(data: List<BloodPressureEntry>) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Gemiddelde · ${data.size} metingen", color = InkSoft, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                StatBlock("Alles", data.avg())
                StatBlock("Ochtend", data.filter { it.isMorning }.avg())
                StatBlock("Avond", data.filter { !it.isMorning }.avg())
            }
        }
    }
}

@Composable
private fun RowScope.StatBlock(label: String, a: Avg?) {
    Column(Modifier.weight(1f)) {
        Text(label, color = InkSoft, fontSize = 12.sp)
        if (a == null) {
            Text("–", fontSize = 22.sp, color = InkSoft)
        } else {
            val lvl = overallLevel(a.sys, a.dia)
            Text("${a.sys}/${a.dia}", color = lvl.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("pols ${a.pulse}", color = InkSoft, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SelectionInfo(e: BloodPressureEntry?) {
    Column(Modifier.fillMaxWidth().heightIn(min = 40.dp)) {
        if (e == null) {
            Text("Tik op de grafiek voor de details van een meting.", color = InkSoft, fontSize = 12.sp)
        } else {
            val lvl = overallLevel(e.systolic, e.diastolic)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(lvl.color))
                Text(
                    "  ${e.timestamp.toLocalDateTime().format(detailFmt)} · ${if (e.isMorning) "Ochtend" else "Avond"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "${e.systolic}/${e.diastolic} mmHg · pols ${e.pulse} · ${lvl.label}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = lvl.ink
                )
            }
        }
    }
}

@Composable
private fun BpLineChart(
    data: List<BloodPressureEntry>,
    selected: Int?,
    trendWindow: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int?) -> Unit
) {
    if (data.isEmpty()) return

    val measurer = rememberTextMeasurer()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val inkSoftColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val dividerColor = MaterialTheme.colorScheme.outline
    val labelStyle = remember(inkSoftColor) { TextStyle(fontSize = 10.sp, color = inkSoftColor) }

    val minT = data.first().timestamp
    val maxT = data.last().timestamp
    val yMin = (((data.minOf { it.diastolic } - 10) / 20) * 20).coerceAtLeast(40).coerceAtMost(60)
    val yMax = ((((data.maxOf { it.systolic } + 10) + 19) / 20) * 20).coerceAtLeast(160)

    fun xAt(t: Long, left: Float, w: Float): Float =
        if (maxT == minT) left + w / 2f
        else left + w * (t - minT).toFloat() / (maxT - minT).toFloat()

    val density = LocalDensity.current

    Canvas(
        modifier
            .pointerInput(data, selected) {
                detectTapGestures { offset ->
                    val leftPx = with(density) { 38.dp.toPx() }
                    val rightPx = with(density) { 12.dp.toPx() }
                    val w = size.width - leftPx - rightPx
                    val idx = data.indices.minByOrNull { abs(xAt(data[it].timestamp, leftPx, w) - offset.x) }
                    onSelect(if (idx == selected) null else idx)
                }
            }
    ) {
        val left = 38.dp.toPx()
        val right = 12.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = 26.dp.toPx()
        val w = size.width - left - right
        val h = size.height - top - bottom

        if (w <= 0f || h <= 0f) return@Canvas

        fun y(v: Float): Float = top + h * (1f - (v - yMin) / (yMax - yMin).toFloat())

        // ---------- AFBEELDING KLEURBANDEN ACHTERGROND (Rood, Geel, Groen) ----------
        val y140 = y(140f).coerceIn(top, top + h)
        val y120 = y(120f).coerceIn(top, top + h)

        // 1. Rode Categorie Zone (>= 140 mmHg)
        val redH = (y140 - top).coerceAtLeast(0f)
        if (redH > 0f) {
            drawRect(
                color = Color(0xFFFFEBEE).copy(alpha = 0.65f),
                topLeft = Offset(left, top),
                size = Size(w, redH)
            )
        }

        // 2. Gele/Oranje Categorie Zone (120 - 139 mmHg)
        val yellowTop = if (140 in yMin..yMax) y140 else top
        val yellowH = (y120 - yellowTop).coerceAtLeast(0f)
        if (yellowH > 0f) {
            drawRect(
                color = Color(0xFFFFF8E1).copy(alpha = 0.65f),
                topLeft = Offset(left, yellowTop),
                size = Size(w, yellowH)
            )
        }

        // 3. Groene Categorie Zone (< 120 mmHg)
        val greenH = (top + h - y120).coerceAtLeast(0f)
        if (greenH > 0f) {
            drawRect(
                color = Color(0xFFE8F5E9).copy(alpha = 0.65f),
                topLeft = Offset(left, y120),
                size = Size(w, greenH)
            )
        }

        // Raster en waarden op de Y-as
        var g = yMin
        while (g <= yMax) {
            drawLine(dividerColor.copy(alpha = 0.3f), Offset(left, y(g.toFloat())), Offset(left + w, y(g.toFloat())), strokeWidth = 1.dp.toPx())
            val tl = measurer.measure(g.toString(), labelStyle)
            drawText(
                tl,
                topLeft = Offset(left - tl.size.width - 6.dp.toPx(), y(g.toFloat()) - tl.size.height / 2f)
            )
            g += 20
        }

        // Grenslijnen (140 / 90)
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        listOf(140, 90).forEach { lim ->
            if (lim in yMin..yMax) {
                drawLine(
                    BpLevel.HIGH.color.copy(alpha = 0.65f),
                    Offset(left, y(lim.toFloat())),
                    Offset(left + w, y(lim.toFloat())),
                    strokeWidth = 1.5f.dp.toPx(),
                    pathEffect = dash
                )
            }
        }

        // Datums op de X-as
        val ticks = if (maxT == minT) 1 else 6
        for (i in 0 until ticks) {
            val t = if (ticks == 1) minT else minT + (maxT - minT) * i / (ticks - 1)
            val tl = measurer.measure(t.toLocalDateTime().format(axisFmt), labelStyle)
            val x = xAt(t, left, w)
            val posX = (x - tl.size.width / 2f).coerceIn(0f, (size.width - tl.size.width).coerceAtLeast(0f))
            drawText(
                tl,
                topLeft = Offset(
                    posX,
                    top + h + 6.dp.toPx()
                )
            )
        }

        // Markering van de gekozen meting
        selected?.let { si ->
            data.getOrNull(si)?.let { e ->
                val x = xAt(e.timestamp, left, w)
                drawLine(inkSoftColor.copy(alpha = 0.5f), Offset(x, top), Offset(x, top + h), strokeWidth = 1.5f.dp.toPx())
            }
        }

        fun drawSeries(value: (BloodPressureEntry) -> Int, color: Color) {
            if (data.size > 1) {
                val path = Path()
                data.forEachIndexed { i, e ->
                    val px = xAt(e.timestamp, left, w)
                    val py = y(value(e).toFloat())
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(
                    path,
                    color.copy(alpha = if (trendWindow > 0) 0.4f else 1f),
                    style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        clipRect(left, top, left + w * progress.value, top + h) {
            drawSeries({ it.systolic }, SysColor)
            drawSeries({ it.diastolic }, DiaColor)

            // Voortschrijdend gemiddelde trendlijn (7d of 30d)
            if (trendWindow > 0 && data.size >= 2) {
                val windowDays = if (trendWindow == 1) 7 else 30
                val windowMillis = windowDays * 86_400_000L

                val sysTrendPath = Path()
                val diaTrendPath = Path()

                data.forEachIndexed { i, e ->
                    val cutoff = e.timestamp - windowMillis
                    val inWindow = data.filter { it.timestamp in cutoff..e.timestamp }
                    val avgSys = inWindow.map { it.systolic }.average().toFloat()
                    val avgDia = inWindow.map { it.diastolic }.average().toFloat()

                    val px = xAt(e.timestamp, left, w)
                    val pySys = y(avgSys)
                    val pyDia = y(avgDia)

                    if (i == 0) {
                        sysTrendPath.moveTo(px, pySys)
                        diaTrendPath.moveTo(px, pyDia)
                    } else {
                        sysTrendPath.lineTo(px, pySys)
                        diaTrendPath.lineTo(px, pyDia)
                    }
                }

                val trendEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
                drawPath(sysTrendPath, SysColor, style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round, pathEffect = trendEffect))
                drawPath(diaTrendPath, DiaColor, style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round, pathEffect = trendEffect))
            }

            // Punten op de lijnen
            data.forEachIndexed { i, e ->
                val px = xAt(e.timestamp, left, w)
                val isSel = i == selected
                val r = if (isSel) 6.dp.toPx() else 3.5f.dp.toPx()

                drawCircle(SysColor, r, Offset(px, y(e.systolic.toFloat())))
                drawCircle(Color.White, r * 0.5f, Offset(px, y(e.systolic.toFloat())))

                drawCircle(DiaColor, r, Offset(px, y(e.diastolic.toFloat())))
                drawCircle(Color.White, r * 0.5f, Offset(px, y(e.diastolic.toFloat())))
            }
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot("Bovendruk", SysColor)
        LegendDot("Onderdruk", DiaColor)
        Text("┄ Trend", color = InkSoft, fontSize = 12.sp)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text("  $label", color = InkSoft, fontSize = 12.sp)
    }
}
