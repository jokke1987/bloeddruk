package com.joeri.bloeddruk.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.data.UserProfile
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    entries: List<BloodPressureEntry>,
    userProfile: UserProfile = UserProfile(),
    hasUpdateAvailable: Boolean = false,
    snackbarHostState: SnackbarHostState,
    onAdd: () -> Unit,
    onEdit: (BloodPressureEntry) -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenMedication: (initialTab: Int) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDoctor: () -> Unit = {},
    onOpenBloodType: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onRestoreBackupUri: (Uri) -> Unit = {},
    onSyncGitHub: () -> Unit = {},
    onCheckForUpdates: () -> Unit,
    onExport: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onRestoreBackupUri(uri)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header van het menu
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Bloeddruk", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                                Text("& Medicatie", fontSize = 13.sp, color = InkSoft)
                            }
                        }
                        IconButton(onClick = {
                            scope.launch { drawerState.close() }
                            onOpenSettings()
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Instellingen", tint = InkSoft)
                        }
                    }

                    HorizontalDivider(color = Divider)

                    // Sectie: Algemeen
                    Text(
                        "Algemeen",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Home, null) },
                        label = { Text("Dashboard / Overzicht") },
                        selected = tab == 0,
                        onClick = {
                            tab = 0
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.ShowChart, null) },
                        label = { Text("Grafiek") },
                        selected = tab == 1,
                        onClick = {
                            tab = 1
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Search, null) },
                        label = { Text("Zoeken & Filteren") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenSearch()
                        }
                    )

                    // Sectie: Behandeling & Medicatie
                    Text(
                        "Behandeling",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.MedicalServices, null) },
                        label = { Text("Medicatie Management") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenMedication(0)
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.LocalHospital, null) },
                        label = { Text("Mijn Huisarts / Zorgverlener") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenDoctor()
                        }
                    )

                    // Sectie: Rapporten & Archief
                    Text(
                        "Rapporten & Archief",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Folder, null) },
                        label = { Text("Archief & Mappen") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenArchive()
                        }
                    )

                    // Sectie: Extra
                    Text(
                        "Extra",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.SystemUpdate, null) },
                        label = {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Controleer op updates")
                                if (hasUpdateAvailable) {
                                    Box(
                                        Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "1",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onCheckForUpdates()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Bloeddruk", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSyncGitHub) {
                            Icon(Icons.Filled.Sync, contentDescription = "Synchroniseren")
                        }
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Zoeken")
                        }
                        IconButton(onClick = onExport) {
                            Icon(Icons.Filled.Share, contentDescription = "Exporteren")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Surface) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Overzicht") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.ShowChart, contentDescription = null) },
                        label = { Text("Grafieken") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onOpenMedication(0) },
                        icon = { Icon(Icons.Filled.MedicalServices, contentDescription = null) },
                        label = { Text("Medicatie") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenSettings,
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Instellingen") }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (tab == 0) TableTab(entries, onEdit, onAdd) else ChartTab(entries)
            }
        }
    }
}

