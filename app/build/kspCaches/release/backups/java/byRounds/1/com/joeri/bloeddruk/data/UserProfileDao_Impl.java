package com.joeri.bloeddruk.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserProfileDao_Impl implements UserProfileDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<UserProfile> __upsertionAdapterOfUserProfile;

  public UserProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfUserProfile = new EntityUpsertionAdapter<UserProfile>(new EntityInsertionAdapter<UserProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `user_profile` (`id`,`bloodType`,`doctorName`,`doctorPhone`,`doctorEmail`,`doctorAddress`,`doctorNotes`,`emergencyContact`,`isDarkMode`,`autoArchive`,`sysNormalMax`,`diaNormalMax`,`sysElevatedMax`,`diaElevatedMax`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBloodType());
        statement.bindString(3, entity.getDoctorName());
        statement.bindString(4, entity.getDoctorPhone());
        statement.bindString(5, entity.getDoctorEmail());
        statement.bindString(6, entity.getDoctorAddress());
        statement.bindString(7, entity.getDoctorNotes());
        statement.bindString(8, entity.getEmergencyContact());
        final int _tmp = entity.isDarkMode() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.getAutoArchive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindLong(11, entity.getSysNormalMax());
        statement.bindLong(12, entity.getDiaNormalMax());
        statement.bindLong(13, entity.getSysElevatedMax());
        statement.bindLong(14, entity.getDiaElevatedMax());
      }
    }, new EntityDeletionOrUpdateAdapter<UserProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `user_profile` SET `id` = ?,`bloodType` = ?,`doctorName` = ?,`doctorPhone` = ?,`doctorEmail` = ?,`doctorAddress` = ?,`doctorNotes` = ?,`emergencyContact` = ?,`isDarkMode` = ?,`autoArchive` = ?,`sysNormalMax` = ?,`diaNormalMax` = ?,`sysElevatedMax` = ?,`diaElevatedMax` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBloodType());
        statement.bindString(3, entity.getDoctorName());
        statement.bindString(4, entity.getDoctorPhone());
        statement.bindString(5, entity.getDoctorEmail());
        statement.bindString(6, entity.getDoctorAddress());
        statement.bindString(7, entity.getDoctorNotes());
        statement.bindString(8, entity.getEmergencyContact());
        final int _tmp = entity.isDarkMode() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.getAutoArchive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindLong(11, entity.getSysNormalMax());
        statement.bindLong(12, entity.getDiaNormalMax());
        statement.bindLong(13, entity.getSysElevatedMax());
        statement.bindLong(14, entity.getDiaElevatedMax());
        statement.bindLong(15, entity.getId());
      }
    });
  }

  @Override
  public Object upsert(final UserProfile profile, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfUserProfile.upsert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserProfile> observeProfile() {
    final String _sql = "SELECT * FROM user_profile WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_profile"}, new Callable<UserProfile>() {
      @Override
      @Nullable
      public UserProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBloodType = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodType");
          final int _cursorIndexOfDoctorName = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorName");
          final int _cursorIndexOfDoctorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorPhone");
          final int _cursorIndexOfDoctorEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorEmail");
          final int _cursorIndexOfDoctorAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorAddress");
          final int _cursorIndexOfDoctorNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorNotes");
          final int _cursorIndexOfEmergencyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyContact");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfAutoArchive = CursorUtil.getColumnIndexOrThrow(_cursor, "autoArchive");
          final int _cursorIndexOfSysNormalMax = CursorUtil.getColumnIndexOrThrow(_cursor, "sysNormalMax");
          final int _cursorIndexOfDiaNormalMax = CursorUtil.getColumnIndexOrThrow(_cursor, "diaNormalMax");
          final int _cursorIndexOfSysElevatedMax = CursorUtil.getColumnIndexOrThrow(_cursor, "sysElevatedMax");
          final int _cursorIndexOfDiaElevatedMax = CursorUtil.getColumnIndexOrThrow(_cursor, "diaElevatedMax");
          final UserProfile _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpBloodType;
            _tmpBloodType = _cursor.getString(_cursorIndexOfBloodType);
            final String _tmpDoctorName;
            _tmpDoctorName = _cursor.getString(_cursorIndexOfDoctorName);
            final String _tmpDoctorPhone;
            _tmpDoctorPhone = _cursor.getString(_cursorIndexOfDoctorPhone);
            final String _tmpDoctorEmail;
            _tmpDoctorEmail = _cursor.getString(_cursorIndexOfDoctorEmail);
            final String _tmpDoctorAddress;
            _tmpDoctorAddress = _cursor.getString(_cursorIndexOfDoctorAddress);
            final String _tmpDoctorNotes;
            _tmpDoctorNotes = _cursor.getString(_cursorIndexOfDoctorNotes);
            final String _tmpEmergencyContact;
            _tmpEmergencyContact = _cursor.getString(_cursorIndexOfEmergencyContact);
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpAutoArchive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfAutoArchive);
            _tmpAutoArchive = _tmp_1 != 0;
            final int _tmpSysNormalMax;
            _tmpSysNormalMax = _cursor.getInt(_cursorIndexOfSysNormalMax);
            final int _tmpDiaNormalMax;
            _tmpDiaNormalMax = _cursor.getInt(_cursorIndexOfDiaNormalMax);
            final int _tmpSysElevatedMax;
            _tmpSysElevatedMax = _cursor.getInt(_cursorIndexOfSysElevatedMax);
            final int _tmpDiaElevatedMax;
            _tmpDiaElevatedMax = _cursor.getInt(_cursorIndexOfDiaElevatedMax);
            _result = new UserProfile(_tmpId,_tmpBloodType,_tmpDoctorName,_tmpDoctorPhone,_tmpDoctorEmail,_tmpDoctorAddress,_tmpDoctorNotes,_tmpEmergencyContact,_tmpIsDarkMode,_tmpAutoArchive,_tmpSysNormalMax,_tmpDiaNormalMax,_tmpSysElevatedMax,_tmpDiaElevatedMax);
          } else {
            _result = null;
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
  public Object getProfile(final Continuation<? super UserProfile> $completion) {
    final String _sql = "SELECT * FROM user_profile WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserProfile>() {
      @Override
      @Nullable
      public UserProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBloodType = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodType");
          final int _cursorIndexOfDoctorName = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorName");
          final int _cursorIndexOfDoctorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorPhone");
          final int _cursorIndexOfDoctorEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorEmail");
          final int _cursorIndexOfDoctorAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorAddress");
          final int _cursorIndexOfDoctorNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorNotes");
          final int _cursorIndexOfEmergencyContact = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyContact");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfAutoArchive = CursorUtil.getColumnIndexOrThrow(_cursor, "autoArchive");
          final int _cursorIndexOfSysNormalMax = CursorUtil.getColumnIndexOrThrow(_cursor, "sysNormalMax");
          final int _cursorIndexOfDiaNormalMax = CursorUtil.getColumnIndexOrThrow(_cursor, "diaNormalMax");
          final int _cursorIndexOfSysElevatedMax = CursorUtil.getColumnIndexOrThrow(_cursor, "sysElevatedMax");
          final int _cursorIndexOfDiaElevatedMax = CursorUtil.getColumnIndexOrThrow(_cursor, "diaElevatedMax");
          final UserProfile _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpBloodType;
            _tmpBloodType = _cursor.getString(_cursorIndexOfBloodType);
            final String _tmpDoctorName;
            _tmpDoctorName = _cursor.getString(_cursorIndexOfDoctorName);
            final String _tmpDoctorPhone;
            _tmpDoctorPhone = _cursor.getString(_cursorIndexOfDoctorPhone);
            final String _tmpDoctorEmail;
            _tmpDoctorEmail = _cursor.getString(_cursorIndexOfDoctorEmail);
            final String _tmpDoctorAddress;
            _tmpDoctorAddress = _cursor.getString(_cursorIndexOfDoctorAddress);
            final String _tmpDoctorNotes;
            _tmpDoctorNotes = _cursor.getString(_cursorIndexOfDoctorNotes);
            final String _tmpEmergencyContact;
            _tmpEmergencyContact = _cursor.getString(_cursorIndexOfEmergencyContact);
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpAutoArchive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfAutoArchive);
            _tmpAutoArchive = _tmp_1 != 0;
            final int _tmpSysNormalMax;
            _tmpSysNormalMax = _cursor.getInt(_cursorIndexOfSysNormalMax);
            final int _tmpDiaNormalMax;
            _tmpDiaNormalMax = _cursor.getInt(_cursorIndexOfDiaNormalMax);
            final int _tmpSysElevatedMax;
            _tmpSysElevatedMax = _cursor.getInt(_cursorIndexOfSysElevatedMax);
            final int _tmpDiaElevatedMax;
            _tmpDiaElevatedMax = _cursor.getInt(_cursorIndexOfDiaElevatedMax);
            _result = new UserProfile(_tmpId,_tmpBloodType,_tmpDoctorName,_tmpDoctorPhone,_tmpDoctorEmail,_tmpDoctorAddress,_tmpDoctorNotes,_tmpEmergencyContact,_tmpIsDarkMode,_tmpAutoArchive,_tmpSysNormalMax,_tmpDiaNormalMax,_tmpSysElevatedMax,_tmpDiaElevatedMax);
          } else {
            _result = null;
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
