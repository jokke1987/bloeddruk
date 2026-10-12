package com.joeri.bloeddruk.data

import android.content.Context
import com.joeri.bloeddruk.export.Exporter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    /** Slimme herstel-functie voor zowel JSON back-up bestanden als CSV bestanden */
    fun parseFileOrJsonOrCsv(text: String): BackupData {
        return try {
            if (text.trim().startsWith("{")) {
                parseBackupJson(text)
            } else {
                val entries = parseCsv(text)
                BackupData(entries, emptyList(), UserProfile())
            }
        } catch (e: Exception) {
            val entries = parseCsv(text)
            BackupData(entries, emptyList(), UserProfile())
        }
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

    /** Herstelt metingen direct vanuit een geëxporteerd CSV bestand */
    private fun parseCsv(csvText: String): List<BloodPressureEntry> {
        val entries = mutableListOf<BloodPressureEntry>()
        val lines = csvText.lines().map { it.trim().removePrefix("\uFEFF") }.filter { it.isNotBlank() }
        if (lines.size <= 1) return entries

        val dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        for (i in 1 until lines.size) {
            try {
                val cols = lines[i].split(";").map { it.removeSurrounding("\"").trim() }
                if (cols.size >= 6) {
                    val dateStr = cols[0]
                    val timeStr = cols[1]
                    val momentStr = cols[2]
                    val sys = cols[3].toIntOrNull() ?: 120
                    val dia = cols[4].toIntOrNull() ?: 80
                    val pulse = cols[5].toIntOrNull() ?: 70
                    val tags = if (cols.size >= 8) cols[7] else ""
                    val note = if (cols.size >= 9) cols[8] else ""

                    val dt = LocalDateTime.parse("$dateStr $timeStr", dateFmt)
                    val timestamp = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val isMorning = momentStr.equals("Ochtend", ignoreCase = true)

                    entries.add(
                        BloodPressureEntry(
                            timestamp = timestamp,
                            isMorning = isMorning,
                            systolic = sys,
                            diastolic = dia,
                            pulse = pulse,
                            note = note,
                            tags = tags
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return entries
    }
}
