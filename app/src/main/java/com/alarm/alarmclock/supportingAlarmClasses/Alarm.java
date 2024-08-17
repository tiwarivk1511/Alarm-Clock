package com.alarm.alarmclock.supportingAlarmClasses;

import android.widget.AdapterView;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int hour;
    private int minute;
    private String repeatOption;
    private boolean vibrate;
    private String taskLabel;
    private String ringtoneUri;
    private boolean isEnabled;

    public Alarm(int hour, int minute, String repeatOption, boolean vibrate, String taskLabel, String ringtoneUri, boolean isEnabled) {
        this.hour = hour;
        this.minute = minute;
        this.repeatOption = repeatOption;
        this.vibrate = vibrate;
        this.taskLabel = taskLabel;
        this.ringtoneUri = ringtoneUri;
        this.isEnabled = isEnabled;
    }

    public Alarm() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getRepeatOption() {
        return repeatOption;
    }
    public void setRepeatOption(String repeatOption) {
        this.repeatOption = repeatOption;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getTaskLabel() {
        return taskLabel;
    }

    public void setTaskLabel(String taskLabel) {
        this.taskLabel = taskLabel;
    }

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public String toString() {
        return String.format("%s %02d:%02d - %s", repeatOption, hour, minute, taskLabel);
    }

    // Convert time to 12-hour format string
    public String getFormattedTime() {
        int displayHour = hour % 12;
        displayHour = displayHour == 0 ? 12 : displayHour; // Adjust for 12 AM/PM
        String amPm = hour < 12 ? "AM" : "PM";
        return String.format("%02d:%02d %s", displayHour, minute, amPm);
    }
}