@Composable
private fun TableTab(
    entries: List<BloodPressureEntry>,
    onEdit: (BloodPressureEntry) -> Unit,
    onAdd: () -> Unit
) {
    val monthNames = listOf(
        "", "Januari", "Februari", "Maart", "April", "Mei", "Juni",
        "Juli", "Augustus", "September", "Oktober", "November", "December"
    )

    val groupedByMonth = remember(entries) {
        entries.sortedByDescending { it.timestamp }.groupBy {
            val dt = it.timestamp.toLocalDateTime()
            dt.year to dt.monthValue
        }
    }

    val now = LocalDate.now(ZoneId.systemDefault())
    val todays = entries.filter { it.timestamp.toLocalDateTime().toLocalDate() == now }
    val hasMorning = todays.any { it.isMorning }
    val hasEvening = todays.any { !it.isMorning }

    val currentHour = java.time.LocalDateTime.now().hour
    var snoozed by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        // 1. 🔔 Herinnering / Alarm Pop-up Banner
        if (!snoozed && ((currentHour in 5..12 && !hasMorning) || (currentHour in 17..23 && !hasEvening))) {
            val isMorningReminder = currentHour in 5..12
            val reminderTitle = if (isMorningReminder) "Tijd voor uw ochtendmeting! ☀️" else "Tijd voor uw avondmeting! 🌙"

            item(key = "reminder_banner") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                reminderTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAdd,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Meting Invoeren", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { snoozed = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Snooze (15 min)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Halve Cirkel Boog Gauge
        item(key = "dashboard_gauge") {
            DashboardGaugeCard(entries)
        }

        // 3. Metingen Kaart
        item(key = "measurements_card") {
            MeasurementsTableCard(entries, onEdit)
        }

        // 4. Legenda
        item(key = "legend") {
            Legend()
        }

        if (entries.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nog geen metingen.\nTik op Toevoegen om te starten.",
                        color = InkSoft,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/** Halve Cirkel Boog Gauge Card overeenkomstig het Gezondheidsdashboard ontwerp */
@Composable
private fun DashboardGaugeCard(entries: List<BloodPressureEntry>) {
    val today = LocalDate.now(ZoneId.systemDefault())
    val todays = entries.filter { it.timestamp.toLocalDateTime().toLocalDate() == today }
    val hasMorning = todays.any { it.isMorning }
    val hasEvening = todays.any { !it.isMorning }
    val count = (if (hasMorning) 1 else 0) + (if (hasEvening) 1 else 0)

    val avgSys = if (todays.isNotEmpty()) todays.map { it.systolic }.average().roundToInt() else 128
    val avgDia = if (todays.isNotEmpty()) todays.map { it.diastolic }.average().roundToInt() else 85
    val lvl = overallLevel(avgSys, avgDia)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GEZONDHEIDSDASHBOARD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(12.dp))

            // Halve Cirkel Boog Gauge
            Box(
                Modifier.size(190.dp, 100.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    val diameter = size.width - stroke
                    val topPx = size.height - diameter / 2f

                    drawArc(
                        color = Color(0xFF4C5BD4),
                        startAngle = 180f,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(stroke / 2f, topPx),
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Butt)
                    )
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = 225f,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(stroke / 2f, topPx),
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Butt)
                    )
                    drawArc(
                        color = Color(0xFFFF9800),
                        startAngle = 270f,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(stroke / 2f, topPx),
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Butt)
                    )
                    drawArc(
                        color = Color(0xFFE53935),
                        startAngle = 315f,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(stroke / 2f, topPx),
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Butt)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$avgSys/$avgDia", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("Vandaag Gemiddeld - ${lvl.label}", fontSize = 12.sp, color = lvl.color, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { count / 2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF4CAF50),
                trackColor = Divider,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = if (hasMorning) Color(0xFF4CAF50) else Color(0xFFF2A93B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("$count van 2 metingen", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = if (hasEvening) Color(0xFF4CAF50) else Color(0xFF6C63B5),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (hasEvening) "Avond ✓" else "Avond –", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkSoft)
                }
            }
        }
    }
}

/** Metingen Kaart overzicht overeenkomstig het ontwerp */
@Composable
private fun MeasurementsTableCard(
    entries: List<BloodPressureEntry>,
    onEdit: (BloodPressureEntry) -> Unit
) {
    val dateFmt = DateTimeFormatter.ofPattern("d MMM")
    val today = LocalDate.now(ZoneId.systemDefault())
    val dateText = "Metingen van ${today.format(dateFmt)}"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(dateText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
            Spacer(Modifier.height(12.dp))

            // Tabel Koptekst
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F6F8), shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tijd (Icon)", modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft)
                Text("Meting (mmHg)", modifier = Modifier.weight(1.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft, textAlign = TextAlign.Center)
                Text("Pols (BPM)", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft, textAlign = TextAlign.Center)
                Text("Medicatie", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft, textAlign = TextAlign.End)
            }

            Spacer(Modifier.height(6.dp))

            if (entries.isEmpty()) {
                Text(
                    "Nog geen metingen voor vandaag.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                entries.sortedByDescending { it.timestamp }.forEach { e ->
                    MeasurementRow(e = e, onClick = { onEdit(e) })
                }
            }
        }
    }
}

@Composable
private fun MeasurementRow(e: BloodPressureEntry, onClick: () -> Unit) {
    val dt = e.timestamp.toLocalDateTime()
    val lvl = overallLevel(e.systolic, e.diastolic)
    val hasMed = e.tags.contains("Medicatie", ignoreCase = true) || e.note.isNotBlank()

    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tijd + Zon/Maan + Kleur-bolletje
            Row(Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${two(dt.hour)}:${two(dt.minute)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Ink
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    if (e.isMorning) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = if (e.isMorning) Color(0xFFF2A93B) else Color(0xFF6C63B5),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(lvl.color)
                )
            }

            // Meting (mmHg)
            Text(
                "${e.systolic}/${e.diastolic}",
                modifier = Modifier.weight(1.3f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Ink,
                textAlign = TextAlign.Center
            )

            // Pols (BPM)
            Text(
                "${e.pulse}",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Ink,
                textAlign = TextAlign.Center
            )

            // Medicatie Pill Icon 💊
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                if (hasMed) {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = "Medicatie",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = Divider.copy(alpha = 0.5f))
    }
}

/** Uitleg van de kleurcodes. */
@Composable
private fun Legend() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BpLevel.entries.forEach { lvl ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(lvl.color)
                )
                Text(" ${lvl.label}", fontSize = 11.sp, color = InkSoft)
            }
        }
    }
}
