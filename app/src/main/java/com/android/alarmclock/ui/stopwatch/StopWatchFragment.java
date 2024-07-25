package com.android.alarmclock.ui.stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.alarmclock.R;
import com.google.android.material.card.MaterialCardView;

public class StopWatchFragment extends Fragment {

    private TextView textTimer;
    private MaterialCardView startBtn, pauseBtn, resetBtn;
    private Handler handler = new Handler();
    private long startTime, timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L;
    private int seconds, minutes, milliseconds, hours;
    private boolean isRunning = false;

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            seconds = (int) (updatedTime / 1000);
            minutes = seconds / 60;
            hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;
            milliseconds = (int) (updatedTime % 1000);

            textTimer.setText(String.format("%02d  :  %02d  :  %02d  :  %03d", hours, minutes, seconds, milliseconds));
            handler.postDelayed(this, 0);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialization if needed
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stop_watch, container, false);
        textTimer = view.findViewById(R.id.textTimer);
        startBtn = view.findViewById(R.id.startBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        resetBtn = view.findViewById(R.id.resetCard);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    startTime = SystemClock.uptimeMillis();
                    handler.post(updateTimer);
                    isRunning = true;
                    startBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    timeSwapBuff += timeInMilliseconds;
                    handler.removeCallbacks(updateTimer);
                    isRunning = false;
                    startBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.GONE);
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeSwapBuff = 0L;
                timeInMilliseconds = 0L;
                updatedTime = 0L;
                seconds = 0;
                minutes = 0;
                hours = 0;
                milliseconds = 0;
                textTimer.setText("00  :  00  :  00  :  000");
                if (isRunning) {
                    handler.removeCallbacks(updateTimer);
                    isRunning = false;
                    startBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }
}
