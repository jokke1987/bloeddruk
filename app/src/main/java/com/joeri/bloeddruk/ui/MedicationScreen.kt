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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.MedicationItem

private data class SlotConfig(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    medications: List<MedicationItem>,
    onSave: (MedicationItem) -> Unit,
    onDelete: (MedicationItem) -> Unit,
    onBack: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var editingItem by remember { mutableStateOf<MedicationItem?>(null) }
    var defaultSlotForAdd by remember { mutableStateOf("MORNING") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Medicatie Management", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                SecondaryTabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Per Innamemoment") },
                        icon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Volledige Lijst") },
                        icon = { Icon(Icons.Filled.List, contentDescription = null) }
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
            if (tabIndex == 0) {
                InnamemomentenTab(
                    medications = medications,
                    onEdit = {
                        editingItem = it
                        showDialog = true
                    },
                    onAddForSlot = { slotId ->
                        editingItem = null
                        defaultSlotForAdd = slotId
                        showDialog = true
                    }
                )
            } else {
                MedicationListTab(
                    medications = medications,
                    onEdit = {
                        editingItem = it
                        showDialog = true
                    },
                    onDelete = onDelete,
                    onAddNew = {
                        editingItem = null
                        defaultSlotForAdd = "MORNING"
                        showDialog = true
                    }
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
            val itemsForSlot = medications.filter { it.slot.contains(cfg.id) }
            SlotCard(
                config = cfg,
                items = itemsForSlot,
                onEdit = onEdit,
                onAdd = { onAddForSlot(cfg.id) }
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/** Inklapbaar kaartvak voor één dagdeel */
@Composable
private fun SlotCard(
    config: SlotConfig,
    items: List<MedicationItem>,
    onEdit: (MedicationItem) -> Unit,
    onAdd: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(config.icon, contentDescription = null, tint = config.color, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(config.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(
                        if (items.isEmpty()) "Geen medicatie ingesteld"
                        else "${items.size} medicijn(en) · ${config.subtitle}",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                }
                IconButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = "Toevoegen", tint = Primary)
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
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))
                    if (items.isEmpty()) {
                        Text(
                            "Nog geen medicatie voor ${config.title.lowercase()}.\nTik op + om toe te voegen.",
                            fontSize = 12.sp,
                            color = InkSoft,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        items.forEach { m ->
                            CompactMedicationRow(item = m, onClick = { onEdit(m) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMedicationRow(item: MedicationItem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
            if (item.dosage.isNotBlank()) {
                Text("Dosering: ${item.dosage}", fontSize = 12.sp, color = InkSoft)
            }
            if (item.purpose.isNotBlank()) {
                Text("Waarvoor: ${item.purpose}", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Medium)
            }
        }
        Icon(Icons.Filled.Edit, contentDescription = "Bewerken", tint = InkSoft, modifier = Modifier.size(18.dp))
    }
    HorizontalDivider(color = InkSoft.copy(alpha = 0.1f))
}

/** Tab 2: Volledige overzichtslijst */
@Composable
private fun MedicationListTab(
    medications: List<MedicationItem>,
    onEdit: (MedicationItem) -> Unit,
    onDelete: (MedicationItem) -> Unit,
    onAddNew: () -> Unit
) {
    if (medications.isEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Geen medicatie ingevoerd.", color = InkSoft)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAddNew) {
                    Text("Medicatie toevoegen")
                }
            }
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

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                if (item.dosage.isNotBlank()) Text("Dosering: ${item.dosage}", fontSize = 12.sp, color = InkSoft)
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

                                if (item.purpose.isNotEmpty()) {
                                    Text("Waarvoor: ${item.purpose}", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                                }
                                if (item.note.isNotEmpty()) {
                                    Text("Notitie: ${item.note}", fontSize = 12.sp, color = InkSoft)
                                }

                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
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
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Dialog om medicatie toe te voegen of te bewerken met meerdere innamemomenten tegelijk */
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
    var note by remember { mutableStateOf(initial?.note ?: "") }

    val slots = listOf("MORNING", "AFTERNOON", "EVENING", "NIGHT")
    val slotLabels = listOf("Ochtend", "Middag", "Avond", "Nacht")

    var selectedSlots by remember {
        mutableStateOf(
            if (initial != null && initial.slot.isNotBlank()) {
                initial.slot.split(",").toSet()
            } else {
                setOf(defaultSlot)
            }
        )
    }

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
                    label = { Text("Naam medicijn") },
                    placeholder = { Text("bv. Amlodipine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosering") },
                    placeholder = { Text("bv. 5 mg - 1 tablet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Waarvoor dient dit medicijn?") },
                    placeholder = { Text("bv. Tegen hoge bloeddruk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Innamemomenten (selecteer 1 of meer):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = InkSoft)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    slots.forEachIndexed { idx, s ->
                        val isSelected = selectedSlots.contains(s)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSlots = if (isSelected) {
                                    if (selectedSlots.size > 1) selectedSlots - s else selectedSlots
                                } else {
                                    selectedSlots + s
                                }
                            },
                            label = { Text(slotLabels[idx], fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Opmerking / Instructies") },
                    placeholder = { Text("bv. Innemen met water bij het ontbijt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    val item = (initial ?: MedicationItem(name = "")).copy(
                        name = name.trim(),
                        dosage = dosage.trim(),
                        purpose = purpose.trim(),
                        slot = selectedSlots.joinToString(","),
                        note = note.trim()
                    )
                    onSave(item)
                }
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(onClick = { onDelete(initial) }) {
                        Text("Verwijderen", color = Color(0xFFE53935))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuleren")
                }
            }
        }
    )
}
