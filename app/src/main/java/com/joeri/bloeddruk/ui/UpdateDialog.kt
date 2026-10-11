package com.joeri.bloeddruk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joeri.bloeddruk.update.AppUpdater
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    initialUrl: String = "https://github.com/jokke1987/bloeddruk/releases/latest/download/app-debug.apk",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apkUrl by remember(initialUrl) {
        mutableStateOf(initialUrl)
    }
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canInstall by remember { mutableStateOf(AppUpdater.canInstallApk(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Update", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Download de nieuwste versie van de app (APK) rechtstreeks vanaf GitHub Releases.",
                    fontSize = 13.sp,
                    color = InkSoft
                )

                if (!canInstall) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4DC))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Toestemming vereist: 'Onbekende apps installeren' moet worden ingeschakeld om de update te starten.",
                                fontSize = 12.sp,
                                color = Ink
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    AppUpdater.requestInstallPermission(context)
                                    canInstall = AppUpdater.canInstallApk(context)
                                }
                            ) {
                                Text("Inschakelen", fontSize = 12.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = apkUrl,
                    onValueChange = { apkUrl = it },
                    label = { Text("APK Download URL") },
                    singleLine = true,
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isDownloading) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            "Downloaden... $progress%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = Color(0xFFE53935),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isDownloading && apkUrl.isNotBlank(),
                onClick = {
                    if (!AppUpdater.canInstallApk(context)) {
                        AppUpdater.requestInstallPermission(context)
                        return@Button
                    }
                    isDownloading = true
                    errorMessage = null
                    progress = 0

                    scope.launch {
                        val result = AppUpdater.downloadAndInstallApk(context, apkUrl.trim()) { p ->
                            progress = p
                        }
                        isDownloading = false
                        if (result.isFailure) {
                            errorMessage = "Download mislukt: ${result.exceptionOrNull()?.message}"
                        }
                    }
                }
            ) {
                Text(if (isDownloading) "Downloaden..." else "Update Starten")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading
            ) {
                Text("Sluiten")
            }
        }
    )
}
