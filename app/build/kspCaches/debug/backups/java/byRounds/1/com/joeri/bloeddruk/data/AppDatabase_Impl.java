package com.joeri.bloeddruk.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile BloodPressureDao _bloodPressureDao;

  private volatile AlarmDao _alarmDao;

  private volatile MedicationDao _medicationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `metingen` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `isMorning` INTEGER NOT NULL, `systolic` INTEGER NOT NULL, `diastolic` INTEGER NOT NULL, `pulse` INTEGER NOT NULL, `note` TEXT NOT NULL, `tags` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `alarmen` (`id` INTEGER NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `isMorning` INTEGER NOT NULL, `soundUri` TEXT NOT NULL, `soundTitle` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `medicatie` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `dosage` TEXT NOT NULL, `slot` TEXT NOT NULL, `note` TEXT NOT NULL, `purpose` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '094ec22cb87fb6b37e961358cc85caf2')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `metingen`");
        db.execSQL("DROP TABLE IF EXISTS `alarmen`");
        db.execSQL("DROP TABLE IF EXISTS `medicatie`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMetingen = new HashMap<String, TableInfo.Column>(8);
        _columnsMetingen.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("isMorning", new TableInfo.Column("isMorning", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("systolic", new TableInfo.Column("systolic", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("diastolic", new TableInfo.Column("diastolic", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("pulse", new TableInfo.Column("pulse", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMetingen.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMetingen = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMetingen = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMetingen = new TableInfo("metingen", _columnsMetingen, _foreignKeysMetingen, _indicesMetingen);
        final TableInfo _existingMetingen = TableInfo.read(db, "metingen");
        if (!_infoMetingen.equals(_existingMetingen)) {
          return new RoomOpenHelper.ValidationResult(false, "metingen(com.joeri.bloeddruk.data.BloodPressureEntry).\n"
                  + " Expected:\n" + _infoMetingen + "\n"
                  + " Found:\n" + _existingMetingen);
        }
        final HashMap<String, TableInfo.Column> _columnsAlarmen = new HashMap<String, TableInfo.Column>(7);
        _columnsAlarmen.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("hour", new TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("minute", new TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("isMorning", new TableInfo.Column("isMorning", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("soundUri", new TableInfo.Column("soundUri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmen.put("soundTitle", new TableInfo.Column("soundTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlarmen = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlarmen = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlarmen = new TableInfo("alarmen", _columnsAlarmen, _foreignKeysAlarmen, _indicesAlarmen);
        final TableInfo _existingAlarmen = TableInfo.read(db, "alarmen");
        if (!_infoAlarmen.equals(_existingAlarmen)) {
          return new RoomOpenHelper.ValidationResult(false, "alarmen(com.joeri.bloeddruk.data.AlarmSetting).\n"
                  + " Expected:\n" + _infoAlarmen + "\n"
                  + " Found:\n" + _existingAlarmen);
        }
        final HashMap<String, TableInfo.Column> _columnsMedicatie = new HashMap<String, TableInfo.Column>(6);
        _columnsMedicatie.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicatie.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicatie.put("dosage", new TableInfo.Column("dosage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicatie.put("slot", new TableInfo.Column("slot", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicatie.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicatie.put("purpose", new TableInfo.Column("purpose", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedicatie = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMedicatie = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMedicatie = new TableInfo("medicatie", _columnsMedicatie, _foreignKeysMedicatie, _indicesMedicatie);
        final TableInfo _existingMedicatie = TableInfo.read(db, "medicatie");
        if (!_infoMedicatie.equals(_existingMedicatie)) {
          return new RoomOpenHelper.ValidationResult(false, "medicatie(com.joeri.bloeddruk.data.MedicationItem).\n"
                  + " Expected:\n" + _infoMedicatie + "\n"
                  + " Found:\n" + _existingMedicatie);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "094ec22cb87fb6b37e961358cc85caf2", "4a9181dfd182833b42a398cc8c6cd246");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "metingen","alarmen","medicatie");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `metingen`");
      _db.execSQL("DELETE FROM `alarmen`");
      _db.execSQL("DELETE FROM `medicatie`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BloodPressureDao.class, BloodPressureDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlarmDao.class, AlarmDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MedicationDao.class, MedicationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BloodPressureDao bloodPressureDao() {
    if (_bloodPressureDao != null) {
      return _bloodPressureDao;
    } else {
      synchronized(this) {
        if(_bloodPressureDao == null) {
          _bloodPressureDao = new BloodPressureDao_Impl(this);
        }
        return _bloodPressureDao;
      }
    }
  }

  @Override
  public AlarmDao alarmDao() {
    if (_alarmDao != null) {
      return _alarmDao;
    } else {
      synchronized(this) {
        if(_alarmDao == null) {
          _alarmDao = new AlarmDao_Impl(this);
        }
        return _alarmDao;
      }
    }
  }

  @Override
  public MedicationDao medicationDao() {
    if (_medicationDao != null) {
      return _medicationDao;
    } else {
      synchronized(this) {
        if(_medicationDao == null) {
          _medicationDao = new MedicationDao_Impl(this);
        }
        return _medicationDao;
      }
    }
  }
}
