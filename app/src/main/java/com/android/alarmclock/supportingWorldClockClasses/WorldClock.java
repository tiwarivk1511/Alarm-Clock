package com.android.alarmclock.supportingWorldClockClasses;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WorldClock {
    private String cityName;
    private String timeZone;
    private String time;

    public WorldClock(String cityName, String timeZone) {
        this.cityName = cityName;
        this.timeZone = timeZone;
        updateTime(); // Initialize time when creating the object
    }

    public String getCityName() {
        return cityName;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getTime() {
        return time;
    }

    public void updateTime() {
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a"); // 12-hour format with AM/PM
        sdf.setTimeZone(tz);
        this.time = sdf.format(new Date());
    }
}
