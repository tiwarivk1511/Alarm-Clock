package com.alarm.alarmclock.supportingAlarmClasses;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alarm.alarmclock.R;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ALARM_CHANNEL_ID";
    private static final String ACTION_CANCEL_ALARM = "ACTION_CANCEL_ALARM";
    private static final String ACTION_SHOW_NOTIFICATION = "SHOW_NOTIFICATION";
    private static final String ACTION_RESCHEDULE_ALARM = "ACTION_RESCHEDULE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_SHOW_NOTIFICATION.equals(action)) {
            // Show notification 5 minutes before the alarm
            createNotification(context);
        } else if (ACTION_CANCEL_ALARM.equals(action)) {
            // Handle alarm cancellation
            Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_SHORT).show();
            stopRingtone(context);
        } else if (ACTION_RESCHEDULE_ALARM.equals(action)) {
            // Handle alarm rescheduling
            rescheduleAlarm(context);
        } else {
            // Handle the actual alarm
            createNotification(context);
            playAlarmSound(context);
        }
    }

    private void createNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Create notification channel for Android 8.0 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent to cancel the alarm
        Intent cancelIntent = new Intent(context, AlarmReceiver.class);
        cancelIntent.setAction(ACTION_CANCEL_ALARM);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText("Tap to cancel the alarm")
                .setSmallIcon(R.drawable.ic_logo)
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Cancel",
                        cancelPendingIntent))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Handle missing notification permission
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void playAlarmSound(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
    }

    private void stopRingtone(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void rescheduleAlarm(Context context) {
        // Extract alarm details from your storage or intent extras
        int hour = 10;
        int minute = 0;

        // Set up the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(ACTION_SHOW_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Use Calendar to set the alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent);

        // Set the new alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
