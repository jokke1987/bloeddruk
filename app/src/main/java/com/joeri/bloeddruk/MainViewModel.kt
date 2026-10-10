package com.joeri.bloeddruk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joeri.bloeddruk.alarm.AlarmScheduler
import com.joeri.bloeddruk.data.AlarmSetting
import com.joeri.bloeddruk.data.AppDatabase
import com.joeri.bloeddruk.data.BloodPressureEntry
import com.joeri.bloeddruk.data.MedicationItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    init {
        // Eerste keer opstarten: 2 ochtend- en 2 avondalarmen aanmaken.
        viewModelScope.launch {
            if (db.alarmDao().count() == 0) {
                listOf(
                    AlarmSetting(0, 7, 30, enabled = true, isMorning = true),
                    AlarmSetting(1, 8, 30, enabled = false, isMorning = true),
                    AlarmSetting(2, 19, 30, enabled = true, isMorning = false),
                    AlarmSetting(3, 20, 30, enabled = false, isMorning = false)
                ).forEach {
                    db.alarmDao().upsert(it)
                    AlarmScheduler.schedule(getApplication(), it)
                }
            }
        }
    }

    fun save(entry: BloodPressureEntry) = viewModelScope.launch { db.bloodPressureDao().upsert(entry) }

    fun delete(entry: BloodPressureEntry) = viewModelScope.launch { db.bloodPressureDao().delete(entry) }

    fun saveMedication(item: MedicationItem) = viewModelScope.launch { db.medicationDao().upsert(item) }

    fun deleteMedication(item: MedicationItem) = viewModelScope.launch { db.medicationDao().delete(item) }

    fun updateAlarm(alarm: AlarmSetting) = viewModelScope.launch {
        db.alarmDao().upsert(alarm)
        AlarmScheduler.schedule(getApplication(), alarm)
    }

    /** Opnieuw plannen, bv. nadat de toestemming voor exacte alarmen is verleend. */
    fun rescheduleAll() = viewModelScope.launch { AlarmScheduler.rescheduleAll(getApplication()) }
}
