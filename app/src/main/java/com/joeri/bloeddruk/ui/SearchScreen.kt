package com.joeri.bloeddruk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    entries: List<BloodPressureEntry>,
    onEdit: (BloodPressureEntry) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<BpLevel?>(null) }

    val dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    val filteredEntries = remember(query, selectedLevel, entries) {
        entries.filter { e ->
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.timestamp), ZoneId.systemDefault())
            val dateStr = dt.format(dateFmt)
            val overall = overallLevel(e.systolic, e.diastolic)

            val matchesQuery = query.isBlank() ||
                    dateStr.contains(query, ignoreCase = true) ||
                    e.note.contains(query, ignoreCase = true) ||
                    e.tags.contains(query, ignoreCase = true) ||
                    e.systolic.toString().contains(query) ||
                    e.diastolic.toString().contains(query) ||
                    overall.label.contains(query, ignoreCase = true)

            val matchesLevel = selectedLevel == null || overall == selectedLevel
            matchesQuery && matchesLevel
        }.sortedByDescending { it.timestamp }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Zoeken & Filteren", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Zoekbalk
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Zoek op datum, notitie, waarde...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Wissen")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            )

            // Filter chips voor categorieën (Laag, Normaal, Licht verhoogd, Hoog)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedLevel == null,
                        onClick = { selectedLevel = null },
                        label = { Text("Alles (${entries.size})") }
                    )
                }
                items(BpLevel.entries.toTypedArray()) { lvl ->
                    val count = entries.count { overallLevel(it.systolic, it.diastolic) == lvl }
                    FilterChip(
                        selected = selectedLevel == lvl,
                        onClick = {
                            selectedLevel = if (selectedLevel == lvl) null else lvl
                        },
                        label = { Text("${lvl.label} ($count)") }
                    )
                }
            }

            // Zoekresultaten
            if (filteredEntries.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Geen metingen gevonden.",
                        color = InkSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredEntries, key = { it.id }) { e ->
                        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.timestamp), ZoneId.systemDefault())
                        val lvl = overallLevel(e.systolic, e.diastolic)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.clickable { onEdit(e) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        dt.format(dateFmt) + if (e.isMorning) " · Ochtend" else " · Avond",
                                        fontSize = 12.sp,
                                        color = InkSoft
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${e.systolic}/${e.diastolic}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Ink
                                        )
                                        Text(
                                            " mmHg",
                                            fontSize = 12.sp,
                                            color = InkSoft
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Pols: ${e.pulse}",
                                            fontSize = 14.sp,
                                            color = Ink
                                        )
                                    }
                                    val extra = (e.tags.tagList() + listOfNotNull(e.note.trim().ifEmpty { null })).joinToString(" · ")
                                    if (extra.isNotEmpty()) {
                                        Text(extra, fontSize = 12.sp, color = InkSoft, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = lvl.color)
                                ) {
                                    Text(
                                        lvl.label,
                                        color = lvl.ink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
