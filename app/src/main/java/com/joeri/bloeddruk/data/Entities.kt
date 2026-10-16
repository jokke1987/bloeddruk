package com.joeri.bloeddruk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Eén bloeddrukmeting (ochtend of avond). */
@Entity(tableName = "metingen")
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Moment van meten, in milliseconden sinds epoch. */
    val timestamp: Long,
    /** true = Ochtend (AM), false = Avond (PM). */
    val isMorning: Boolean,
    /** Bovendruk (systolisch) in mmHg. */
    val systolic: Int,
    /** Onderdruk (diastolisch) in mmHg. */
    val diastolic: Int,
    /** Pols / hartslag in BPM. */
    val pulse: Int,
    /** Vrije notitie, bv. "na het sporten". */
    val note: String = "",
    /** Gekozen labels, gescheiden door komma's. */
    val tags: String = ""
)

/** Eén van de 4 instelbare alarmen (id 0..3). */
@Entity(tableName = "alarmen")
data class AlarmSetting(
    @PrimaryKey val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val isMorning: Boolean,
    val soundUri: String = "",
    val soundTitle: String = "Standaard beltoon"
)

/** Medicatie per dagdeel (MORNING, AFTERNOON, EVENING, NIGHT). */
@Entity(tableName = "medicatie")
data class MedicationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val slot: String = "MORNING", // MORNING, AFTERNOON, EVENING, NIGHT
    val note: String = "",
    val purpose: String = "" // Waarvoor dient het (bv. "Voor hoge bloeddruk")
)

/** FASE 3: Registratie van daadwerkelijke medicatie-inname per dag en dagdeel. */
@Entity(tableName = "medicatie_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dateStr: String, // "YYYY-MM-DD" voor snelle dagelijkse opzoeking
    val slot: String,    // "MORNING", "AFTERNOON", "EVENING", "NIGHT"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "TAKEN" // "TAKEN" of "SKIPPED"
)

/** Medisch profiel en instellingen van de gebruiker. */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val bloodType: String = "",       // e.g. "A+", "O-", etc.
    val doctorName: String = "",      // e.g. "Dr. Janssens"
    val doctorPhone: String = "",     // e.g. "0123456789"
    val doctorEmail: String = "",     // e.g. "dokter@praktijk.be"
    val doctorAddress: String = "",   // e.g. "Medisch Centrum"
    val doctorNotes: String = "",     // e.g. "Spreekuur op dinsdag"
    val emergencyContact: String = "",// e.g. "Partner: 0471234567"
    val isDarkMode: Boolean = false,  // Donkere modus aan/uit
    val autoArchive: Boolean = true,  // Automatische maand-archivering
    val sysNormalMax: Int = 120,      // Normaal bovendruk bovengrens
    val diaNormalMax: Int = 80,       // Normaal onderdruk bovengrens
    val sysElevatedMax: Int = 139,    // Licht verhoogd bovendruk bovengrens
    val diaElevatedMax: Int = 89      // Licht verhoogd onderdruk bovengrens
)
