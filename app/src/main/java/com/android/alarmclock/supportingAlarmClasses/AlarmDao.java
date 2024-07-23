package com.android.alarmclock.supportingAlarmClasses;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    @Insert
    void insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarms")
    List<Alarm> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    Alarm getAlarmById(int alarmId);

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    List<Alarm> getEnabledAlarms();
}
