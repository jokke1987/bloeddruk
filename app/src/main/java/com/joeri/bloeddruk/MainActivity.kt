package com.joeri.bloeddruk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joeri.bloeddruk.alarm.AlarmPlayer
import com.joeri.bloeddruk.data.BackupManager
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.export.Exporter
import com.joeri.bloeddruk.ui.AlarmsScreen
import com.joeri.bloeddruk.ui.ArchiveScreen
import com.joeri.bloeddruk.ui.BloeddrukTheme
import com.joeri.bloeddruk.ui.EntrySheet
import com.joeri.bloeddruk.ui.ExportAction
import com.google.firebase.auth.FirebaseAuth
import com.joeri.bloeddruk.ui.ExportDialog
import com.joeri.bloeddruk.ui.LoginScreen
import com.joeri.bloeddruk.ui.MeasurementDetailDialog
import com.joeri.bloeddruk.ui.MedicationScreen
import com.joeri.bloeddruk.ui.OverviewScreen
import com.joeri.bloeddruk.ui.SearchScreen
import com.joeri.bloeddruk.ui.BloodTypeDialog
import com.joeri.bloeddruk.ui.DoctorDialog
import com.joeri.bloeddruk.ui.SettingsScreen
import com.joeri.bloeddruk.ui.SplashScreen
import com.joeri.bloeddruk.ui.UpdateDialog
import com.joeri.bloeddruk.update.AppUpdater
import com.joeri.bloeddruk.update.GitHubReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        AlarmPlayer.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val userProfile by vm.userProfile.collectAsStateWithLifecycle()

            BloeddrukTheme(isDarkMode = userProfile.isDarkMode) {
                val entries by vm.entries.collectAsStateWithLifecycle()
                val alarms by vm.alarms.collectAsStateWithLifecycle()
                val medications by vm.medications.collectAsStateWithLifecycle()
                val medicationLogs by vm.todayMedicationLogs.collectAsStateWithLifecycle()
                val context = LocalContext.current

                var showSplash by remember { mutableStateOf(true) }
                var activeScreen by rememberSaveable { mutableStateOf("overview") } // overview, alarms, medication, archive, search
                var medicationInitialTab by rememberSaveable { mutableIntStateOf(0) }
                var sheetOpen by rememberSaveable { mutableStateOf(false) }
                var showExport by rememberSaveable { mutableStateOf(false) }
                var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
                var showDoctorDialog by rememberSaveable { mutableStateOf(false) }
                var showBloodTypeDialog by rememberSaveable { mutableStateOf(false) }
                var editing by remember { mutableStateOf<BloodPressureEntry?>(null) }
                var selectedDetailEntry by remember { mutableStateOf<BloodPressureEntry?>(null) }

                val snackbar = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    // Terugval voor "Opslaan": de gebruiker kiest zelf een plek (bestandskiezer).
                    var pendingSave by remember { mutableStateOf<File?>(null) }
                    val saveLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val target = result.data?.data
                        val file = pendingSave
                        pendingSave = null
                        if (result.resultCode == Activity.RESULT_OK && target != null && file != null) {
                            scope.launch {
                                val ok = withContext(Dispatchers.IO) { Exporter.copyTo(context, file, target) }
                                snackbar.showSnackbar(if (ok) "Bestand opgeslagen" else "Opslaan is mislukt")
                            }
                        }
                    }

                    // Toestemming voor meldingen (Android 13+)
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Automatische achtergrondcontrole op GitHub Releases
                    var updateReleaseInfo by remember { mutableStateOf<GitHubReleaseInfo?>(null) }
                    var updateDownloadUrl by remember { mutableStateOf("https://github.com/jokke1987/bloeddruk/releases/latest/download/app-debug.apk") }
                    var hasUpdateAvailable by rememberSaveable { mutableStateOf(false) }

                    // Wekelijkse Automatische Lokale Back-up
                    LaunchedEffect(entries, medications, userProfile) {
                        if (entries.isNotEmpty()) {
                            val prefs = context.getSharedPreferences("bloeddruk_prefs", android.content.Context.MODE_PRIVATE)
                            val lastBackup = prefs.getLong("last_weekly_backup", 0L)
                            val now = System.currentTimeMillis()
                            if (now - lastBackup >= 7 * 86_400_000L) {
                                val json = BackupManager.createBackupJson(entries, medications, userProfile)
                                BackupManager.saveBackupToFile(context, json)
                                prefs.edit().putLong("last_weekly_backup", now).apply()
                            }
                        }
                    }

                    // Automatische GitHub Privé Synchronisatie op opstart (Pull)
                    LaunchedEffect(Unit) {
                        val (token, repo) = BackupManager.getGitHubCredentials(context)
                        if (token.isNotBlank() && repo.isNotBlank()) {
                            val remoteData = BackupManager.downloadFromGitHub(token, repo)
                            if (remoteData != null && remoteData.entries.isNotEmpty()) {
                                vm.restoreBackup(remoteData)
                            }
                        }
                    }

                    // Automatische GitHub Privé Synchronisatie bij wijzigingen (Push)
                    LaunchedEffect(entries.size, medications.size, userProfile) {
                        val (token, repo) = BackupManager.getGitHubCredentials(context)
                        if (token.isNotBlank() && repo.isNotBlank() && entries.isNotEmpty()) {
                            val json = BackupManager.createBackupJson(entries, medications, userProfile)
                            BackupManager.uploadBackupToGitHub(token, repo, json)
                        }
                    }

                    LaunchedEffect(Unit) {
                        val release = AppUpdater.checkForUpdates(context)
                        if (release != null) {
                            updateReleaseInfo = release
                            updateDownloadUrl = release.downloadUrl
                            hasUpdateAvailable = true
                        }
                    }

                    BackHandler(enabled = activeScreen != "overview") { activeScreen = "overview" }

                    when (activeScreen) {
                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { userId ->
                                    activeScreen = "overview"
                                    scope.launch { snackbar.showSnackbar("Succesvol ingelogd bij Firebase!") }
                                },
                                onSkipLogin = {
                                    activeScreen = "overview"
                                }
                            )
                        }
                        "alarms" -> {
                            AlarmsScreen(
                                alarms = alarms,
                                onChange = vm::updateAlarm,
                                onExactPermissionChanged = vm::rescheduleAll,
                                onBack = { activeScreen = "overview" }
                            )
                        }
                        "medication" -> {
                            MedicationScreen(
                                medications = medications,
                                medicationLogs = medicationLogs,
                                onSave = vm::saveMedication,
                                onDelete = vm::deleteMedication,
                                onLogIntake = { med, slot, status -> vm.logMedicationIntake(med, slot, status) },
                                onUndoIntake = { med, slot -> vm.undoMedicationIntake(med, slot) },
                                onBack = { activeScreen = "overview" }
                            )
                        }
                        "archive" -> {
                            ArchiveScreen(
                                entries = entries,
                                onBack = { activeScreen = "overview" }
                            )
                        }
                        "search" -> {
                            SearchScreen(
                                entries = entries,
                                onEdit = {
                                    editing = it
                                    sheetOpen = true
                                },
                                onBack = { activeScreen = "overview" }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                userProfile = userProfile,
                                onSaveProfile = vm::saveProfile,
                                onOpenAlarms = { activeScreen = "alarms" },
                                onOpenDoctor = { showDoctorDialog = true },
                                onOpenExport = { showExport = true },
                                onCreateBackup = {
                                    val json = BackupManager.createBackupJson(entries, medications, userProfile)
                                    BackupManager.saveBackupToFile(context, json)
                                    scope.launch { snackbar.showSnackbar("Back-up opgeslagen in Downloads/Bloeddruk") }
                                },
                                onRestoreBackupUri = { uri ->
                                    scope.launch {
                                        try {
                                            val text = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                                            if (text != null) {
                                                val data = BackupManager.parseFileOrJsonOrCsv(text)
                                                vm.restoreBackup(data)
                                                snackbar.showSnackbar("Back-up hersteld (${data.entries.size} metingen)!")
                                            }
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Herstellen mislukt: ongeldig bestand")
                                        }
                                    }
                                },
                                onUploadGitHubBackup = { token, repo ->
                                    scope.launch {
                                        val json = BackupManager.createBackupJson(entries, medications, userProfile)
                                        val ok = BackupManager.uploadBackupToGitHub(token, repo, json)
                                        snackbar.showSnackbar(if (ok) "Back-up geüpload naar GitHub!" else "GitHub upload mislukt: controleer PAT en repo")
                                    }
                                },
                                onBack = { activeScreen = "overview" }
                            )
                        }
                        else -> {
                            OverviewScreen(
                                entries = entries,
                                medications = medications,
                                userProfile = userProfile,
                                hasUpdateAvailable = hasUpdateAvailable,
                                snackbarHostState = snackbar,
                                onAdd = { editing = null; sheetOpen = true },
                                onEdit = { selectedDetailEntry = it },
                                onOpenAlarms = { activeScreen = "alarms" },
                                onOpenMedication = { tab ->
                                    medicationInitialTab = tab
                                    activeScreen = "medication"
                                },
                                onOpenArchive = { activeScreen = "archive" },
                                onOpenSearch = { activeScreen = "search" },
                                onOpenDoctor = { showDoctorDialog = true },
                                onOpenBloodType = { showBloodTypeDialog = true },
                                onOpenSettings = { activeScreen = "settings" },
                                onSyncGitHub = {
                                    val (token, repo) = BackupManager.getGitHubCredentials(context)
                                    if (token.isBlank() || repo.isBlank()) {
                                        scope.launch { snackbar.showSnackbar("Koppel eerst je GitHub account in Instellingen ⚙️") }
                                    } else {
                                        scope.launch {
                                            val remoteData = BackupManager.downloadFromGitHub(token, repo)
                                            if (remoteData != null && remoteData.entries.isNotEmpty()) {
                                                vm.restoreBackup(remoteData)
                                            }
                                            val json = BackupManager.createBackupJson(entries, medications, userProfile)
                                            val ok = BackupManager.uploadBackupToGitHub(token, repo, json)
                                            snackbar.showSnackbar(if (ok) "Metingen gesynchroniseerd met GitHub ✓" else "Synchronisatie mislukt")
                                        }
                                    }
                                },
                                onCreateBackup = {
                                    val json = BackupManager.createBackupJson(entries, medications, userProfile)
                                    BackupManager.saveBackupToFile(context, json)
                                    scope.launch { snackbar.showSnackbar("Back-up opgeslagen in Downloads/Bloeddruk") }
                                },
                                onRestoreBackupUri = { uri ->
                                    scope.launch {
                                        try {
                                            val text = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                                            if (text != null) {
                                                val data = BackupManager.parseFileOrJsonOrCsv(text)
                                                vm.restoreBackup(data)
                                                snackbar.showSnackbar("Back-up hersteld (${data.entries.size} metingen)!")
                                            }
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Herstellen mislukt: ongeldig bestand")
                                        }
                                    }
                                },
                                onCheckForUpdates = { showUpdateDialog = true },
                                onExport = { showExport = true }
                            )
                        }
                    }

                    if (showUpdateDialog) {
                        UpdateDialog(
                            initialUrl = updateDownloadUrl,
                            onDismiss = { showUpdateDialog = false }
                        )
                    }

                    if (showDoctorDialog) {
                        DoctorDialog(
                            profile = userProfile,
                            onSave = {
                                vm.saveProfile(it)
                                scope.launch { snackbar.showSnackbar("Doktergegevens opgeslagen") }
                            },
                            onDismiss = { showDoctorDialog = false }
                        )
                    }

                    if (showBloodTypeDialog) {
                        BloodTypeDialog(
                            profile = userProfile,
                            onSave = {
                                vm.saveProfile(it)
                                scope.launch { snackbar.showSnackbar("Bloedgroep opgeslagen") }
                            },
                            onDismiss = { showBloodTypeDialog = false }
                        )
                    }

                    if (selectedDetailEntry != null) {
                        MeasurementDetailDialog(
                            entry = selectedDetailEntry!!,
                            onEdit = { e ->
                                selectedDetailEntry = null
                                editing = e
                                sheetOpen = true
                            },
                            onDelete = { e ->
                                selectedDetailEntry = null
                                vm.delete(e)
                                scope.launch { snackbar.showSnackbar("Meting verwijderd") }
                            },
                            onDismiss = { selectedDetailEntry = null }
                        )
                    }

                    if (sheetOpen) {
                        EntrySheet(
                            initial = editing,
                            onDismiss = { sheetOpen = false },
                            onSave = {
                                vm.save(it)
                                sheetOpen = false
                                scope.launch { snackbar.showSnackbar("Meting opgeslagen") }
                            },
                            onDelete = {
                                vm.delete(it)
                                sheetOpen = false
                                scope.launch { snackbar.showSnackbar("Meting verwijderd") }
                            }
                        )
                    }

                    if (showExport) {
                        ExportDialog(
                            onDismiss = { showExport = false },
                            onExport = { days, pdf, action ->
                                showExport = false
                                val list = if (days == null) {
                                    entries
                                } else {
                                    val cutoff = System.currentTimeMillis() - days * 86_400_000L
                                    entries.filter { it.timestamp >= cutoff }
                                }
                                scope.launch {
                                    if (list.isEmpty()) {
                                        snackbar.showSnackbar("Geen metingen in deze periode")
                                    } else {
                                        try {
                                            val mime = if (pdf) "application/pdf" else "text/csv"
                                            val file = withContext(Dispatchers.IO) {
                                                if (pdf) Exporter.exportPdf(context, list, userProfile)
                                                else Exporter.exportCsv(context, list)
                                            }
                                            when (action) {
                                                ExportAction.OPEN -> {
                                                    if (!Exporter.open(context, file, mime)) {
                                                        snackbar.showSnackbar(
                                                            "Geen app gevonden om dit bestand te openen. Kies Opslaan."
                                                        )
                                                    }
                                                }
                                                ExportAction.SHARE -> {
                                                    Exporter.share(
                                                        context, Exporter.uriFor(context, file), mime, "Bloeddruk"
                                                    )
                                                }
                                                ExportAction.SAVE -> {
                                                    val saved = withContext(Dispatchers.IO) {
                                                        Exporter.saveCopy(context, file, mime)
                                                    }
                                                    if (saved) {
                                                        val r = snackbar.showSnackbar(
                                                            "Opgeslagen in Downloads/Bloeddruk",
                                                            actionLabel = "Openen",
                                                            duration = SnackbarDuration.Long
                                                        )
                                                        if (r == SnackbarResult.ActionPerformed) {
                                                            Exporter.open(context, file, mime)
                                                        }
                                                    } else {
                                                        // Oudere Android of mislukt: laat de gebruiker zelf een plek kiezen.
                                                        pendingSave = file
                                                        saveLauncher.launch(
                                                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                                                addCategory(Intent.CATEGORY_OPENABLE)
                                                                type = mime
                                                                putExtra(Intent.EXTRA_TITLE, file.name)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Exporteren is mislukt")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
