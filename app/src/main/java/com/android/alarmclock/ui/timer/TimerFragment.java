package com.android.alarmclock.ui.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.android.alarmclock.R;
import com.android.alarmclock.databinding.FragmentTimerBinding;
import com.android.alarmclock.supportingTimerClasses.StopSoundReceiver;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean timerRunning;
    private static Ringtone ringtone;
    private static final String CHANNEL_ID = "timer_channel";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.playCard.setOnClickListener(v -> startTimer());
        binding.pauseCard.setOnClickListener(v -> pauseTimer());
        binding.resetCard.setOnClickListener(v -> resetTimer());

        createNotificationChannel(); // Create notification channel

        return root;
    }

    private void startTimer() {
        if (!timerRunning) {
            String hoursStr = binding.hourInput.getText().toString();
            String minutesStr = binding.minuteInput.getText().toString();
            String secondsStr = binding.secondInput.getText().toString();

            long hours = hoursStr.isEmpty() ? 0 : Long.parseLong(hoursStr);
            long minutes = minutesStr.isEmpty() ? 0 : Long.parseLong(minutesStr);
            long seconds = secondsStr.isEmpty() ? 0 : Long.parseLong(secondsStr);

            if (hours == 0 && minutes == 0 && seconds == 0) {
                Toast.makeText(getActivity(), "Please enter a valid time", Toast.LENGTH_SHORT).show();
                return;
            }

            long millisInput = (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
            setTime(millisInput);
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateCountDownText();
                }

                @Override
                public void onFinish() {
                    timerRunning = false;
                    updateButtons();
                    showNotification(); // Show notification when timer finishes
                    playAlarmSound(); // Play sound when timer finishes
                }
            }.start();

            timerRunning = true;
            updateButtons();
        }
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = 0;
        updateCountDownText();
        updateAspectTime();
        timerRunning = false;
        updateButtons();
        stopAlarmSound(); // Stop any sound when timer is reset
    }

    private void pauseTimer() {
        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
            updateButtons();
        }
    }

    private void setTime(long milliseconds) {
        timeLeftInMillis = milliseconds;
        updateCountDownText();
        updateAspectTime();
    }

    private void updateCountDownText() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        binding.timerDisplay.setText(timeFormatted);
    }

    private void updateAspectTime() {
        int days = (int) (timeLeftInMillis / (1000 * 60 * 60 * 24));
        int hours = (int) ((timeLeftInMillis / (1000 * 60 * 60)) % 24);
        int minutes = (int) ((timeLeftInMillis / (1000 * 60)) % 60);
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String aspectTimeFormatted = String.format("%d days %d hours %d minutes %d seconds", days, hours, minutes, seconds);
        binding.aspectTime.setText(aspectTimeFormatted);
    }

    private void updateButtons() {
        if (timerRunning) {
            binding.playCard.setVisibility(View.GONE);
            binding.pauseCard.setVisibility(View.VISIBLE);
        } else {
            binding.playCard.setVisibility(View.VISIBLE);
            binding.pauseCard.setVisibility(View.GONE);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_circle_notifications_24) // Replace with your icon
                .setContentTitle("Timer Finished")
                .setContentText("The timer has finished. Tap to stop the sound.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.outline_pause_circle_outline_24, "Stop", getStopPendingIntent())
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private PendingIntent getStopPendingIntent() {
        Intent intent = new Intent(getActivity(), StopSoundReceiver.class);
        return PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void playAlarmSound() {
        if (ringtone == null) {
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (defaultRingtoneUri != null) {
                ringtone = RingtoneManager.getRingtone(getActivity(), defaultRingtoneUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.setLooping(true); // Set to true if you want it to loop
                    }
                }
            }
        }
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    public static void stopAlarmSound() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            ringtone = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAlarmSound();
    }
}
