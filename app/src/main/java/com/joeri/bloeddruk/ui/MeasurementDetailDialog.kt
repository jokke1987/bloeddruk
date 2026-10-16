package com.joeri.bloeddruk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import java.time.format.DateTimeFormatter

private val detailDateFmt = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")
private val detailTimeFmt = DateTimeFormatter.ofPattern("HH:mm")

/** FASE 2: Professionele Detailweergave van een bloeddrukmeting */
@Composable
fun MeasurementDetailDialog(
    entry: BloodPressureEntry,
    onEdit: (BloodPressureEntry) -> Unit,
    onDelete: (BloodPressureEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dt = entry.timestamp.toLocalDateTime()
    val lvl = overallLevel(entry.systolic, entry.diastolic)
    val sysLvl = systolicLevel(entry.systolic)
    val diaLvl = diastolicLevel(entry.diastolic)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Details Meting",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink
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
        },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Prominente Bloeddruk Waarde Kaart
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${entry.systolic}/${entry.diastolic}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                " mmHg",
                                fontSize = 16.sp,
                                color = InkSoft,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("Bovendruk: ${sysLvl.label}", fontSize = 12.sp, color = sysLvl.color, fontWeight = FontWeight.SemiBold)
                            Text("·", fontSize = 12.sp, color = InkSoft)
                            Text("Onderdruk: ${diaLvl.label}", fontSize = 12.sp, color = diaLvl.color, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // 2. Informatiekaarten: Hartslag, Datum, Tijd, Moment
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Datum & Tijd
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Datum & Tijd", fontSize = 11.sp, color = InkSoft)
                                Text("${dt.format(detailDateFmt)} om ${dt.format(detailTimeFmt)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            }
                        }

                        HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                        // Hartslag (BPM)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = HeartRed, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Hartslag", fontSize = 11.sp, color = InkSoft)
                                Text("${entry.pulse} BPM", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            }
                        }

                        HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                        // Meetmoment
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (entry.isMorning) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                                contentDescription = null,
                                tint = if (entry.isMorning) Color(0xFFF2A93B) else Color(0xFF6C63B5),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Meetmoment", fontSize = 11.sp, color = InkSoft)
                                Text(if (entry.isMorning) "Ochtendmeting" else "Avondmeting", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            }
                        }
                    }
                }

                // 3. Notitie & Medicatiecontext
                if (entry.note.isNotBlank() || entry.tags.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (entry.note.isNotBlank()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.Notes, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Notitie", fontSize = 11.sp, color = InkSoft)
                                        Text(entry.note, fontSize = 13.sp, color = Ink)
                                    }
                                }
                            }

                            if (entry.tags.isNotBlank()) {
                                if (entry.note.isNotBlank()) HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Labels / Medicatiecontext", fontSize = 11.sp, color = InkSoft)
                                        Text(entry.tags, fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    "Let op: Deze app vervangt geen professioneel medisch advies. Neem bij aanhoudend hoge waarden contact op met je huisarts.",
                    fontSize = 11.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
                )
            }
        },
        confirmButton = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onEdit(entry)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Bewerken")
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusErrorText),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Verwijderen")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sluiten")
            }
        }
    )

    // Bevestiging voor verwijderen
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Meting verwijderen?", fontWeight = FontWeight.Bold) },
            text = { Text("Weet je zeker dat je deze bloeddrukmeting van ${dt.format(detailDateFmt)} definitief wilt verwijderen?", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDelete(entry)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusErrorText)
                ) {
                    Text("Definitief Verwijderen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuleren")
                }
            }
        )
    }
}
