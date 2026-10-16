package com.joeri.bloeddruk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.data.MedicationItem
import java.time.format.DateTimeFormatter

private val detailFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm")

/** Component 1: Laatste Bloeddrukmeting Kaart */
@Composable
fun LatestBloodPressureCard(
    latestEntry: BloodPressureEntry?,
    onAddFirst: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (latestEntry == null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Nog geen metingen geregistreerd",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Voeg je eerste bloeddrukmeting toe om je gezondheid op te volgen.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                Button(
                    onClick = onAddFirst,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eerste meting toevoegen", fontSize = 13.sp)
                }
            }
        } else {
            val lvl = overallLevel(latestEntry.systolic, latestEntry.diastolic)
            val dt = latestEntry.timestamp.toLocalDateTime()

            Column(Modifier.padding(18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LAATSTE METING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        letterSpacing = 0.5.sp
                    )

                    // Status Pill
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(lvl.color.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            lvl.label,
                            color = lvl.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${latestEntry.systolic}/${latestEntry.diastolic}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                " mmHg",
                                fontSize = 13.sp,
                                color = InkSoft,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        Text(
                            dt.format(detailFmt),
                            fontSize = 12.sp,
                            color = InkSoft,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = HeartRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                "${latestEntry.pulse}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text("BPM", fontSize = 11.sp, color = InkSoft)
                        }
                    }
                }
            }
        }
    }
}

/** Component 2: Snelacties Balk - Flexibel en Volledig Zichtbaar */
@Composable
fun QuickActionsBar(
    onNewMeasurement: () -> Unit,
    onOpenMedication: () -> Unit,
    onOpenReport: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onNewMeasurement,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
                .weight(1.2f)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Nieuwe meting",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onOpenMedication,
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            elevation = ButtonDefaults.buttonElevation(1.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Medicatie",
                    fontSize = 12.5.sp,
                    color = Ink,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onOpenReport,
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            elevation = ButtonDefaults.buttonElevation(1.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Rapport",
                    fontSize = 12.5.sp,
                    color = Ink,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Component 3: Vandaag Medicatie Overzicht Kaart */
@Composable
fun MedicationTodayCard(
    medications: List<MedicationItem>,
    onOpenMedication: () -> Unit
) {
    val morningCount = medications.count { it.slot.contains("MORNING") }
    val afternoonCount = medications.count { it.slot.contains("AFTERNOON") }
    val eveningCount = medications.count { it.slot.contains("EVENING") }
    val nightCount = medications.count { it.slot.contains("NIGHT") }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenMedication)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MEDICATIE VANDAAG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft, letterSpacing = 0.5.sp)
                Text("Beheren >", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicationSlotStatus("Ochtend", Icons.Filled.WbSunny, morningCount, Color(0xFFF2A93B))
                MedicationSlotStatus("Middag", Icons.Filled.WbTwilight, afternoonCount, Color(0xFFE67E22))
                MedicationSlotStatus("Avond", Icons.Filled.DarkMode, eveningCount, Color(0xFF6C63B5))
                MedicationSlotStatus("Nacht", Icons.Filled.NightlightRound, nightCount, Color(0xFF2C3E50))
            }
        }
    }
}

@Composable
private fun MedicationSlotStatus(
    label: String,
    icon: Any,
    count: Int,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Ink)
        Text(
            if (count > 0) "$count stuks" else "Geen",
            fontSize = 10.5.sp,
            color = if (count > 0) Primary else InkSoft
        )
    }
}

/** Component 4: Recente Meetgeschiedenis Sectie */
@Composable
fun RecentMeasurementsSection(
    entries: List<BloodPressureEntry>,
    onEdit: (BloodPressureEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "RECENTE METINGEN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InkSoft,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        entries.take(5).forEach { e ->
            RecentMeasurementRowCard(e = e, onClick = { onEdit(e) })
        }
    }
}

@Composable
private fun RecentMeasurementRowCard(
    e: BloodPressureEntry,
    onClick: () -> Unit
) {
    val dt = e.timestamp.toLocalDateTime()
    val lvl = overallLevel(e.systolic, e.diastolic)
    val hasNote = e.note.isNotBlank() || e.tags.isNotBlank()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(lvl.color)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            two(dt.dayOfMonth) + "-" + two(dt.monthValue) + " " + two(dt.hour) + ":" + two(dt.minute),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            if (e.isMorning) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = if (e.isMorning) Color(0xFFF2A93B) else Color(0xFF6C63B5),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (hasNote) {
                        Text(
                            e.note.ifBlank { e.tags },
                            fontSize = 11.sp,
                            color = InkSoft,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${e.systolic}/${e.diastolic}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text("${e.pulse} BPM", fontSize = 11.sp, color = InkSoft)
                }
            }
        }
    }
}
