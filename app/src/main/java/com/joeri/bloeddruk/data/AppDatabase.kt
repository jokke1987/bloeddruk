package com.joeri.bloeddruk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BloodPressureEntry::class, AlarmSetting::class, MedicationItem::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun alarmDao(): AlarmDao
    abstract fun medicationDao(): MedicationDao

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

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bloeddruk.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { instance = it }
            }
    }
}
