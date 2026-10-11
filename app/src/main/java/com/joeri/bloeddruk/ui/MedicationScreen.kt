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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.MedicationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    medications: List<MedicationItem>,
    onSave: (MedicationItem) -> Unit,
    onDelete: (MedicationItem) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingItem by remember { mutableStateOf<MedicationItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var defaultSlotForAdd by remember { mutableStateOf("MORNING") }

    fun openAdd(slot: String = "MORNING") {
        defaultSlotForAdd = slot
        editingItem = null
        showDialog = true
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Medicatiebeheer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                actions = {
                    IconButton(onClick = { openAdd() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Medicatie toevoegen")
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
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Innamemomenten") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Alle Medicatie (${medications.size})") }
                )
            }

            if (selectedTab == 0) {
                // Weergave in 4 in- en uitklapbare vakken: Ochtend, Middag, Avond, Nacht
                InnamemomentenTab(
                    medications = medications,
                    onEdit = {
                        editingItem = it
                        showDialog = true
                    },
                    onAddForSlot = { slot -> openAdd(slot) }
                )
            } else {
                // Beheer & Inklapbare lijst van alle ingevoerde medicijnen
                MedicationListTab(
                    medications = medications,
                    onEdit = {
                        editingItem = it
                        showDialog = true
                    },
                    onDelete = onDelete
                )
            }
        }
    }

    if (showDialog) {
        MedicationDialog(
            initial = editingItem,
            defaultSlot = defaultSlotForAdd,
            onDismiss = { showDialog = false },
            onSave = { item ->
                onSave(item)
                showDialog = false
            },
            onDelete = { item ->
                onDelete(item)
                showDialog = false
            }
        )
    }
}

/** Tab 1: De 4 inklapbare vakken (Ochtend, Middag, Avond, Nacht) */
@Composable
private fun InnamemomentenTab(
    medications: List<MedicationItem>,
    onEdit: (MedicationItem) -> Unit,
    onAddForSlot: (String) -> Unit
) {
    val slots = listOf(
        SlotConfig("MORNING", "Ochtend", "’s Morgens", Icons.Filled.WbSunny, Color(0xFFF2A93B)),
        SlotConfig("AFTERNOON", "Middag", "’s Middags", Icons.Filled.WbTwilight, Color(0xFFE67E22)),
        SlotConfig("EVENING", "Avond", "’s Avonds", Icons.Filled.DarkMode, Color(0xFF6C63B5)),
        SlotConfig("NIGHT", "Nacht", "Nacht", Icons.Filled.NightlightRound, Color(0xFF2C3E50))
    )

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(slots, key = { it.id }) { cfg ->
            val itemsForSlot = medications.filter { it.slot == cfg.id }
            SlotCard(
                cfg = cfg,
                items = itemsForSlot,
                onEdit = onEdit,
                onAdd = { onAddForSlot(cfg.id) }
            )
        }
    }
}

