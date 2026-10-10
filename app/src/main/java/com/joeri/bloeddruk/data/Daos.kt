package com.joeri.bloeddruk.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Query("SELECT * FROM metingen ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BloodPressureEntry>>

    @Upsert
    suspend fun upsert(entry: BloodPressureEntry)

    @Delete
    suspend fun delete(entry: BloodPressureEntry)
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarmen ORDER BY id")
    fun observeAll(): Flow<List<AlarmSetting>>

    @Query("SELECT * FROM alarmen ORDER BY id")
    suspend fun getAll(): List<AlarmSetting>

    @Query("SELECT COUNT(*) FROM alarmen")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(alarm: AlarmSetting)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medicatie ORDER BY id ASC")
    fun observeAll(): Flow<List<MedicationItem>>

    @Upsert
    suspend fun upsert(item: MedicationItem)

    @Delete
    suspend fun delete(item: MedicationItem)
}
