package com.alarm.alarmclock.supportingWorldClockClasses;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.alarmclock.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.TimeZone;

public class WorldClockAdapter extends RecyclerView.Adapter<WorldClockAdapter.ViewHolder> {

    private List<WorldClock> worldClockList;

    public WorldClockAdapter(List<WorldClock> worldClockList) {
        this.worldClockList = worldClockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.world_clock_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Add code to fetch and display the current time based on timeZone
        WorldClock worldClock = worldClockList.get(position);

        holder.cityName.setText(worldClock.getCityName());
        holder.currentTime.setText(worldClock.getTime());
        holder.currentTime.setText(worldClock.getTimeZone());

        // Fetch current time based on time zone
        TimeZone timeZone = TimeZone.getTimeZone(worldClock.getTimeZone());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a"); // 12-hour format with AM/PM
        sdf.setTimeZone(timeZone);
        String currentTime = sdf.format(new Date());

        holder.currentTime.setText(currentTime);

    }

    @Override
    public int getItemCount() {
        return worldClockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView cityName;
        public TextView currentTime;
        public TextView timeZone;

        public ViewHolder(View view) {
            super(view);
            cityName = view.findViewById(R.id.locationName);
            currentTime = view.findViewById(R.id.currentTime);
        }
    }
}
