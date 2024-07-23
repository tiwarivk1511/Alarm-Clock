package com.android.alarmclock.supportingAlarmClasses;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class AlarmScheduler {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void scheduleAlarm(int alarmId, Calendar calendar) {
        if (checkExactAlarmPermission()) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarm_id", alarmId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else {
            // Notify user to grant the required permission
            Toast.makeText(context, "Please grant the exact alarm permission in settings.", Toast.LENGTH_LONG).show();
            // Optionally, direct the user to the settings page
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);

        }
    }

    private boolean checkExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return context.getSystemService(AlarmManager.class).canScheduleExactAlarms();
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void scheduleNotification(int alarmId, Calendar calendar) {
        if (checkExactAlarmPermission()) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction("NOTIFICATION_ACTION");
            intent.putExtra("alarm_id", alarmId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else {
            // Notify user to grant the required permission
            Toast.makeText(context, "Please grant the exact alarm permission in settings.", Toast.LENGTH_LONG).show();
            // Optionally, direct the user to the settings page
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
        }
    }
}
