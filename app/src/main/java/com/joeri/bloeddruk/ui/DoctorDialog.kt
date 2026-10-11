package com.joeri.bloeddruk.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.joeri.bloeddruk.data.UserProfile

/** Dialog om de contactgegevens van de dokter / behandelend arts in te vullen */
@Composable
fun DoctorDialog(
    profile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(profile.doctorName) }
    var phone by remember { mutableStateOf(profile.doctorPhone) }
    var email by remember { mutableStateOf(profile.doctorEmail) }
    var address by remember { mutableStateOf(profile.doctorAddress) }
    var notes by remember { mutableStateOf(profile.doctorNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dokter & Zorgverlener", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Vul hier de contactgegevens van je huisarts of behandelend arts in.",
                    fontSize = 13.sp,
                    color = InkSoft
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam dokter / Praktijk") },
                    placeholder = { Text("bv. Dr. Janssens") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefoonnummer") },
                        placeholder = { Text("bv. 012 34 56 78") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (phone.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Filled.Call, contentDescription = "Bellen", tint = Primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mailadres") },
                    placeholder = { Text("bv. dokter@praktijk.be") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adres / Praktijk") },
                    placeholder = { Text("bv. Medisch Centrum Hasselt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notities / Spreekuur") },
                    placeholder = { Text("bv. Telefonisch bereikbaar tussen 8u en 10u") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            doctorName = name.trim(),
                            doctorPhone = phone.trim(),
                            doctorEmail = email.trim(),
                            doctorAddress = address.trim(),
                            doctorNotes = notes.trim()
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Sluiten")
            }
        }
    )
}

/** Dialog om de eigen bloedgroep te selecteren */
@Composable
fun BloodTypeDialog(
    profile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(profile.bloodType) }
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Onbekend")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mijn Bloedgroep", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Selecteer je bloedgroep om deze snel bij de hand te hebben.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                bloodTypes.chunked(3).forEach { rowTypes ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTypes.forEach { type ->
                            val isSelected = selectedType.equals(type, ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Primary else Surface
                                ),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isSelected) Color.White else Ink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(profile.copy(bloodType = selectedType))
                    onDismiss()
                }
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Sluiten")
            }
        }
    )
}
