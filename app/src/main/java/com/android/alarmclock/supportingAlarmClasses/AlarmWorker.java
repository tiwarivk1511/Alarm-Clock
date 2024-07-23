package com.android.alarmclock.supportingAlarmClasses;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmWorker extends Worker {
    public static final String ALARM_ID = "ALARM_ID";

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        int alarmId = getInputData().getInt(ALARM_ID, -1);

        if (alarmId != -1) {
            // Handle background work here
            // For example, check the alarm and trigger notification
            // This will not trigger if the app is completely closed
            // Make sure to use AlarmManager for immediate alarms
            AlarmDatabase alarmDatabase = AlarmDatabase.getDatabase(getApplicationContext());
            Alarm alarm = alarmDatabase.alarmDao().getAlarmById(alarmId);

            if (alarm != null) {
                // Create and send a broadcast to AlarmReceiver
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.putExtra("ALARM_ID", alarmId);
                getApplicationContext().sendBroadcast(intent);
                return Result.success();
            }
        }

        return Result.failure();
    }
}
