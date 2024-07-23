package com.android.alarmclock.supportingWorldClockClasses;

public class WorldClock {
    private String cityName;
    private String timeZone;
    private String time;

    public WorldClock(String cityName, String timeZone) {
        this.cityName = cityName;
        this.timeZone = timeZone;
        this.time = time;
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

}

