package com.joeri.bloeddruk.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class AlarmDao_Impl implements AlarmDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<AlarmSetting> __upsertionAdapterOfAlarmSetting;

  public AlarmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfAlarmSetting = new EntityUpsertionAdapter<AlarmSetting>(new EntityInsertionAdapter<AlarmSetting>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `alarmen` (`id`,`hour`,`minute`,`enabled`,`isMorning`,`soundUri`,`soundTitle`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmSetting entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHour());
        statement.bindLong(3, entity.getMinute());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isMorning() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindString(6, entity.getSoundUri());
        statement.bindString(7, entity.getSoundTitle());
      }
    }, new EntityDeletionOrUpdateAdapter<AlarmSetting>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `alarmen` SET `id` = ?,`hour` = ?,`minute` = ?,`enabled` = ?,`isMorning` = ?,`soundUri` = ?,`soundTitle` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmSetting entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHour());
        statement.bindLong(3, entity.getMinute());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isMorning() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindString(6, entity.getSoundUri());
        statement.bindString(7, entity.getSoundTitle());
        statement.bindLong(8, entity.getId());
      }
    });
  }

  @Override
  public Object upsert(final AlarmSetting alarm, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfAlarmSetting.upsert(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlarmSetting>> observeAll() {
    final String _sql = "SELECT * FROM alarmen ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alarmen"}, new Callable<List<AlarmSetting>>() {
      @Override
      @NonNull
      public List<AlarmSetting> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfIsMorning = CursorUtil.getColumnIndexOrThrow(_cursor, "isMorning");
          final int _cursorIndexOfSoundUri = CursorUtil.getColumnIndexOrThrow(_cursor, "soundUri");
          final int _cursorIndexOfSoundTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "soundTitle");
          final List<AlarmSetting> _result = new ArrayList<AlarmSetting>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmSetting _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final boolean _tmpIsMorning;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsMorning);
            _tmpIsMorning = _tmp_1 != 0;
            final String _tmpSoundUri;
            _tmpSoundUri = _cursor.getString(_cursorIndexOfSoundUri);
            final String _tmpSoundTitle;
            _tmpSoundTitle = _cursor.getString(_cursorIndexOfSoundTitle);
            _item = new AlarmSetting(_tmpId,_tmpHour,_tmpMinute,_tmpEnabled,_tmpIsMorning,_tmpSoundUri,_tmpSoundTitle);
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
  public Object getAll(final Continuation<? super List<AlarmSetting>> $completion) {
    final String _sql = "SELECT * FROM alarmen ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlarmSetting>>() {
      @Override
      @NonNull
      public List<AlarmSetting> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfIsMorning = CursorUtil.getColumnIndexOrThrow(_cursor, "isMorning");
          final int _cursorIndexOfSoundUri = CursorUtil.getColumnIndexOrThrow(_cursor, "soundUri");
          final int _cursorIndexOfSoundTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "soundTitle");
          final List<AlarmSetting> _result = new ArrayList<AlarmSetting>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmSetting _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final boolean _tmpIsMorning;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsMorning);
            _tmpIsMorning = _tmp_1 != 0;
            final String _tmpSoundUri;
            _tmpSoundUri = _cursor.getString(_cursorIndexOfSoundUri);
            final String _tmpSoundTitle;
            _tmpSoundTitle = _cursor.getString(_cursorIndexOfSoundTitle);
            _item = new AlarmSetting(_tmpId,_tmpHour,_tmpMinute,_tmpEnabled,_tmpIsMorning,_tmpSoundUri,_tmpSoundTitle);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM alarmen";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
