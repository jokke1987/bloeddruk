package com.joeri.bloeddruk.data;

import android.database.Cursor;
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
public final class MedicationDao_Impl implements MedicationDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<MedicationItem> __deletionAdapterOfMedicationItem;

  private final EntityUpsertionAdapter<MedicationItem> __upsertionAdapterOfMedicationItem;

  public MedicationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfMedicationItem = new EntityDeletionOrUpdateAdapter<MedicationItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `medicatie` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__upsertionAdapterOfMedicationItem = new EntityUpsertionAdapter<MedicationItem>(new EntityInsertionAdapter<MedicationItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `medicatie` (`id`,`name`,`dosage`,`slot`,`note`,`purpose`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDosage());
        statement.bindString(4, entity.getSlot());
        statement.bindString(5, entity.getNote());
        statement.bindString(6, entity.getPurpose());
      }
    }, new EntityDeletionOrUpdateAdapter<MedicationItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `medicatie` SET `id` = ?,`name` = ?,`dosage` = ?,`slot` = ?,`note` = ?,`purpose` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDosage());
        statement.bindString(4, entity.getSlot());
        statement.bindString(5, entity.getNote());
        statement.bindString(6, entity.getPurpose());
        statement.bindLong(7, entity.getId());
      }
    });
  }

  @Override
  public Object delete(final MedicationItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMedicationItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final MedicationItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfMedicationItem.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationItem>> observeAll() {
    final String _sql = "SELECT * FROM medicatie ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medicatie"}, new Callable<List<MedicationItem>>() {
      @Override
      @NonNull
      public List<MedicationItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPurpose = CursorUtil.getColumnIndexOrThrow(_cursor, "purpose");
          final List<MedicationItem> _result = new ArrayList<MedicationItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpSlot;
            _tmpSlot = _cursor.getString(_cursorIndexOfSlot);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final String _tmpPurpose;
            _tmpPurpose = _cursor.getString(_cursorIndexOfPurpose);
            _item = new MedicationItem(_tmpId,_tmpName,_tmpDosage,_tmpSlot,_tmpNote,_tmpPurpose);
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
