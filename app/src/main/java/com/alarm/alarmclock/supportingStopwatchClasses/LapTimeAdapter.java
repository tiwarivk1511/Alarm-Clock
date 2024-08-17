package com.alarm.alarmclock.supportingStopwatchClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.alarmclock.R;

import java.util.List;

public class LapTimeAdapter extends RecyclerView.Adapter<LapTimeAdapter.LapTimeViewHolder> {
    private final List<LapTime> lapTimes;

    public LapTimeAdapter(List<LapTime> lapTimes) {
        this.lapTimes = lapTimes;
    }

    @NonNull
    @Override
    public LapTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stopwatch_layout, parent, false);
        return new LapTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapTimeViewHolder holder, int position) {
        holder.bind(lapTimes.get(position));
    }

    @Override
    public int getItemCount() {
        return lapTimes.size();
    }

    static class LapTimeViewHolder extends RecyclerView.ViewHolder {
        private final TextView lapTimeTextView;

        public LapTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            lapTimeTextView = itemView.findViewById(R.id.timeLap);
        }

        public void bind(LapTime lapTime) {
            lapTimeTextView.setText(lapTime.getTime());
        }
    }
}
