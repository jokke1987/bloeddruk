package com.joeri.bloeddruk.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.joeri.bloeddruk.data.BloodPressureEntry
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EntrySheet(
    initial: BloodPressureEntry?,
    onDismiss: () -> Unit,
    onSave: (BloodPressureEntry) -> Unit,
    onDelete: (BloodPressureEntry) -> Unit
) {
    val context = LocalContext.current

    // Datum en tijd staan standaard op "nu", maar zijn aanpasbaar.
    var dateTime by remember {
        mutableStateOf(
            initial?.timestamp?.toLocalDateTime()
                ?: LocalDateTime.now().withSecond(0).withNano(0)
        )
    }
    var morning by remember { mutableStateOf(initial?.isMorning ?: (dateTime.hour < 12)) }
    var userChoseSession by remember { mutableStateOf(initial != null) }
    var sys by remember { mutableStateOf(initial?.systolic?.toString() ?: "") }
    var dia by remember { mutableStateOf(initial?.diastolic?.toString() ?: "") }
    var pulse by remember { mutableStateOf(initial?.pulse?.toString() ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var selectedTags by remember { mutableStateOf(initial?.tags?.tagList()?.toSet() ?: emptySet()) }
    var triedSave by remember { mutableStateOf(false) }

    // Vaste labels + eventuele eigen labels die al bij deze meting stonden.
    val allTags = remember {
        PresetTags + (initial?.tags?.tagList().orEmpty().filter { it !in PresetTags })
    }

    val sysV = sys.toIntOrNull()
    val diaV = dia.toIntOrNull()
    val pulseV = pulse.toIntOrNull()
    val sysOk = sysV != null && sysV in 50..260
    val diaOk = diaV != null && diaV in 30..160 && (sysV == null || diaV < sysV)
    val pulseOk = pulseV != null && pulseV in 30..220
    val valid = sysOk && diaOk && pulseOk

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                if (initial == null) "Nieuwe meting" else "Meting aanpassen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Ochtend / Avond
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                SegmentedButton(
                    selected = morning,
                    onClick = { morning = true; userChoseSession = true },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.WbSunny, null, Modifier.size(18.dp)) }
                ) { Text("Ochtend") }
                SegmentedButton(
                    selected = !morning,
                    onClick = { morning = false; userChoseSession = true },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.DarkMode, null, Modifier.size(18.dp)) }
                ) { Text("Avond") }
            }

            // Datum + tijd
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, y, m, d -> dateTime = dateTime.withYear(y).withMonth(m + 1).withDayOfMonth(d) },
                            dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CalendarMonth, null, Modifier.size(18.dp))
                    Text("  ${two(dateTime.dayOfMonth)} | ${two(dateTime.monthValue)} | ${two(dateTime.year % 100)}")
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, min ->
                                dateTime = dateTime.withHour(h).withMinute(min)
                                if (!userChoseSession) morning = h < 12
                            },
                            dateTime.hour, dateTime.minute, true
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp))
                    Text("  ${two(dateTime.hour)}:${two(dateTime.minute)}")
                }
            }

            // Waarden
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumberField("Bovendruk", sys, { sys = it }, triedSave && !sysOk, Modifier.weight(1f))
                NumberField("Onderdruk", dia, { dia = it }, triedSave && !diaOk, Modifier.weight(1f))
                NumberField("Pols / Hartslag", pulse, { pulse = it }, triedSave && !pulseOk, Modifier.weight(1f))
            }
            if (triedSave && !valid) {
                Text(
                    "Controleer de waarden: bovendruk 50–260, onderdruk 30–160 (lager dan bovendruk), pols 30–220.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Labels
            Text(
                "Labels",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = {
                            selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                        },
                        label = { Text(tag) }
                    )
                }
            }

            // Notitie
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 200) note = it },
                label = { Text("Notitie (optioneel)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Button(
                onClick = {
                    triedSave = true
                    if (valid) {
                        onSave(
                            BloodPressureEntry(
                                id = initial?.id ?: 0,
                                timestamp = dateTime.toMillis(),
                                isMorning = morning,
                                systolic = sysV!!,
                                diastolic = diaV!!,
                                pulse = pulseV!!,
                                note = note.trim(),
                                tags = allTags.filter { it in selectedTags }.joinToString(",")
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp)
            ) { Text("Opslaan", fontWeight = FontWeight.SemiBold) }

            if (initial != null) {
                TextButton(
                    onClick = { onDelete(initial) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Verwijderen", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: Boolean,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) onChange(it) },
        label = { Text(label, maxLines = 1, softWrap = false) },
        singleLine = true,
        isError = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
