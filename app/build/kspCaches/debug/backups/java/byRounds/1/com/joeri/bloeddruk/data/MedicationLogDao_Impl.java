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
public final class MedicationLogDao_Impl implements MedicationLogDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<MedicationLog> __deletionAdapterOfMedicationLog;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLogForSlot;

  private final EntityUpsertionAdapter<MedicationLog> __upsertionAdapterOfMedicationLog;

  public MedicationLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfMedicationLog = new EntityDeletionOrUpdateAdapter<MedicationLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `medicatie_logs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationLog entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteLogForSlot = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM medicatie_logs WHERE medicationId = ? AND dateStr = ? AND slot = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfMedicationLog = new EntityUpsertionAdapter<MedicationLog>(new EntityInsertionAdapter<MedicationLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `medicatie_logs` (`id`,`medicationId`,`medicationName`,`dateStr`,`slot`,`timestamp`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicationId());
        statement.bindString(3, entity.getMedicationName());
        statement.bindString(4, entity.getDateStr());
        statement.bindString(5, entity.getSlot());
        statement.bindLong(6, entity.getTimestamp());
        statement.bindString(7, entity.getStatus());
      }
    }, new EntityDeletionOrUpdateAdapter<MedicationLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `medicatie_logs` SET `id` = ?,`medicationId` = ?,`medicationName` = ?,`dateStr` = ?,`slot` = ?,`timestamp` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicationId());
        statement.bindString(3, entity.getMedicationName());
        statement.bindString(4, entity.getDateStr());
        statement.bindString(5, entity.getSlot());
        statement.bindLong(6, entity.getTimestamp());
        statement.bindString(7, entity.getStatus());
        statement.bindLong(8, entity.getId());
      }
    });
  }

  @Override
  public Object delete(final MedicationLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMedicationLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLogForSlot(final long medId, final String dateStr, final String slot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLogForSlot.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, medId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, dateStr);
        _argIndex = 3;
        _stmt.bindString(_argIndex, slot);
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
          __preparedStmtOfDeleteLogForSlot.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final MedicationLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfMedicationLog.upsert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationLog>> observeLogsForDate(final String dateStr) {
    final String _sql = "SELECT * FROM medicatie_logs WHERE dateStr = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dateStr);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medicatie_logs"}, new Callable<List<MedicationLog>>() {
      @Override
      @NonNull
      public List<MedicationLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDateStr = CursorUtil.getColumnIndexOrThrow(_cursor, "dateStr");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<MedicationLog> _result = new ArrayList<MedicationLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicationId;
            _tmpMedicationId = _cursor.getLong(_cursorIndexOfMedicationId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDateStr;
            _tmpDateStr = _cursor.getString(_cursorIndexOfDateStr);
            final String _tmpSlot;
            _tmpSlot = _cursor.getString(_cursorIndexOfSlot);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new MedicationLog(_tmpId,_tmpMedicationId,_tmpMedicationName,_tmpDateStr,_tmpSlot,_tmpTimestamp,_tmpStatus);
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

  @Override
  public Flow<List<MedicationLog>> observeAllLogs() {
    final String _sql = "SELECT * FROM medicatie_logs ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medicatie_logs"}, new Callable<List<MedicationLog>>() {
      @Override
      @NonNull
      public List<MedicationLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDateStr = CursorUtil.getColumnIndexOrThrow(_cursor, "dateStr");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<MedicationLog> _result = new ArrayList<MedicationLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicationId;
            _tmpMedicationId = _cursor.getLong(_cursorIndexOfMedicationId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDateStr;
            _tmpDateStr = _cursor.getString(_cursorIndexOfDateStr);
            final String _tmpSlot;
            _tmpSlot = _cursor.getString(_cursorIndexOfSlot);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new MedicationLog(_tmpId,_tmpMedicationId,_tmpMedicationName,_tmpDateStr,_tmpSlot,_tmpTimestamp,_tmpStatus);
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
