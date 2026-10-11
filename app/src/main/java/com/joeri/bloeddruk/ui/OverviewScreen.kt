package com.joeri.bloeddruk.ui

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.BloodPressureEntry
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.SystemUpdate
import com.joeri.bloeddruk.data.UserProfile
import java.time.LocalDate
import java.time.ZoneId

// Kolombreedtes (gewichten): DD, MM, JJ, tijd, bovendruk, onderdruk, pols
private val W = listOf(0.8f, 0.8f, 0.8f, 1.7f, 1.9f, 1.9f, 1.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    entries: List<BloodPressureEntry>,
    userProfile: UserProfile = UserProfile(),
    snackbarHostState: SnackbarHostState,
    onAdd: () -> Unit,
    onEdit: (BloodPressureEntry) -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenMedication: (initialTab: Int) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDoctor: () -> Unit = {},
    onOpenBloodType: () -> Unit = {},
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
                                Text("Bloeddruk", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                                Text("& Medicatie", fontSize = 13.sp, color = InkSoft)
                            }
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Filled.Settings, contentDescription = null, tint = InkSoft)
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
                        icon = { Icon(Icons.Filled.Dashboard, null) },
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
                        icon = { Icon(Icons.Filled.Bloodtype, null) },
                        label = {
                            Text(
                                if (userProfile.bloodType.isNotBlank()) "Bloedgroep (${userProfile.bloodType})"
                                else "Mijn Bloedgroep"
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenBloodType()
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
                        label = {
                            Text(
                                if (userProfile.doctorName.isNotBlank()) "Dokter (${userProfile.doctorName})"
                                else "Dokter & Zorgverlener"
                            )
                        },
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

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Alarm, null) },
                        label = { Text("Alarmen & Herinneringen") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenAlarms()
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
                        label = { Text("Controleer op updates") },
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
                    title = { Text("Bloeddruk", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        // Drie streepjes (Hamburger Menu) linksboven
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Zoeken")
                        }
                        IconButton(onClick = onExport) {
                            Icon(Icons.Filled.Share, contentDescription = "Exporteren")
                        }
                        IconButton(onClick = onOpenAlarms) {
                            Icon(Icons.Filled.Alarm, contentDescription = "Alarmen")
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
                        icon = { Icon(Icons.Filled.TableChart, contentDescription = null) },
                        label = { Text("Overzicht") }
                    )
                    // Medicatie snelkoppeling
                    NavigationBarItem(
                        selected = false,
                        onClick = { onOpenMedication(0) },
                        icon = { Icon(Icons.Filled.MedicalServices, contentDescription = null) },
                        label = { Text("Medicatie") }
                    )
                    // Toevoegen
                    NavigationBarItem(
                        selected = false,
                        onClick = onAdd,
                        icon = {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White) }
                        },
                        label = { Text("Toevoegen") }
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
                if (tab == 0) TableTab(entries, onEdit) else ChartTab(entries)
            }
        }
    }
}

@Composable
private fun TableTab(entries: List<BloodPressureEntry>, onEdit: (BloodPressureEntry) -> Unit) {
    val monthNames = listOf(
        "", "Januari", "Februari", "Maart", "April", "Mei", "Juni",
        "Juli", "Augustus", "September", "Oktober", "November", "December"
    )

    // Groepeer de metingen op Maand en Jaar
    val groupedByMonth = remember(entries) {
        entries.sortedByDescending { it.timestamp }.groupBy {
            val dt = it.timestamp.toLocalDateTime()
            dt.year to dt.monthValue
        }
    }

    Column(Modifier.fillMaxSize()) {
        StatusCard(entries)
        Legend()
        TableHeader()
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nog geen metingen.\nTik op Toevoegen om te starten.",
                    color = InkSoft,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().background(Surface)) {
                groupedByMonth.forEach { (yearMonth, monthEntries) ->
                    val (year, month) = yearMonth
                    val monthTitle = "${monthNames[month]} $year"

                    // Maandtitel balk (bijv. "Oktober 2026")
                    item(key = "month_header_${year}_${month}") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEFEFF4))
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                monthTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                        }
                    }

                    items(monthEntries, key = { it.id }) { e ->
                        EntryRow(e, onClick = { onEdit(e) })
                    }
                }
                item { Box(Modifier.height(16.dp)) }
            }
        }
    }
}

