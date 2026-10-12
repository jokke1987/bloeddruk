package com.joeri.bloeddruk.data

import android.content.Context
import com.joeri.bloeddruk.export.Exporter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class BackupData(
    val entries: List<BloodPressureEntry>,
    val medications: List<MedicationItem>,
    val userProfile: UserProfile
)

object BackupManager {

    /** Exporteert alle metingen, medicatie en profielgegevens naar een overzichtelijk JSON back-up bestand */
    fun createBackupJson(
        entries: List<BloodPressureEntry>,
        medications: List<MedicationItem>,
        userProfile: UserProfile
    ): String {
        val root = JSONObject()

        // 1. Metingen
        val entriesArray = JSONArray()
        entries.forEach { e ->
            val obj = JSONObject().apply {
                put("timestamp", e.timestamp)
                put("isMorning", e.isMorning)
                put("systolic", e.systolic)
                put("diastolic", e.diastolic)
                put("pulse", e.pulse)
                put("note", e.note)
                put("tags", e.tags)
            }
            entriesArray.put(obj)
        }
        root.put("entries", entriesArray)

        // 2. Medicatie
        val medArray = JSONArray()
        medications.forEach { m ->
            val obj = JSONObject().apply {
                put("name", m.name)
                put("dosage", m.dosage)
                put("slot", m.slot)
                put("note", m.note)
                put("purpose", m.purpose)
            }
            medArray.put(obj)
        }
        root.put("medications", medArray)

        // 3. Profiel & Instellingen
        val profileObj = JSONObject().apply {
            put("bloodType", userProfile.bloodType)
            put("doctorName", userProfile.doctorName)
            put("doctorPhone", userProfile.doctorPhone)
            put("doctorEmail", userProfile.doctorEmail)
            put("doctorAddress", userProfile.doctorAddress)
            put("doctorNotes", userProfile.doctorNotes)
            put("emergencyContact", userProfile.emergencyContact)
            put("isDarkMode", userProfile.isDarkMode)
            put("autoArchive", userProfile.autoArchive)
            put("sysNormalMax", userProfile.sysNormalMax)
            put("diaNormalMax", userProfile.diaNormalMax)
            put("sysElevatedMax", userProfile.sysElevatedMax)
            put("diaElevatedMax", userProfile.diaElevatedMax)
        }
        root.put("profile", profileObj)

        return root.toString(2)
    }

    /** Bewaart het back-up bestand in Downloads/Bloeddruk */
    fun saveBackupToFile(context: Context, jsonString: String): File {
        val dir = File(context.cacheDir, "export").apply { mkdirs() }
        val file = File(dir, "bloeddruk_backup.json")
        file.writeText(jsonString, Charsets.UTF_8)
        Exporter.saveCopy(context, file, "application/json")
        return file
    }

    /** Lees en herstel back-up gegevens vanuit een JSON String */
    fun parseBackupJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        // 1. Metingen
        val entries = mutableListOf<BloodPressureEntry>()
        val entriesArray = root.optJSONArray("entries")
        if (entriesArray != null) {
            for (i in 0 until entriesArray.length()) {
                val obj = entriesArray.getJSONObject(i)
                entries.add(
                    BloodPressureEntry(
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isMorning = obj.optBoolean("isMorning", true),
                        systolic = obj.optInt("systolic", 120),
                        diastolic = obj.optInt("diastolic", 80),
                        pulse = obj.optInt("pulse", 70),
                        note = obj.optString("note", ""),
                        tags = obj.optString("tags", "")
                    )
                )
            }
        }

        // 2. Medicatie
        val medications = mutableListOf<MedicationItem>()
        val medArray = root.optJSONArray("medications")
        if (medArray != null) {
            for (i in 0 until medArray.length()) {
                val obj = medArray.getJSONObject(i)
                medications.add(
                    MedicationItem(
                        name = obj.optString("name", ""),
                        dosage = obj.optString("dosage", ""),
                        slot = obj.optString("slot", "MORNING"),
                        note = obj.optString("note", ""),
                        purpose = obj.optString("purpose", "")
                    )
                )
            }
        }

        // 3. Profiel
        var profile = UserProfile()
        val profileObj = root.optJSONObject("profile")
        if (profileObj != null) {
            profile = UserProfile(
                bloodType = profileObj.optString("bloodType", ""),
                doctorName = profileObj.optString("doctorName", ""),
                doctorPhone = profileObj.optString("doctorPhone", ""),
                doctorEmail = profileObj.optString("doctorEmail", ""),
                doctorAddress = profileObj.optString("doctorAddress", ""),
                doctorNotes = profileObj.optString("doctorNotes", ""),
                emergencyContact = profileObj.optString("emergencyContact", ""),
                isDarkMode = profileObj.optBoolean("isDarkMode", false),
                autoArchive = profileObj.optBoolean("autoArchive", true),
                sysNormalMax = profileObj.optInt("sysNormalMax", 120),
                diaNormalMax = profileObj.optInt("diaNormalMax", 80),
                sysElevatedMax = profileObj.optInt("sysElevatedMax", 139),
                diaElevatedMax = profileObj.optInt("diaElevatedMax", 89)
            )
        }

        return BackupData(entries, medications, profile)
    }
}
