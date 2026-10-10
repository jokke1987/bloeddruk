package com.joeri.bloeddruk.ui

import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.joeri.bloeddruk.data.AlarmSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    alarms: List<AlarmSetting>,
    onChange: (AlarmSetting) -> Unit,
    onExactPermissionChanged: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var canExact by remember { mutableStateOf(canScheduleExact(context)) }

    // Opnieuw controleren zodra de gebruiker terugkomt uit de systeeminstellingen.
    LifecycleResumeEffect(Unit) {
        val now = canScheduleExact(context)
        if (now && !canExact) onExactPermissionChanged()
        canExact = now
        onPauseOrDispose { }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Alarmen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!canExact) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4DC))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Voor alarmen op de exacte minuut moet je 'Alarmen en herinneringen' toestaan.",
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.padding(top = 10.dp)
                            ) { Text("Toestaan") }
                        }
                    }
                }
            }
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(alarm, onChange)
            }
            item {
                Text(
                    "Elk alarm herhaalt zich elke dag op het ingestelde tijdstip.",
                    color = InkSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AlarmCard(alarm: AlarmSetting, onChange: (AlarmSetting) -> Unit) {
    val context = LocalContext.current
    var showSoundDialog by remember { mutableStateOf(false) }

    // Titel: "Ochtendalarm 1/2" of "Avondalarm 1/2"
    val number = if (alarm.isMorning) alarm.id + 1 else alarm.id - 1
    val title = if (alarm.isMorning) "Ochtendalarm $number" else "Avondalarm $number"
    val accent = if (alarm.isMorning) Color(0xFFF2A93B) else Color(0xFF6C63B5)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (alarm.isMorning) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m -> onChange(alarm.copy(hour = h, minute = m, enabled = true)) },
                                alarm.hour, alarm.minute, true
                            ).show()
                        }
                ) {
                    Text(title, color = InkSoft, fontSize = 13.sp)
                    Text(
                        "${two(alarm.hour)}:${two(alarm.minute)}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light,
                        color = if (alarm.enabled) Ink else InkSoft
                    )
                }
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { onChange(alarm.copy(enabled = it)) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = InkSoft.copy(alpha = 0.15f)
            )

            // Klik op Alarmgeluid om het geluid te wijzigen
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showSoundDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = "Alarmgeluid",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Alarmgeluid", color = InkSoft, fontSize = 12.sp)
                    Text(
                        alarm.soundTitle.ifEmpty { "Standaard beltoon" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink
                    )
                }
            }
        }
    }

    if (showSoundDialog) {
        AlarmSoundDialog(
            alarm = alarm,
            onChange = onChange,
            onDismiss = { showSoundDialog = false }
        )
    }
}

@Composable
private fun AlarmSoundDialog(
    alarm: AlarmSetting,
    onChange: (AlarmSetting) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Launcher om eigen muziekbestand te kiezen (.mp3, .wav, m4a, etc.)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val title = getAudioTitle(context, uri)
            onChange(alarm.copy(soundUri = uri.toString(), soundTitle = title))
            onDismiss()
        }
    }

    // Launcher voor de standaard Android systeembeltoonkiezer
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(context, uri)
                val title = ringtone?.getTitle(context) ?: "Systeembeltoon"
                onChange(alarm.copy(soundUri = uri.toString(), soundTitle = title))
            } else {
                onChange(alarm.copy(soundUri = "", soundTitle = "Standaard beltoon"))
            }
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarmgeluid", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                // Plusje (+) rechtsboven om meteen een eigen bestand te kiezen
                IconButton(onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Eigen nummer kiezen (+)",
                        tint = Primary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Kies een beltoon of tik op het plusje (+) om je eigen nummer te kiezen.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Optie 1: Standaard beltoon
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChange(alarm.copy(soundUri = "", soundTitle = "Standaard beltoon"))
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = alarm.soundUri.isEmpty(),
                        onClick = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = InkSoft)
                    Spacer(Modifier.width(10.dp))
                    Text("Standaard beltoon", fontSize = 15.sp, color = Ink)
                }

                // Optie 2: Bellen / Systeembeltoon (Kies een standaardmelodie of beltoon)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Kies een beltoon")
                                if (alarm.soundUri.isNotEmpty()) {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(alarm.soundUri))
                                }
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Bellen / Systeembeltoon", fontSize = 15.sp, color = Ink, fontWeight = FontWeight.Medium)
                        Text("Kies een ingebouwde beltoon", fontSize = 12.sp, color = InkSoft)
                    }
                }

                // Optie 3: Eigen muziekbestand (Kies een liedje dat je zelf hebt gedownload)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            audioPickerLauncher.launch(arrayOf("audio/*"))
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Eigen muziekbestand (+)", fontSize = 15.sp, color = Ink, fontWeight = FontWeight.Medium)
                        Text("Kies een gedownload liedje of MP3", fontSize = 12.sp, color = InkSoft)
                    }
                }

                // Huidige gekozen nummer als dat een eigen nummertje of specifieke beltoon is
                if (alarm.soundUri.isNotEmpty()) {
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Gekozen: ${alarm.soundTitle}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Sluiten")
            }
        }
    )
}

private fun getAudioTitle(context: Context, uri: Uri): String {
    var title = "Eigen muziekbestand"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)
                if (!displayName.isNullOrBlank()) {
                    title = displayName
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return title
}

private fun canScheduleExact(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
