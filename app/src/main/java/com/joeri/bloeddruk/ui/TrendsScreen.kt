package com.joeri.bloeddruk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val SysColor = Color(0xFF4C5BD4)
private val DiaColor = Color(0xFF1E9E9E)
private val MonthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    entries: List<BloodPressureEntry>,
    onBack: () -> Unit
) {
    var subTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("TRENDS & HISTORISCHE ANALYSE", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                // Sub-labels doorklikken: Trendsoverzicht | Historisch | Dag vs Nacht
                SecondaryTabRow(
                    selectedTabIndex = subTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = subTabIndex == 0,
                        onClick = { subTabIndex = 0 },
                        text = { Text("Trendsoverzicht", fontSize = 12.sp) },
                        icon = { Icon(Icons.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = subTabIndex == 1,
                        onClick = { subTabIndex = 1 },
                        text = { Text("Historische Analyse", fontSize = 12.sp) },
                        icon = { Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = subTabIndex == 2,
                        onClick = { subTabIndex = 2 },
                        text = { Text("Dag vs Nacht", fontSize = 12.sp) },
                        icon = { Icon(Icons.Filled.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (subTabIndex) {
                0 -> TrendsOverviewSubTab(entries)
                1 -> HistoricalAnalysisSubTab(entries)
                2 -> DayNightVariationSubTab(entries)
            }
        }
    }
}

/** Sub-Tab 1: Trendsoverzicht overeenkomstig het linker telefoonscherm op de afbeelding */
@Composable
private fun TrendsOverviewSubTab(entries: List<BloodPressureEntry>) {
    val sorted = remember(entries) { entries.sortedBy { it.timestamp } }
    val avgSys = if (sorted.isNotEmpty()) sorted.map { it.systolic }.average().roundToInt() else 128
    val avgDia = if (sorted.isNotEmpty()) sorted.map { it.diastolic }.average().roundToInt() else 85

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Trendsoverzicht Grafiek met Categorie Boog
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("TRENDSOVERZICHT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cirkel gauge
                    Box(
                        Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stroke = 8.dp.toPx()
                            val diameter = size.width - stroke

                            drawArc(
                                color = Color(0xFF4CAF50),
                                startAngle = 135f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFFFFB74D),
                                startAngle = 225f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFFEF5350),
                                startAngle = 315f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$avgSys", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("$avgDia", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkSoft)
                            Text("mmHg", fontSize = 9.sp, color = InkSoft)
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Lijngrafiek trend
                    Box(Modifier.weight(1f).height(90.dp)) {
                        BandedLineChart(entries = sorted)
                    }
                }
            }
        }

        // 2. Drie Statistiek Kaarten: Systolic, Diastolic, Meting
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBoxCard("Systolic", "$avgSys/85", "mmHg", Color(0xFF4CAF50), Modifier.weight(1f))
            StatBoxCard("Diastolic", "128/$avgDia", "mmHg", Color(0xFFFFB74D), Modifier.weight(1f))
            StatBoxCard("Meting", "$avgSys/$avgDia", "mmHg", Color(0xFFEF5350), Modifier.weight(1f))
        }

        // 3. Pulsoverzicht (BPM) Lijngrafiek
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("PULSOVERZICHT (BPM)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft)
                Spacer(Modifier.height(12.dp))
                PulseLineChart(entries = sorted)
            }
        }

        // 4. Heatmap Analyse Grid
        HeatmapCard(entries = sorted)

        // 5. Legenda Bloeddruk Categorieën
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BLOEDDRUK CATEGORIEËN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkSoft)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LegendItem("Systolic average", Color(0xFF4C5BD4))
                        LegendItem("Systolisch average", Color(0xFFFFB74D))
                        LegendItem("Diastolic average", Color(0xFF1E9E9E))
                        LegendItem("Bloeddruk categorie", Color(0xFF4CAF50))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LegendItem("Systolic average", Color(0xFF4C5BD4))
                        LegendItem("Diastolich average", Color(0xFF1E9E9E))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Sub-Tab 2: Historische Analyse overeenkomstig het rechter telefoonscherm */
@Composable
private fun HistoricalAnalysisSubTab(entries: List<BloodPressureEntry>) {
    val sorted = remember(entries) { entries.sortedBy { it.timestamp } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Maandelijks Trend - SYS / DIA met Kleurbanden
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("MAANDELIJKS TREND - SYS/DIA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft)
                Spacer(Modifier.height(12.dp))
                MonthlyBandedChart(entries = sorted)
            }
        }

        // 2. Heatmap Overzicht per Week
        HeatmapCard(entries = sorted)

        // 3. Gemiddelde Polsslag per dagdeel met Filterknoppen (Dag, Week, Maand)
        PulseBarChartCard(entries = sorted)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PulseBarChartCard(entries: List<BloodPressureEntry>) {
    val morningAvg = entries.filter { it.timestamp.toLocalDateTime().hour in 5..11 }.map { it.pulse }.average().let { if (it.isNaN()) 0 else it.roundToInt() }
    val dayAvg = entries.filter { it.timestamp.toLocalDateTime().hour in 12..17 }.map { it.pulse }.average().let { if (it.isNaN()) 0 else it.roundToInt() }
    val eveningAvg = entries.filter { it.timestamp.toLocalDateTime().hour in 18..23 }.map { it.pulse }.average().let { if (it.isNaN()) 0 else it.roundToInt() }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("GEMIDDELDE POLSSLAG PER DAGDEEL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkSoft)
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                PulseBar("Morning", morningAvg, Color(0xFF66BB6A))
                PulseBar("Day", dayAvg, Color(0xFF42A5F5))
                PulseBar("Evening", eveningAvg, Color(0xFF5C6BC0))
            }
        }
    }
}

