package com.joeri.bloeddruk.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenDoctor: () -> Unit,
    onOpenExport: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackupUri: (Uri) -> Unit,
    onBack: () -> Unit
) {
    var showThresholdsDialog by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onRestoreBackupUri(uri)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Instellingen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Weergave & Thema (Donkere Modus)
            SettingsCategoryHeader("Weergave & Thema")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Donkere modus (Dark Mode)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Rustig voor de ogen 's avonds", fontSize = 12.sp, color = InkSoft)
                            }
                        }
                        Switch(
                            checked = userProfile.isDarkMode,
                            onCheckedChange = { onSaveProfile(userProfile.copy(isDarkMode = it)) }
                        )
                    }
                }
            }

            // 2. Persoonlijke Gegevens & Streefwaarden
            SettingsCategoryHeader("Persoonlijke Gegevens & Streefwaarden")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Profiel & Noodcontacten
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDoctor() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Profiel, Dokter & Noodcontacten", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                if (userProfile.doctorName.isNotBlank()) "Dokter: ${userProfile.doctorName}"
                                else "Voer profiel- en doktersgegevens in voor rapporten",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                    // Aangepaste Streefwaarden
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showThresholdsDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Aangepaste Streefwaarden", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "Normaal: < ${userProfile.sysNormalMax}/${userProfile.diaNormalMax} mmHg",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }
                }
            }

            // 3. Meldingen & Herinneringen (Centraal Beheer)
            SettingsCategoryHeader("Meldingen & Herinneringen")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAlarms() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Alarm, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Herinneringen & Alarmen", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Stel meet- en medicatieherinneringen in", fontSize = 12.sp, color = InkSoft)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }
                }
            }

            // 4. Gegevensbeheer & Back-up
            SettingsCategoryHeader("Gegevensbeheer & Back-up")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Volledige Back-up Maken
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onCreateBackup() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Volledige Back-up Maken", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Sla al je metingen en medicatie op in Downloads/Bloeddruk", fontSize = 12.sp, color = InkSoft)
                        }
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                    // Back-up Herstellen
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { restoreLauncher.launch(arrayOf("application/json", "*/*")) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Back-up Herstellen", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Herstel metingen en medicatie vanuit een back-up bestand", fontSize = 12.sp, color = InkSoft)
                        }
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                    // Automatische maand-archivering
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Automatische Maand-archivering", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Sla maandrapport automatisch op aan het einde van de maand", fontSize = 12.sp, color = InkSoft)
                        }
                        Switch(
                            checked = userProfile.autoArchive,
                            onCheckedChange = { onSaveProfile(userProfile.copy(autoArchive = it)) }
                        )
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                    // Back-up & Exporteren PDF/CSV
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenExport() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.FolderZip, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Exporteer PDF / CSV", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Exporteer maandrapporten of CSV voor Excel", fontSize = 12.sp, color = InkSoft)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showThresholdsDialog) {
        ThresholdsDialog(
            userProfile = userProfile,
            onSave = { updated ->
                onSaveProfile(updated)
                showThresholdsDialog = false
            },
            onDismiss = { showThresholdsDialog = false }
        )
    }
}

@Composable
private fun SettingsCategoryHeader(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = InkSoft,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

/** Dialog om bloeddruk streefwaarden handmatig aan te passen (in overleg met de arts) */
@Composable
private fun ThresholdsDialog(
    userProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var sysNormal by remember { mutableStateOf(userProfile.sysNormalMax.toString()) }
    var diaNormal by remember { mutableStateOf(userProfile.diaNormalMax.toString()) }
    var sysElevated by remember { mutableStateOf(userProfile.sysElevatedMax.toString()) }
    var diaElevated by remember { mutableStateOf(userProfile.diaElevatedMax.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Streefwaarden Aanpassen", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Pas de drempelwaarden voor bloeddrukcategorieën aan (in overleg met je arts).",
                    fontSize = 12.sp,
                    color = InkSoft
                )

                Text("Maximale waarden voor 'Normaal':", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sysNormal,
                        onValueChange = { sysNormal = it },
                        label = { Text("Bovendruk (mmHg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diaNormal,
                        onValueChange = { diaNormal = it },
                        label = { Text("Onderdruk (mmHg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Maximale waarden voor 'Licht verhoogd':", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sysElevated,
                        onValueChange = { sysElevated = it },
                        label = { Text("Bovendruk (mmHg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diaElevated,
                        onValueChange = { diaElevated = it },
                        label = { Text("Onderdruk (mmHg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        userProfile.copy(
                            sysNormalMax = sysNormal.toIntOrNull() ?: 120,
                            diaNormalMax = diaNormal.toIntOrNull() ?: 80,
                            sysElevatedMax = sysElevated.toIntOrNull() ?: 139,
                            diaElevatedMax = diaElevated.toIntOrNull() ?: 89
                        )
                    )
                }
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren")
            }
        }
    )
}
