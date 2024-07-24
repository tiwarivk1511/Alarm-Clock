package com.android.alarmclock.supportingTimerClasses;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.alarmclock.R;

public class TimerWorker extends Worker {

    public static final String KEY_MILLIS_LEFT = "MILLIS_LEFT";
    private static final String CHANNEL_ID = "timer_channel";

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long millisLeft = getInputData().getLong(KEY_MILLIS_LEFT, 0);

        if (millisLeft > 0) {
            try {
                Thread.sleep(millisLeft);
                showNotification();
                playAlertSound();
                return Result.success();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Result.failure();
            }
        }

        return Result.failure();
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Timer Finished")
                .setContentText("Your timer has completed.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void playAlertSound() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        RingtoneManager.getRingtone(getApplicationContext(), alarmSound).play();
    }
}
