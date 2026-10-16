package com.joeri.bloeddruk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BloodPressureEntry::class, AlarmSetting::class, MedicationItem::class, UserProfile::class, MedicationLog::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun alarmDao(): AlarmDao
    abstract fun medicationDao(): MedicationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        /** Versie 2: notitie en labels bij een meting. Bestaande metingen blijven behouden. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE metingen ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE metingen ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Versie 3: aangepast wekkergeluid per alarm. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarmen ADD COLUMN soundUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE alarmen ADD COLUMN soundTitle TEXT NOT NULL DEFAULT 'Standaard beltoon'")
            }
        }

        /** Versie 4: medicatielijst per dagdeel ('s morgens, 's middags, 's avonds, nacht). */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medicatie` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `dosage` TEXT NOT NULL DEFAULT '',
                        `slot` TEXT NOT NULL DEFAULT 'MORNING',
                        `note` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        /** Versie 5: veld 'purpose' (waarvoor dient de medicatie) toegevoegd. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicatie ADD COLUMN purpose TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Versie 6: tabel 'user_profile' voor doktergegevens en bloedgroep. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `bloodType` TEXT NOT NULL DEFAULT '',
                        `doctorName` TEXT NOT NULL DEFAULT '',
                        `doctorPhone` TEXT NOT NULL DEFAULT '',
                        `doctorEmail` TEXT NOT NULL DEFAULT '',
                        `doctorAddress` TEXT NOT NULL DEFAULT '',
                        `doctorNotes` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        /** Versie 7: instellingen toegevoegd aan 'user_profile' (donkere modus, streefwaarden, noodcontact, auto-archiveren). */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN emergencyContact TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isDarkMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN autoArchive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN sysNormalMax INTEGER NOT NULL DEFAULT 120")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN diaNormalMax INTEGER NOT NULL DEFAULT 80")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN sysElevatedMax INTEGER NOT NULL DEFAULT 139")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN diaElevatedMax INTEGER NOT NULL DEFAULT 89")
            }
        }

        /** Versie 8: FASE 3 - tabel 'medicatie_logs' voor daadwerkelijke innameregistraties per dag/dagdeel. */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medicatie_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `medicationId` INTEGER NOT NULL,
                        `medicationName` TEXT NOT NULL,
                        `dateStr` TEXT NOT NULL,
                        `slot` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'TAKEN'
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bloeddruk.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { instance = it }
            }
    }
}
