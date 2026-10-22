package com.joeri.bloeddruk.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BloodPressureDao_Impl implements BloodPressureDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<BloodPressureEntry> __deletionAdapterOfBloodPressureEntry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final EntityUpsertionAdapter<BloodPressureEntry> __upsertionAdapterOfBloodPressureEntry;

  public BloodPressureDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfBloodPressureEntry = new EntityDeletionOrUpdateAdapter<BloodPressureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `metingen` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BloodPressureEntry entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM metingen";
        return _query;
      }
    };
    this.__upsertionAdapterOfBloodPressureEntry = new EntityUpsertionAdapter<BloodPressureEntry>(new EntityInsertionAdapter<BloodPressureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `metingen` (`id`,`timestamp`,`isMorning`,`systolic`,`diastolic`,`pulse`,`note`,`tags`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BloodPressureEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        final int _tmp = entity.isMorning() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getSystolic());
        statement.bindLong(5, entity.getDiastolic());
        statement.bindLong(6, entity.getPulse());
        statement.bindString(7, entity.getNote());
        statement.bindString(8, entity.getTags());
      }
    }, new EntityDeletionOrUpdateAdapter<BloodPressureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `metingen` SET `id` = ?,`timestamp` = ?,`isMorning` = ?,`systolic` = ?,`diastolic` = ?,`pulse` = ?,`note` = ?,`tags` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BloodPressureEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        final int _tmp = entity.isMorning() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getSystolic());
        statement.bindLong(5, entity.getDiastolic());
        statement.bindLong(6, entity.getPulse());
        statement.bindString(7, entity.getNote());
        statement.bindString(8, entity.getTags());
        statement.bindLong(9, entity.getId());
      }
    });
  }

  @Override
  public Object delete(final BloodPressureEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBloodPressureEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final BloodPressureEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfBloodPressureEntry.upsert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BloodPressureEntry>> observeAll() {
    final String _sql = "SELECT * FROM metingen ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"metingen"}, new Callable<List<BloodPressureEntry>>() {
      @Override
      @NonNull
      public List<BloodPressureEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsMorning = CursorUtil.getColumnIndexOrThrow(_cursor, "isMorning");
          final int _cursorIndexOfSystolic = CursorUtil.getColumnIndexOrThrow(_cursor, "systolic");
          final int _cursorIndexOfDiastolic = CursorUtil.getColumnIndexOrThrow(_cursor, "diastolic");
          final int _cursorIndexOfPulse = CursorUtil.getColumnIndexOrThrow(_cursor, "pulse");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final List<BloodPressureEntry> _result = new ArrayList<BloodPressureEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BloodPressureEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsMorning;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMorning);
            _tmpIsMorning = _tmp != 0;
            final int _tmpSystolic;
            _tmpSystolic = _cursor.getInt(_cursorIndexOfSystolic);
            final int _tmpDiastolic;
            _tmpDiastolic = _cursor.getInt(_cursorIndexOfDiastolic);
            final int _tmpPulse;
            _tmpPulse = _cursor.getInt(_cursorIndexOfPulse);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            _item = new BloodPressureEntry(_tmpId,_tmpTimestamp,_tmpIsMorning,_tmpSystolic,_tmpDiastolic,_tmpPulse,_tmpNote,_tmpTags);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
