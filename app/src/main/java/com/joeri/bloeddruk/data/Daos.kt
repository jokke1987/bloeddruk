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

    @Query("DELETE FROM metingen")
    suspend fun deleteAll()
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

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfile?

    @Upsert
    suspend fun upsert(profile: UserProfile)
}

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medicatie_logs WHERE dateStr = :dateStr")
    fun observeLogsForDate(dateStr: String): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medicatie_logs ORDER BY timestamp DESC")
    fun observeAllLogs(): Flow<List<MedicationLog>>

    @Upsert
    suspend fun upsert(log: MedicationLog)

    @Delete
    suspend fun delete(log: MedicationLog)

    @Query("DELETE FROM medicatie_logs WHERE medicationId = :medId AND dateStr = :dateStr AND slot = :slot")
    suspend fun deleteLogForSlot(medId: Long, dateStr: String, slot: String)
}
