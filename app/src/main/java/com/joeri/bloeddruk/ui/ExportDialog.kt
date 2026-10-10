package com.joeri.bloeddruk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ExportAction { OPEN, SAVE, SHARE }

/** Kies periode en formaat, en dan: direct openen, opslaan op de telefoon of delen. */
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    /** days = null betekent: alle metingen. */
    onExport: (days: Int?, pdf: Boolean, action: ExportAction) -> Unit
) {
    val options = listOf<Pair<String, Int?>>(
        "Laatste 7 dagen" to 7,
        "Laatste 14 dagen" to 14,
        "Laatste 30 dagen" to 30,
        "Laatste 90 dagen" to 90,
        "Alle metingen" to null
    )
    var sel by rememberSaveable { mutableStateOf(2) }
    var pdf by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text("Exporteren") },
        text = {
            Column {
                Text("Periode", color = InkSoft, fontSize = 13.sp)
                options.forEachIndexed { i, (label, _) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { sel = i }
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sel == i, onClick = { sel = i })
                        Text(label)
                    }
                }

                Text(
                    "Formaat",
                    color = InkSoft,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = pdf,
                        onClick = { pdf = true },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("PDF") }
                    SegmentedButton(
                        selected = !pdf,
                        onClick = { pdf = false },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("CSV") }
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Openen" to ExportAction.OPEN,
                        "Opslaan" to ExportAction.SAVE
                    ).forEach { (label, action) ->
                        FilledTonalButton(
                            onClick = { onExport(options[sel].second, pdf, action) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) { Text(label, fontSize = 13.sp, maxLines = 1) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Sluiten") } }
    )
}
