package com.android.alarmclock.supportingWorldClockClasses;

import android.annotation.SuppressLint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeDataUtil {

    public static Map<String, String> getTimeForAllCountries() {
        Map<String, String> timeData = new HashMap<>();
        String[] timeZoneIDs = TimeZone.getAvailableIDs();

        for (String timeZoneID : timeZoneIDs) {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
            Calendar calendar = Calendar.getInstance(timeZone);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            @SuppressLint("DefaultLocale")
            String timeString = String.format("%02d:%02d:%02d %s ", hour, minute, second);
            timeData.put(timeZoneID, timeString);
        }

        return timeData;
    }
}
