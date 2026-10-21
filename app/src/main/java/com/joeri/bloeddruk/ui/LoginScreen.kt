package com.joeri.bloeddruk.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

/**
 * FASE 5: Firebase Authenticatie Inlogscherm met E-mail en Wachtwoord.
 * Volgt de rustige, lichte stijl met afgeronde witte kaarten en blauwe accenten.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (userId: String) -> Unit,
    onSkipLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Brand Header Logo & Titel
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = "Logo",
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    "Bloeddruk Assistent",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightInk
                )

                Text(
                    if (isSignUpMode) "Maak een nieuw account aan om je metingen veilig in de cloud te bewaren."
                    else "Log in om je bloeddrukmetingen en rapporten synchroon te houden op al je apparaten.",
                    fontSize = 13.5.sp,
                    color = LightInkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Witte Inlogkaart (ElevatedCard / Card)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            if (isSignUpMode) "Account Registreren" else "Inloggen bij Firebase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = LightInk
                        )

                        // Invoerveld E-mailadres
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text("E-mailadres") },
                            placeholder = { Text("bv. naam@voorbeeld.be") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null, tint = Primary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Invoerveld Wachtwoord met Oogje (Visibility Toggle)
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Wachtwoord") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Primary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Wachtwoord verbergen" else "Wachtwoord tonen",
                                        tint = LightInkSoft
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Foutmelding weergave
                        errorMessage?.let { msg ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusErrorText.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    msg,
                                    color = StatusErrorText,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Primaire Knop "Inloggen" / "Registreren"
                        Button(
                            enabled = !isLoggingIn && email.isNotBlank() && password.isNotBlank(),
                            onClick = {
                                isLoggingIn = true
                                errorMessage = null
                                val auth = FirebaseAuth.getInstance()

                                if (isSignUpMode) {
                                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoggingIn = false
                                            val userId = result.user?.uid ?: ""
                                            onLoginSuccess(userId)
                                        }
                                        .addOnFailureListener { e ->
                                            isLoggingIn = false
                                            errorMessage = "Registratie mislukt: ${e.localizedMessage ?: "controleer de gegevens"}"
                                        }
                                } else {
                                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoggingIn = false
                                            val userId = result.user?.uid ?: ""
                                            onLoginSuccess(userId)
                                        }
                                        .addOnFailureListener { e ->
                                            isLoggingIn = false
                                            errorMessage = "Inloggen mislukt: controleer je e-mailadres en wachtwoord."
                                        }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Verifiëren...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                Text(
                                    if (isSignUpMode) "Account Aanmaken" else "Inloggen",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Schakelen tussen Inloggen en Account Aanmaken
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (isSignUpMode) "Heb je al een account?" else "Nog geen account?",
                        fontSize = 13.sp,
                        color = LightInkSoft
                    )
                    TextButton(onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = null
                    }) {
                        Text(
                            if (isSignUpMode) "Inloggen" else "Registreren",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                // Optioneel: Lokaal verdergaan zonder inloggen
                TextButton(onClick = onSkipLogin) {
                    Text("Lokaal verdergaan zonder inloggen", fontSize = 12.sp, color = LightInkSoft)
                }
            }
        }
    }
}
