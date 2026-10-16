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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phonelink
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    onOpenTrends: () -> Unit = {},
    onCreateBackup: () -> Unit,
    onRestoreBackupUri: (Uri) -> Unit,
    onUploadGitHubBackup: (token: String, repo: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val (savedToken, savedRepo) = remember { com.joeri.bloeddruk.data.BackupManager.getGitHubCredentials(context) }
    var showThresholdsDialog by remember { mutableStateOf(false) }
    var githubRepo by rememberSaveable { mutableStateOf(savedRepo) }
    var githubToken by rememberSaveable { mutableStateOf(savedToken) }

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
                title = { Text("INSTELLINGEN", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
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
            // 1. Account & Profiel
            SettingsCategoryHeader("Account & Profiel")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDoctor() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Account, Huisarts & Bloedgroep", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                if (userProfile.doctorName.isNotBlank()) "Dokter: ${userProfile.doctorName}"
                                else "Voer profiel- en doktersgegevens in",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

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

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

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

            // 2. Meldingen & Wekker
            SettingsCategoryHeader("Meldingen & Wekker")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAlarms() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Alarm, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Ochtend & Avond Reminders", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Herinneringen en wekkergeluiden instellen", fontSize = 12.sp, color = InkSoft)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }
                }
            }

            // 3. Apparaten Koppelen & GitHub Sync
            SettingsCategoryHeader("Apparaten Koppelen & Synchronisatie")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phonelink, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Privé GitHub Back-up & Sync", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Text(
                        "Koppel je apparaten veilig via een privé GitHub repository.",
                        fontSize = 12.sp,
                        color = InkSoft
                    )

                    OutlinedTextField(
                        value = githubRepo,
                        onValueChange = { githubRepo = it },
                        label = { Text("GitHub Repository (eigenaar/repo)") },
                        placeholder = { Text("bv. jokke1987/bloeddruk-backup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = githubToken,
                        onValueChange = { githubToken = it },
                        label = { Text("Personal Access Token (PAT)") },
                        placeholder = { Text("ghp_xxxxxxxxxxxxxx") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        enabled = githubRepo.isNotBlank() && githubToken.isNotBlank(),
                        onClick = {
                            com.joeri.bloeddruk.data.BackupManager.saveGitHubCredentials(context, githubToken, githubRepo)
                            onUploadGitHubBackup(githubToken, githubRepo)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Koppelen & Uploaden naar GitHub")
                    }
                }
            }

            // 4. Data-export & Back-up
            SettingsCategoryHeader("Data-export & Back-up")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { restoreLauncher.launch(arrayOf("*/*")) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Back-up Herstellen (CSV of JSON)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Herstel metingen en medicatie vanuit een CSV- of JSON-bestand", fontSize = 12.sp, color = InkSoft)
                        }
                    }

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

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

                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTrends() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Analytics, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Trends & Historische Analyse", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Bekijk maandelijkse trends, dag vs nacht en variatie", fontSize = 12.sp, color = InkSoft)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InkSoft)
                    }
                }
            }

            // 5. App Info
            SettingsCategoryHeader("App Info")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Bloeddruk Assistent Pro", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Versie 1.0.1 (Build 2)", fontSize = 12.sp, color = InkSoft)
                            }
                        }
                    }
                    HorizontalDivider(color = InkSoft.copy(alpha = 0.15f))
                    Text(
                        "Created by Wierinckx Joeri 10/2026",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkSoft,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Created by Wierinckx Joeri 10/2026",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkSoft
                )
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
