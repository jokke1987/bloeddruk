package com.joeri.bloeddruk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.export.Exporter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    entries: List<BloodPressureEntry>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val monthNames = listOf(
        "", "Januari", "Februari", "Maart", "April", "Mei", "Juni",
        "Juli", "Augustus", "September", "Oktober", "November", "December"
    )

    // Groepeer metingen per Jaar en per Maand
    val groupedByYear = remember(entries, searchQuery) {
        entries.groupBy {
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault())
            dt.year to dt.monthValue
        }
            .filter { (yearMonth, _) ->
                val monthName = monthNames[yearMonth.second]
                val title = "$monthName ${yearMonth.first}"
                searchQuery.isBlank() || title.contains(searchQuery, ignoreCase = true)
            }
            .entries
            .groupBy { it.key.first } // Groepeer per Jaar
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Archief & Mappen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Zoekbalk voor archief
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Zoek op maand of jaar (bv. Oktober of 2026)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = InkSoft) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (groupedByYear.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Geen gearchiveerde rapporten gevonden.",
                        color = InkSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedByYear.forEach { (year, monthMap) ->
                        item(key = "year_card_$year") {
                            YearAccordionCard(
                                year = year,
                                monthMap = monthMap,
                                monthNames = monthNames,
                                onOpenPdf = { monthEntries ->
                                    val pdfFile = Exporter.exportPdf(context, monthEntries)
                                    Exporter.open(context, pdfFile, "application/pdf")
                                },
                                onSharePdf = { monthTitle, monthEntries ->
                                    val pdfFile = Exporter.exportPdf(context, monthEntries)
                                    Exporter.share(context, Exporter.uriFor(context, pdfFile), "application/pdf", "Bloeddrukmethode $monthTitle")
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun YearAccordionCard(
    year: Int,
    monthMap: List<Map.Entry<Pair<Int, Int>, List<BloodPressureEntry>>>,
    monthNames: List<String>,
    onOpenPdf: (List<BloodPressureEntry>) -> Unit,
    onSharePdf: (String, List<BloodPressureEntry>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val totalYearEntries = monthMap.sumOf { it.value.size }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.FolderZip,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Jaar $year", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                        Text("${monthMap.size} gearchiveerde maanden · $totalYearEntries metingen", fontSize = 12.sp, color = InkSoft)
                    }
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Inklappen" else "Uitklappen",
                        tint = InkSoft
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))
                    monthMap.forEach { (yearMonth, monthEntries) ->
                        val monthTitle = "${monthNames[yearMonth.second]} $year"
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPdf(monthEntries) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Folder,
                                contentDescription = null,
                                tint = Color(0xFFF2A93B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(monthTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
                                Text("${monthEntries.size} metingen · Open PDF", fontSize = 12.sp, color = InkSoft)
                            }
                            IconButton(onClick = { onSharePdf(monthTitle, monthEntries) }) {
                                Icon(Icons.Filled.Share, contentDescription = "PDF Delen", tint = Primary)
                            }
                        }
                        HorizontalDivider(color = InkSoft.copy(alpha = 0.08f))
                    }
                }
            }
        }
    }
}
