package com.android.alarmclock.supportingTimerClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;

import com.android.alarmclock.ui.timer.TimerFragment;

public class StopSoundReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TimerFragment.stopAlarmSound();
    }
}