private data class SlotConfig(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun SlotCard(
    cfg: SlotConfig,
    items: List<MedicationItem>,
    onEdit: (MedicationItem) -> Unit,
    onAdd: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(items.isNotEmpty()) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        cfg.icon,
                        contentDescription = null,
                        tint = cfg.color,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(cfg.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                        Text("${items.size} medicijnen", fontSize = 12.sp, color = InkSoft)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Filled.Add, contentDescription = "Toevoegen aan ${cfg.title}", tint = Primary)
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Inklappen" else "Uitklappen",
                            tint = InkSoft
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = InkSoft.copy(alpha = 0.15f)
                    )

                    if (items.isEmpty()) {
                        Text(
                            "Geen medicatie ingevoerd voor ${cfg.subtitle.lowercase()}.",
                            fontSize = 13.sp,
                            color = InkSoft,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { item ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onEdit(item) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.MedicalServices,
                                        contentDescription = null,
                                        tint = cfg.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink)
                                        val desc = listOfNotNull(
                                            item.dosage.trim().ifEmpty { null },
                                            item.purpose.trim().ifEmpty { null }?.let { "Waarvoor: $it" },
                                            item.note.trim().ifEmpty { null }
                                        ).joinToString(" · ")
                                        if (desc.isNotEmpty()) {
                                            Text(desc, fontSize = 13.sp, color = InkSoft)
                                        }
                                    }
                                    IconButton(onClick = { onEdit(item) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Bewerken", tint = InkSoft)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tab 2: Alle medicatielijst met compacte inklapbare blokjes */
@Composable
private fun MedicationListTab(
    medications: List<MedicationItem>,
    onEdit: (MedicationItem) -> Unit,
    onDelete: (MedicationItem) -> Unit
) {
    if (medications.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Nog geen medicatie toegevoegd.\nTik op + om medicatie in te voeren.",
                color = InkSoft,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(medications, key = { it.id }) { item ->
                var isExpanded by remember { mutableStateOf(false) }

                val slotName = when (item.slot) {
                    "MORNING" -> "’s Morgens"
                    "AFTERNOON" -> "’s Middags"
                    "EVENING" -> "’s Avonds"
                    "NIGHT" -> "Nacht"
                    else -> "’s Morgens"
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                                Text("Innamemoment: $slotName", fontSize = 12.sp, color = InkSoft)
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
                            Column(Modifier.padding(top = 10.dp)) {
                                HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))
                                Spacer(Modifier.height(8.dp))

                                if (item.dosage.isNotEmpty()) {
                                    Text("Dosering: ${item.dosage}", fontSize = 13.sp, color = Ink)
                                }
                                if (item.purpose.isNotEmpty()) {
                                    Text("Waarvoor: ${item.purpose}", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                                }
                                if (item.note.isNotEmpty()) {
                                    Text("Notitie: ${item.note}", fontSize = 12.sp, color = InkSoft)
                                }

                                Row(
                                    Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { onEdit(item) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Bewerken", tint = InkSoft)
                                    }
                                    IconButton(onClick = { onDelete(item) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Verwijderen", tint = Color(0xFFE53935))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Dialog om medicatie toe te voegen of te bewerken */
@Composable
private fun MedicationDialog(
    initial: MedicationItem?,
    defaultSlot: String,
    onDismiss: () -> Unit,
    onSave: (MedicationItem) -> Unit,
    onDelete: (MedicationItem) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var dosage by remember { mutableStateOf(initial?.dosage ?: "") }
    var purpose by remember { mutableStateOf(initial?.purpose ?: "") }
    var slot by remember { mutableStateOf(initial?.slot ?: defaultSlot) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    val slots = listOf("MORNING", "AFTERNOON", "EVENING", "NIGHT")
    val slotLabels = listOf("Ochtend", "Middag", "Avond", "Nacht")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "Medicatie toevoegen" else "Medicatie bewerken",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam medicijn *") },
                    placeholder = { Text("bv. Amlodipine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosering / Inname") },
                    placeholder = { Text("bv. 1 tablet 5mg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Waarvoor dient het? (Reden / Indicatie)") },
                    placeholder = { Text("bv. Voor hoge bloeddruk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Innamemoment:", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = InkSoft)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    slots.forEachIndexed { idx, s ->
                        SegmentedButton(
                            selected = slot == s,
                            onClick = { slot = s },
                            shape = SegmentedButtonDefaults.itemShape(idx, slots.size)
                        ) {
                            Text(slotLabels[idx], fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notitie (optioneel)") },
                    placeholder = { Text("bv. Bij het ontbijt innemen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        MedicationItem(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            dosage = dosage.trim(),
                            slot = slot,
                            note = note.trim(),
                            purpose = purpose.trim()
                        )
                    )
                }
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(
                        onClick = { onDelete(initial) },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                    ) {
                        Text("Verwijderen")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuleren")
                }
            }
        }
    )
}
