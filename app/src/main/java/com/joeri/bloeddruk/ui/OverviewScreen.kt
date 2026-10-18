package com.joeri.bloeddruk.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.joeri.bloeddruk.data.MedicationItem
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
    medications: List<MedicationItem> = emptyList(),
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
                                Text("Bloeddruk Assistent", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                                Text("Pro", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
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
                        label = { Text("Grafieken") },
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
                TopAppBar(
                    title = {
                        Text(
                            "Bloeddruk Assistent Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
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
                if (tab == 0) {
                    DashboardScreen(
                        entries = entries,
                        medications = medications,
                        onEdit = onEdit,
                        onAdd = onAdd,
                        onOpenMedication = { onOpenMedication(0) },
                        onOpenReport = onExport
                    )
                } else {
                    ChartTab(entries)
                }
            }
        }
    }
}

/** FASE 1: Gezondheidsdashboard Hoofdscherm Componenten */
@Composable
private fun DashboardScreen(
    entries: List<BloodPressureEntry>,
    medications: List<MedicationItem>,
    onEdit: (BloodPressureEntry) -> Unit,
    onAdd: () -> Unit,
    onOpenMedication: () -> Unit,
    onOpenReport: () -> Unit
) {
    val latestEntry = remember(entries) { entries.maxByOrNull { it.timestamp } }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Snelacties Balk
        item(key = "quick_actions") {
            QuickActionsBar(
                onNewMeasurement = onAdd,
                onOpenMedication = onOpenMedication,
                onOpenReport = onOpenReport
            )
        }

        // 2. Laatste Bloeddrukmeting
        item(key = "latest_card") {
            LatestBloodPressureCard(
                latestEntry = latestEntry,
                onAddFirst = onAdd
            )
        }

        // 3. Halve Cirkel Boog Gauge (Gezondheidsdashboard Status)
        item(key = "dashboard_gauge") {
            DashboardGaugeCard(entries)
        }

        // 4. Medicatie Vandaag Status
        item(key = "medication_today") {
            MedicationTodayCard(
                medications = medications,
                onOpenMedication = onOpenMedication
            )
        }

        // 5. Recente Meetgeschiedenis
        item(key = "recent_section") {
            RecentMeasurementsSection(
                entries = entries,
                onEdit = onEdit
            )
        }

        // 6. Legenda
        item(key = "legend") {
            Legend()
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/** Gezondheidsdashboard Kaart met dynamische real-time SYS/DIA en Canvas Driehoek-Aanwijzer (▲) */
@Composable
private fun DashboardGaugeCard(entries: List<BloodPressureEntry>) {
    val today = LocalDate.now(ZoneId.systemDefault())
    val todays = entries.filter { it.timestamp.toLocalDateTime().toLocalDate() == today }
    val latestEntry = remember(entries) { entries.maxByOrNull { it.timestamp } }

    val hasMorning = todays.any { it.isMorning }
    val hasEvening = todays.any { !it.isMorning }
    val count = (if (hasMorning) 1 else 0) + (if (hasEvening) 1 else 0)

    // Gebruik de meest recente meting van vandaag (of de allerlaatste geregistreerde meting)
    val currentDisplayEntry = todays.maxByOrNull { it.timestamp } ?: latestEntry

    val displaySys = currentDisplayEntry?.systolic ?: 120
    val displayDia = currentDisplayEntry?.diastolic ?: 80
    val lvl = overallLevel(displaySys, displayDia)

    // Continu dynamische fractie voor de driehoekige pijlaanwijzer (0.0f t/m 1.0f)
    val pointerFraction = remember(displaySys) {
        when {
            displaySys <= 100 -> 0.05f
            displaySys in 101..119 -> 0.05f + 0.20f * ((displaySys - 100) / 19f)
            displaySys in 120..129 -> 0.25f + 0.25f * ((displaySys - 120) / 9f)
            displaySys in 130..139 -> 0.50f + 0.25f * ((displaySys - 130) / 9f)
            else -> (0.75f + 0.20f * ((displaySys - 140) / 40f)).coerceAtMost(0.95f)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "GEZONDHEIDSDASHBOARD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkSoft,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(12.dp))

            // Labels boven de balk: Laag / Gemiddeld / Hoog
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Laag", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkSoft)
                Text("Gemiddeld", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkSoft)
                Text("Hoog", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkSoft)
            }

            Spacer(Modifier.height(6.dp))

            // Horizontale Kleurenbalk + Aanwijzer (▲)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                // 4 Gekleurde Balkjes (Groen, Geel, Oranje, Rood)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4CAF50)))
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFCA28)))
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFA726)))
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEF5350)))
                }

                val inkColor = MaterialTheme.colorScheme.onSurface

                // Driehoekige Aanwijzer (▲) getekend via Canvas voor 100% nauwkeurigheid op elk scherm
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    val w = size.width
                    val x = (w * pointerFraction).coerceIn(12f, w - 12f)
                    val yTop = 14.dp.toPx()

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x, yTop)
                        lineTo(x - 8.dp.toPx(), yTop + 12.dp.toPx())
                        lineTo(x + 8.dp.toPx(), yTop + 12.dp.toPx())
                        close()
                    }
                    drawPath(path, color = inkColor)
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BLOEDDRUKSTATUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    letterSpacing = 0.5.sp
                )

                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(lvl.color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(lvl.label, color = lvl.color, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            // SYS | DIA Cijfers van de meest recente meting
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$displaySys", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("SYS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft)
                }

                Box(
                    Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Divider)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$displayDia", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("DIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkSoft)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Voortgangsbalk (Groen voor gedaan, grijs voor te doen)
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

            Spacer(Modifier.height(12.dp))

            // Onderste rij: ☀️ 1 van 2 metingen   🌙 Avond –
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

/** Uitleg van de kleurcodes. */
@Composable
private fun Legend() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
