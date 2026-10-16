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

    fun save(entry: BloodPressureEntry) = viewModelScope.launch { db.bloodPressureDao().upsert(entry) }

    fun delete(entry: BloodPressureEntry) = viewModelScope.launch { db.bloodPressureDao().delete(entry) }

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
