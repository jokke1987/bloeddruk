package com.joeri.bloeddruk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joeri.bloeddruk.alarm.AlarmScheduler
import com.joeri.bloeddruk.data.AlarmSetting
import com.joeri.bloeddruk.data.AppDatabase
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.data.MedicationItem
import com.joeri.bloeddruk.data.UserProfile
import com.joeri.bloeddruk.ui.toLocalDateTime
import com.joeri.bloeddruk.ui.toMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    val entries: StateFlow<List<BloodPressureEntry>> = db.bloodPressureDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alarms: StateFlow<List<AlarmSetting>> = db.alarmDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val medications: StateFlow<List<MedicationItem>> = db.medicationDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userProfile: StateFlow<UserProfile> = db.userProfileDao().observeProfile()
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    val todayStr = java.time.LocalDate.now().toString()
    val todayMedicationLogs: StateFlow<List<com.joeri.bloeddruk.data.MedicationLog>> = db.medicationLogDao().observeLogsForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // FASE 5: Start de real-time Firestore synchronisatie op het pad /users/{userId}/measurements
        com.joeri.bloeddruk.data.FirestoreSyncManager.startRealtimeSync(db, viewModelScope)

        // Zorg dat de 26 oorspronkelijke oktober-metingen aanwezig zijn in de database
        viewModelScope.launch {
            val allEntries = db.bloodPressureDao().getAllEntries()
            val hasOctober = allEntries.any { e ->
                val dt = e.timestamp.toLocalDateTime()
                dt.year == 2026 && dt.monthValue == 10
            }
            if (!hasOctober) {
                val seedList = listOf(
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 7, 14, 57).toMillis(), isMorning = true, systolic = 116, diastolic = 78, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 7, 21, 47).toMillis(), isMorning = false, systolic = 120, diastolic = 84, pulse = 67, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 8, 6, 36).toMillis(), isMorning = true, systolic = 128, diastolic = 88, pulse = 85, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 8, 23, 59).toMillis(), isMorning = false, systolic = 122, diastolic = 80, pulse = 80, note = "avond weg geweest afas dome"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 9, 7, 42).toMillis(), isMorning = true, systolic = 118, diastolic = 87, pulse = 72, note = "Medicatie ingenomen - overslapen en half..."),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 9, 21, 48).toMillis(), isMorning = false, systolic = 129, diastolic = 77, pulse = 75, note = "Medicatie ingenomen - heen en terug naar..."),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 10, 6, 51).toMillis(), isMorning = true, systolic = 125, diastolic = 85, pulse = 65, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 10, 20, 55).toMillis(), isMorning = false, systolic = 114, diastolic = 71, pulse = 79, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 11, 9, 45).toMillis(), isMorning = true, systolic = 128, diastolic = 83, pulse = 88, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 11, 21, 1).toMillis(), isMorning = false, systolic = 119, diastolic = 80, pulse = 72, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 12, 7, 41).toMillis(), isMorning = true, systolic = 131, diastolic = 90, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 12, 19, 50).toMillis(), isMorning = false, systolic = 123, diastolic = 78, pulse = 76, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 13, 6, 39).toMillis(), isMorning = true, systolic = 131, diastolic = 81, pulse = 83, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 13, 21, 54).toMillis(), isMorning = false, systolic = 120, diastolic = 79, pulse = 69, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 14, 6, 12).toMillis(), isMorning = true, systolic = 135, diastolic = 90, pulse = 64, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 14, 22, 26).toMillis(), isMorning = false, systolic = 125, diastolic = 87, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 15, 7, 20).toMillis(), isMorning = true, systolic = 118, diastolic = 80, pulse = 72, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 15, 20, 17).toMillis(), isMorning = false, systolic = 121, diastolic = 76, pulse = 66, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 16, 5, 57).toMillis(), isMorning = true, systolic = 113, diastolic = 92, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 16, 21, 1).toMillis(), isMorning = false, systolic = 114, diastolic = 74, pulse = 69, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 17, 11, 31).toMillis(), isMorning = true, systolic = 111, diastolic = 76, pulse = 91, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 17, 19, 44).toMillis(), isMorning = false, systolic = 105, diastolic = 69, pulse = 71, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 18, 9, 39).toMillis(), isMorning = true, systolic = 115, diastolic = 71, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 18, 21, 33).toMillis(), isMorning = false, systolic = 123, diastolic = 83, pulse = 78, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 19, 5, 59).toMillis(), isMorning = true, systolic = 121, diastolic = 86, pulse = 73, note = "Medicatie ingenomen"),
                    BloodPressureEntry(timestamp = java.time.LocalDateTime.of(2026, 10, 19, 18, 43).toMillis(), isMorning = false, systolic = 121, diastolic = 79, pulse = 75, note = "Medicatie ingenomen")
                )
                seedList.forEach { e ->
                    db.bloodPressureDao().upsert(e)
                    com.joeri.bloeddruk.data.FirestoreSyncManager.saveMeasurementToFirestore(e)
                }
            }
        }

        // Schakel alle automatische interne 7u30 / 19u30 alarmen uit
        viewModelScope.launch {
            val allAlarms = db.alarmDao().getAll()
            allAlarms.forEach { alarm ->
                if (alarm.enabled) {
                    val disabledAlarm = alarm.copy(enabled = false)
                    db.alarmDao().upsert(disabledAlarm)
                    AlarmScheduler.schedule(getApplication(), disabledAlarm)
                }
            }
        }
    }

    fun save(entry: BloodPressureEntry) = viewModelScope.launch {
        db.bloodPressureDao().upsert(entry)
        com.joeri.bloeddruk.data.FirestoreSyncManager.saveMeasurementToFirestore(entry)
        val allEntries = db.bloodPressureDao().getAllEntries()
        com.joeri.bloeddruk.export.MonthlyReportManager.autoUpdateAllMonthlyReports(
            getApplication(),
            allEntries,
            userProfile.value
        )
    }

    fun delete(entry: BloodPressureEntry) = viewModelScope.launch {
        db.bloodPressureDao().delete(entry)
        com.joeri.bloeddruk.data.FirestoreSyncManager.deleteMeasurementFromFirestore(entry)
        val allEntries = db.bloodPressureDao().getAllEntries()
        com.joeri.bloeddruk.export.MonthlyReportManager.autoUpdateAllMonthlyReports(
            getApplication(),
            allEntries,
            userProfile.value
        )
    }

    fun saveMedication(item: MedicationItem) = viewModelScope.launch { db.medicationDao().upsert(item) }

    fun deleteMedication(item: MedicationItem) = viewModelScope.launch { db.medicationDao().delete(item) }

    fun logMedicationIntake(medication: MedicationItem, slot: String, status: String = "TAKEN") = viewModelScope.launch {
        db.medicationLogDao().upsert(
            com.joeri.bloeddruk.data.MedicationLog(
                medicationId = medication.id,
                medicationName = medication.name,
                dateStr = todayStr,
                slot = slot,
                timestamp = System.currentTimeMillis(),
                status = status
            )
        )
    }

    fun undoMedicationIntake(medication: MedicationItem, slot: String) = viewModelScope.launch {
        db.medicationLogDao().deleteLogForSlot(medication.id, todayStr, slot)
    }

    fun saveProfile(profile: UserProfile) = viewModelScope.launch { db.userProfileDao().upsert(profile) }

    fun cleanupDuplicates() = viewModelScope.launch {
        com.joeri.bloeddruk.data.FirestoreSyncManager.cleanupLocalDuplicates(db)
    }

    fun restoreBackup(data: com.joeri.bloeddruk.data.BackupData) = viewModelScope.launch {
        data.entries.forEach { db.bloodPressureDao().upsert(it) }
        data.medications.forEach { db.medicationDao().upsert(it) }
        db.userProfileDao().upsert(data.userProfile)
    }

    fun updateAlarm(alarm: AlarmSetting) = viewModelScope.launch {
        db.alarmDao().upsert(alarm)
        AlarmScheduler.schedule(getApplication(), alarm)
    }

    /** Opnieuw plannen, bv. nadat de toestemming voor exacte alarmen is verleend. */
    fun rescheduleAll() = viewModelScope.launch { AlarmScheduler.rescheduleAll(getApplication()) }
}
