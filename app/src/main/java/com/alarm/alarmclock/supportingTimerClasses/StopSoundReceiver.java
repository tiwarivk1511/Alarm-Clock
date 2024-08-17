package com.alarm.alarmclock.supportingTimerClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alarm.alarmclock.ui.timer.TimerFragment;

public class StopSoundReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TimerFragment.stopAlarmSound();
    }
}
