package com.joeri.bloeddruk.data

import android.content.Context
import com.joeri.bloeddruk.export.Exporter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

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

    /** Uploadt het back-up bestand direct naar een privé GitHub repository via een Personal Access Token */
    suspend fun uploadBackupToGitHub(
        token: String,
        repoOwnerRepo: String,
        jsonString: String
    ): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val cleanRepo = repoOwnerRepo.trim().removePrefix("https://github.com/").removeSuffix(".git").trim()
            if (!cleanRepo.contains("/")) return@withContext false

            val url = java.net.URL("https://api.github.com/repos/$cleanRepo/contents/bloeddruk_backup.json")
            val base64Content = android.util.Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

            var sha: String? = null
            try {
                val getConn = url.openConnection() as java.net.HttpURLConnection
                getConn.setRequestProperty("Authorization", "Bearer ${token.trim()}")
                getConn.setRequestProperty("Accept", "application/vnd.github+json")
                if (getConn.responseCode == 200) {
                    val resp = getConn.inputStream.bufferedReader().readText()
                    sha = JSONObject(resp).optString("sha")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val putConn = url.openConnection() as java.net.HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.setRequestProperty("Authorization", "Bearer ${token.trim()}")
            putConn.setRequestProperty("Content-Type", "application/json")
            putConn.doOutput = true

            val body = JSONObject().apply {
                put("message", "Automatische Bloeddruk Back-up ${System.currentTimeMillis()}")
                put("content", base64Content)
                if (!sha.isNullOrEmpty()) put("sha", sha)
            }

            putConn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = putConn.responseCode
            code == 200 || code == 201
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Downloadt en herstelt het back-up bestand vanuit een privé GitHub repository */
    suspend fun downloadFromGitHub(
        token: String,
        repoOwnerRepo: String
    ): BackupData? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val cleanRepo = repoOwnerRepo.trim().removePrefix("https://github.com/").removeSuffix(".git").trim()
            if (!cleanRepo.contains("/")) return@withContext null

            val url = java.net.URL("https://api.github.com/repos/$cleanRepo/contents/bloeddruk_backup.json")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer ${token.trim()}")
            conn.setRequestProperty("Accept", "application/vnd.github+json")

            if (conn.responseCode == 200) {
                val resp = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(resp)
                val base64Content = json.optString("content").replace("\n", "").replace("\r", "")
                val decodedBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT)
                val jsonText = String(decodedBytes, Charsets.UTF_8)
                parseBackupJson(jsonText)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getGitHubCredentials(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences("bloeddruk_prefs", Context.MODE_PRIVATE)
        return (prefs.getString("github_token", "") ?: "") to (prefs.getString("github_repo", "") ?: "")
    }

    fun saveGitHubCredentials(context: Context, token: String, repo: String) {
        val prefs = context.getSharedPreferences("bloeddruk_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("github_token", token.trim()).putString("github_repo", repo.trim()).apply()
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

    /** Herstelt metingen flexibel vanuit elk type geëxporteerd CSV bestand */
    private fun parseCsv(csvText: String): List<BloodPressureEntry> {
        val entries = mutableListOf<BloodPressureEntry>()
        val lines = csvText.lines()
            .map { it.trim().removePrefix("\uFEFF").removePrefix("\uFFFE") }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return entries

        // Detecteer scheidingsteken (; of , of tab)
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.contains(";") -> ";"
            firstLine.contains("\t") -> "\t"
            else -> ","
        }

        val headerCols = firstLine.split(delimiter).map { it.removeSurrounding("\"").trim().lowercase() }

        var dateIdx = headerCols.indexOfFirst { it.contains("datum") || it.contains("date") }
        var timeIdx = headerCols.indexOfFirst { it.contains("tijd") || it.contains("time") }
        var momentIdx = headerCols.indexOfFirst { it.contains("moment") }
        var sysIdx = headerCols.indexOfFirst { it.contains("bovendruk") || it.contains("sys") }
        var diaIdx = headerCols.indexOfFirst { it.contains("onderdruk") || it.contains("dia") }
        var pulseIdx = headerCols.indexOfFirst { it.contains("pols") || it.contains("pulse") || it.contains("bpm") }
        var tagsIdx = headerCols.indexOfFirst { it.contains("label") || it.contains("tag") }
        var noteIdx = headerCols.indexOfFirst { it.contains("notitie") || it.contains("note") }

        if (sysIdx == -1) sysIdx = 3
        if (diaIdx == -1) diaIdx = 4
        if (pulseIdx == -1) pulseIdx = 5
        if (dateIdx == -1) dateIdx = 0
        if (timeIdx == -1) timeIdx = 1

        val startLineIndex = if (headerCols.any { it.contains("bovendruk") || it.contains("datum") || it.contains("sys") }) 1 else 0

        for (i in startLineIndex until lines.size) {
            val line = lines[i]
            try {
                val cols = line.split(delimiter).map { it.removeSurrounding("\"").trim() }
                if (cols.size > sysIdx && cols.size > diaIdx) {
                    val sys = cols.getOrNull(sysIdx)?.toIntOrNull() ?: continue
                    val dia = cols.getOrNull(diaIdx)?.toIntOrNull() ?: continue
                    val pulse = cols.getOrNull(pulseIdx)?.toIntOrNull() ?: 70
                    val tags = if (tagsIdx != -1 && tagsIdx < cols.size) cols[tagsIdx] else ""
                    val note = if (noteIdx != -1 && noteIdx < cols.size) cols[noteIdx] else ""
                    val momentStr = if (momentIdx != -1 && momentIdx < cols.size) cols[momentIdx] else ""

                    val dateStr = cols.getOrNull(dateIdx) ?: ""
                    val timeStr = cols.getOrNull(timeIdx) ?: "12:00"

                    val timestamp = parseFlexibleTimestamp(dateStr, timeStr)
                    val isMorning = momentStr.contains("Ochtend", ignoreCase = true) || (timeStr.take(2).toIntOrNull() ?: 12) < 12

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

    private fun parseFlexibleTimestamp(dateStr: String, timeStr: String): Long {
        val cleanDate = dateStr.replace("/", "-").trim()
        val cleanTime = timeStr.trim().ifEmpty { "12:00" }

        val dateParts = cleanDate.split("-")
        var year = 2026
        var month = 10
        var day = 9

        if (dateParts.size == 3) {
            if (dateParts[0].length == 4) { // YYYY-MM-DD
                year = dateParts[0].toIntOrNull() ?: 2026
                month = dateParts[1].toIntOrNull() ?: 10
                day = dateParts[2].toIntOrNull() ?: 9
            } else { // DD-MM-YYYY of D-M-YYYY
                day = dateParts[0].toIntOrNull() ?: 9
                month = dateParts[1].toIntOrNull() ?: 10
                val yRaw = dateParts[2].toIntOrNull() ?: 2026
                year = if (yRaw < 100) 2000 + yRaw else yRaw
            }
        }

        val timeParts = cleanTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 12
        val minute = timeParts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

        return try {
            val dt = LocalDateTime.of(year, month.coerceIn(1, 12), day.coerceIn(1, 31), hour, minute)
            dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
