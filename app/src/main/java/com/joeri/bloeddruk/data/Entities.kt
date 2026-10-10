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