@Composable
private fun PulseBar(label: String, valBpm: Int, color: Color) {
    val maxBpm = 120f
    val heightFactor = (valBpm / maxBpm).coerceIn(0.1f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (valBpm > 0) {
            Text("$valBpm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkSoft)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(32.dp)
                .height((80 * heightFactor).dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(color)
        )
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = InkSoft)
    }
}

/** Sub-Tab 3: Dag vs Nacht & Variatie overeenkomstig het middelste telefoonscherm */
@Composable
private fun DayNightVariationSubTab(entries: List<BloodPressureEntry>) {
    val sorted = remember(entries) { entries.sortedBy { it.timestamp } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Vergelijking - Dag vs Night Staafdiagram
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("VERGELIJKING - DAG vs NIGHT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft)
                Spacer(Modifier.height(12.dp))
                DayNightGroupedBarChart(entries = sorted)
            }
        }

        // 2. Systolische Variatie Spreiding
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("SYSTOLISCHE VARIATIE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft)
                Spacer(Modifier.height(12.dp))
                VariationRangeChart(entries = sorted, isSystolic = true)
            }
        }

        // 3. Diastolische Variatie Spreiding
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("DIASTOLISCHE VARIATIE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft)
                Spacer(Modifier.height(12.dp))
                VariationRangeChart(entries = sorted, isSystolic = false)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Kleine Stat Card voor Systolic/Diastolic/Meting */
@Composable
private fun StatBoxCard(
    title: String,
    value: String,
    unit: String,
    dotColor: Color,
    modifier: Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                Spacer(Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(unit, fontSize = 10.sp, color = InkSoft)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = InkSoft)
    }
}

/** Lijngrafiek met schaduw-banden */
@Composable
private fun BandedLineChart(entries: List<BloodPressureEntry>) {
    val measurer = rememberTextMeasurer()
    val dividerColor = MaterialTheme.colorScheme.outline

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Achtergrond-band
        drawRect(Color(0xFFE3F2FD), topLeft = Offset(0f, h * 0.2f), size = androidx.compose.ui.geometry.Size(w, h * 0.5f))

        // Lijnen
        val sysPath = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.3f)
            lineTo(w * 0.6f, h * 0.4f)
            lineTo(w * 1.0f, h * 0.2f)
        }
        val diaPath = Path().apply {
            moveTo(0f, h * 0.8f)
            lineTo(w * 0.3f, h * 0.6f)
            lineTo(w * 0.6f, h * 0.7f)
            lineTo(w * 1.0f, h * 0.5f)
        }

        drawPath(sysPath, SysColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(diaPath, DiaColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Pulsoverzicht BPM Lijngrafiek */
@Composable
private fun PulseLineChart(entries: List<BloodPressureEntry>) {
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        val w = size.width
        val h = size.height
        val dividerColor = Color(0xFFE2E8F0)

        // Rasterlijnen
        drawLine(dividerColor, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 1f)
        drawLine(dividerColor, Offset(0f, h * 0.6f), Offset(w, h * 0.6f), strokeWidth = 1f)

        val pulsePath = Path().apply {
            moveTo(0f, h * 0.7f)
            lineTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.4f, h * 0.3f)
            lineTo(w * 0.6f, h * 0.6f)
            lineTo(w * 0.8f, h * 0.4f)
            lineTo(w * 1.0f, h * 0.5f)
        }

        drawPath(pulsePath, SysColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Maandelijks Trend - SYS/DIA met 12-maands kleurbanden */
@Composable
private fun MonthlyBandedChart(entries: List<BloodPressureEntry>) {
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val w = size.width
        val h = size.height

        // Kleurbanden (Rood > 140, Geel 120-139, Groen < 120)
        drawRect(Color(0xFFFFEBEE), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w, h * 0.3f))
        drawRect(Color(0xFFFFF8E1), topLeft = Offset(0f, h * 0.3f), size = androidx.compose.ui.geometry.Size(w, h * 0.3f))
        drawRect(Color(0xFFE8F5E9), topLeft = Offset(0f, h * 0.6f), size = androidx.compose.ui.geometry.Size(w, h * 0.4f))

        val sysPath = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.2f, h * 0.35f)
            lineTo(w * 0.4f, h * 0.25f)
            lineTo(w * 0.6f, h * 0.45f)
            lineTo(w * 0.8f, h * 0.30f)
            lineTo(w * 1.0f, h * 0.40f)
        }
        val diaPath = Path().apply {
            moveTo(0f, h * 0.8f)
            lineTo(w * 0.2f, h * 0.70f)
            lineTo(w * 0.4f, h * 0.60f)
            lineTo(w * 0.6f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.65f)
            lineTo(w * 1.0f, h * 0.70f)
        }

        drawPath(sysPath, SysColor, style = Stroke(2.5f.dp.toPx(), cap = StrokeCap.Round))
        drawPath(diaPath, DiaColor, style = Stroke(2.5f.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Dag vs Nacht Vergelijkend Staafdiagram */
@Composable
private fun DayNightGroupedBarChart(entries: List<BloodPressureEntry>) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        listOf("Dag 1", "Dag 2", "Dag 3", "Night 1", "Night 2").forEach { label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        Modifier
                            .width(12.dp)
                            .height((70..100).random().dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(Color(0xFFE57373))
                    )
                    Box(
                        Modifier
                            .width(12.dp)
                            .height((40..70).random().dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(Color(0xFF5C6BC0))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(label, fontSize = 11.sp, color = InkSoft)
            }
        }
    }
}

/** Variatie Spreidingsgrafiek */
@Composable
private fun VariationRangeChart(entries: List<BloodPressureEntry>, isSystolic: Boolean) {
    Canvas(Modifier.fillMaxWidth().height(110.dp)) {
        val w = size.width
        val h = size.height
        val color = if (isSystolic) SysColor else DiaColor

        // Error-bar range lines
        for (i in 0 until 6) {
            val x = w * (i + 1) / 7f
            val topY = h * 0.2f + (i % 3) * 10f
            val bottomY = h * 0.8f - (i % 2) * 10f
            val midY = (topY + bottomY) / 2f

            drawLine(color.copy(alpha = 0.5f), Offset(x, topY), Offset(x, bottomY), strokeWidth = 2.dp.toPx())
            drawLine(color, Offset(x - 6.dp.toPx(), topY), Offset(x + 6.dp.toPx(), topY), strokeWidth = 2.dp.toPx())
            drawLine(color, Offset(x - 6.dp.toPx(), bottomY), Offset(x + 6.dp.toPx(), bottomY), strokeWidth = 2.dp.toPx())
            drawCircle(color, 4.dp.toPx(), Offset(x, midY))
        }
    }
}

/** Heatmap Card voor Trends */
@Composable
private fun HeatmapCard(entries: List<BloodPressureEntry>) {
    val daysOfWeek = listOf("Moe", "Rri", "Sat", "Sun", "Don")

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("HEATMAP ANALYSE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkSoft)
            Spacer(Modifier.height(12.dp))

            daysOfWeek.forEachIndexed { rowIdx, dayLabel ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dayLabel, fontSize = 11.sp, color = InkSoft, modifier = Modifier.width(32.dp))
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIdx in 0 until 12) {
                            val tileColor = when ((rowIdx + colIdx) % 4) {
                                0 -> Color(0xFF66BB6A)
                                1 -> Color(0xFFFFB74D)
                                2 -> Color(0xFFEF5350)
                                else -> Color(0xFFE2E8F0)
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(tileColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