/** Kaart met kloppend hart + geanimeerde voortgangsbalk voor de metingen van vandaag. */
@Composable
private fun StatusCard(entries: List<BloodPressureEntry>) {
    val today = LocalDate.now(ZoneId.systemDefault())
    val todays = entries.filter { it.timestamp.toLocalDateTime().toLocalDate() == today }
    val hasMorning = todays.any { it.isMorning }
    val hasEvening = todays.any { !it.isMorning }
    val done = (if (hasMorning) 1 else 0) + (if (hasEvening) 1 else 0)

    val progress by animateFloatAsState(
        targetValue = done / 2f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "voortgang"
    )

    // Continu kloppen: sneller zolang de dag nog niet compleet is.
    val transition = rememberInfiniteTransition(label = "hartslag")
    val beat by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            tween(if (done == 2) 1100 else 700, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "klop"
    )
    // Extra "bump" telkens er een meting bijkomt.
    val bump = remember { Animatable(1f) }
    LaunchedEffect(entries.size) {
        bump.animateTo(1.6f, tween(150))
        bump.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = HeartRed,
                    modifier = Modifier.size(34.dp).scale(beat * bump.value)
                )
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Vandaag", color = InkSoft, fontSize = 12.sp)
                    Text(
                        if (done == 2) "Beide metingen gedaan" else "$done van 2 metingen",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(10.dp).clip(CircleShape),
                color = PulseColor,
                trackColor = Divider,
                strokeCap = StrokeCap.Round
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChip("Ochtend", Icons.Filled.WbSunny, hasMorning)
                StatusChip("Avond", Icons.Filled.DarkMode, hasEvening)
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, icon: ImageVector, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (done) PulseColor else InkSoft, modifier = Modifier.size(18.dp))
        Text(
            "  $label ${if (done) "✓" else "–"}",
            color = if (done) PulseColor else InkSoft,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Uitleg van de kleurcodes. */
@Composable
private fun Legend() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BpLevel.entries.forEach { lvl ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(lvl.color))
                Text(" ${lvl.label}", fontSize = 11.sp, color = InkSoft)
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Column(Modifier.fillMaxWidth().background(Surface)) {
        // Rij met pictogrammen
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            IconCell(Icons.Filled.CalendarMonth, DateColor, W[0] + W[1] + W[2])
            IconCell(Icons.Filled.Schedule, TimeColor, W[3])
            IconCell(Icons.Filled.Speed, Color(0xFF6C63B5), W[4] + W[5])
            IconCell(Icons.Filled.Favorite, HeartRed, W[6])
        }
        // Groepskoppen
        Row(Modifier.fillMaxWidth()) {
            Group("Datum", DateColor, Color.White, W[0] + W[1] + W[2])
            Group("Tijd", TimeColor, Color.White, W[3])
            Group("Bloeddruk", PressureColor, Ink, W[4] + W[5])
            Group("Pols", PulseColor, Color.White, W[6])
        }
        // Subkoppen
        Row(Modifier.fillMaxWidth()) {
            Sub("DD", DateColor, Color.White, W[0])
            Sub("MM", DateColor, Color.White, W[1])
            Sub("JJ", DateColor, Color.White, W[2])
            Sub("UU:MM", TimeColor, Color.White, W[3])
            Sub("Bovendruk", PressureColor, Ink, W[4])
            Sub("Onderdruk", PressureColor, Ink, W[5])
            Sub("BPM", PulseColor, Color.White, W[6])
        }
    }
}

@Composable
private fun RowScope.IconCell(icon: ImageVector, tint: Color, weight: Float) {
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
    }
}

@Composable
private fun RowScope.Group(text: String, bg: Color, fg: Color, weight: Float) {
    Box(
        Modifier.weight(weight).background(bg).padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
}

@Composable
private fun RowScope.Sub(text: String, bg: Color, fg: Color, weight: Float) {
    Box(
        Modifier.weight(weight).background(bg.copy(alpha = if (bg == PressureColor) 0.7f else 0.85f)).padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = fg, fontSize = 10.5.sp, maxLines = 1, softWrap = false) }
}

@Composable
private fun EntryRow(e: BloodPressureEntry, onClick: () -> Unit) {
    val dt = e.timestamp.toLocalDateTime()
    val extra = (e.tags.tagList() + listOfNotNull(e.note.trim().ifEmpty { null }))
        .joinToString(" · ")

    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = if (extra.isEmpty()) 12.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Cell(two(dt.dayOfMonth), W[0])
            Cell(two(dt.monthValue), W[1])
            Cell(two(dt.year % 100), W[2])
            Row(
                Modifier.weight(W[3]),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (e.isMorning) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                    contentDescription = if (e.isMorning) "Ochtend" else "Avond",
                    tint = if (e.isMorning) Color(0xFFF2A93B) else Color(0xFF6C63B5),
                    modifier = Modifier.size(14.dp)
                )
                Text(" ${two(dt.hour)}:${two(dt.minute)}", fontSize = 13.sp)
            }
            ValueCell(e.systolic, systolicLevel(e.systolic), W[4])
            ValueCell(e.diastolic, diastolicLevel(e.diastolic), W[5])
            Cell(e.pulse.toString(), W[6])
        }
        if (extra.isNotEmpty()) {
            Text(
                extra,
                fontSize = 11.sp,
                color = InkSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            )
        }
        HorizontalDivider(color = Divider.copy(alpha = 0.6f))
    }
}

/** Waarde met een subtiele gekleurde pil (groen / oranje / rood / blauw). */
@Composable
private fun RowScope.ValueCell(value: Int, level: BpLevel, weight: Float) {
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(level.color.copy(alpha = 0.14f))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                value.toString(),
                color = level.ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RowScope.Cell(text: String, weight: Float) {
    Text(
        text,
        modifier = Modifier.weight(weight),
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        maxLines = 1
    )
}
