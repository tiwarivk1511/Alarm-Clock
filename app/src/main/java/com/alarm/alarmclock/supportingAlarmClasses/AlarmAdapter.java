package com.alarm.alarmclock.supportingAlarmClasses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.alarmclock.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList; // List of alarms
    private final Context context;
    private final AlarmDatabase alarmDatabase;
    private final OnAlarmClickListener onAlarmClickListener;

    public interface OnAlarmClickListener {
        void onAlarmClick(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarmList, Context context, AlarmDatabase alarmDatabase, OnAlarmClickListener onAlarmClickListener) {
        this.alarmList = alarmList;
        this.context = context;
        this.alarmDatabase = alarmDatabase;
        this.onAlarmClickListener = onAlarmClickListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_card, parent, false);
        return new AlarmViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);

        holder.alarmTime.setText(alarm.getFormattedTime());
        holder.alarmRepeatInfo.setText(alarm.getRepeatOption());

        // Set the initial switch state and track tint
        holder.alarmSwitch.setChecked(alarm.isEnabled());
        if (alarm.isEnabled()) {
            holder.alarmSwitch.setTrackTintList(context.getResources().getColorStateList(R.color.colorPrimary));
        } else {
            holder.alarmSwitch.setTrackTintList(context.getResources().getColorStateList(R.color.colorTextSecondary));
        }

        // Set the onCheckedChangeListener
        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setEnabled(isChecked);
            if (alarm.isEnabled()) {
                holder.alarmSwitch.setTrackTintList(context.getResources().getColorStateList(R.color.colorPrimary));
            } else {
                holder.alarmSwitch.setTrackTintList(context.getResources().getColorStateList(R.color.colorTextSecondary));
            }
            new Thread(() -> alarmDatabase.alarmDao().update(alarm)).start(); // Update alarm in the database
        });

        holder.alarmCard.setOnLongClickListener(v -> {
            // Show edit dialog for the alarm
            showEditAlarmDialog(alarm);
            return true; // Return true to indicate that the long click event has been handled
        });

        // Set card elevation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            holder.alarmCard.setCardElevation(16);
        }

        // Customize card background for better visual
        holder.alarmCard.setCardBackgroundColor(Color.WHITE);
        holder.alarmCard.setStrokeColor(Color.LTGRAY);
    }


    private void showEditAlarmDialog(Alarm alarm) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.edit_alarm, null);
        TextView textViewTitle = dialogView.findViewById(R.id.textViewTitle);
        TextView textViewTime = dialogView.findViewById(R.id.textViewTime);
        MaterialButton buttonPickTime = dialogView.findViewById(R.id.buttonPickTime);
        TextView textViewRepeat = dialogView.findViewById(R.id.textViewRepeat);
        Spinner spinnerRepeat = dialogView.findViewById(R.id.spinnerRepeat);

        TextInputEditText editTextTaskLabel = dialogView.findViewById(R.id.editTextTaskLabel);
        MaterialCardView buttonSaveAlarm = dialogView.findViewById(R.id.buttonSaveAlarm);

        textViewTitle.setText("Edit Alarm");

        // Populate fields with alarm data
        textViewTime.setText(alarm.getFormattedTime());

        editTextTaskLabel.setText(alarm.getTaskLabel());

        // Set up repeat options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapter);
        // Set spinner selection based on alarm data
        int repeatPosition = adapter.getPosition(alarm.getRepeatOption());
        spinnerRepeat.setSelection(repeatPosition >= 0 ? repeatPosition : 0);

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        buttonPickTime.setOnClickListener(v -> {
            // Create a MaterialTimePicker instance with 12-hour format
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                    .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                    .setTitleText("Select Alarm Time")
                    .build();

            // Set a listener for the time picker
            timePicker.addOnPositiveButtonClickListener(view -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = hour >= 12 ? "PM" : "AM"; // Determine AM/PM
                int displayHour = hour % 12; // Convert 24-hour format to 12-hour format
                displayHour = displayHour == 0 ? 12 : displayHour; // Handle midnight and noon
                String time = String.format("%02d:%02d %s", displayHour, minute, amPm);
                textViewTime.setText(time);
                alarm.setHour(hour);
                alarm.setMinute(minute); // Update alarm time
            });

            // Show the time picker dialog
            timePicker.show(((AppCompatActivity) context).getSupportFragmentManager(), "TIME_PICKER");
        });



        buttonSaveAlarm.setOnClickListener(v -> {
            // Save the updated alarm details
            alarm.setTaskLabel(editTextTaskLabel.getText().toString());

            alarm.setRepeatOption(spinnerRepeat.getSelectedItem().toString());

            new Thread(() -> alarmDatabase.alarmDao().update(alarm)).start();
            Toast.makeText(context, "Alarm updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public void removeItem(int position) {
        Alarm alarmToDelete = alarmList.get(position);
        alarmList.remove(position);
        notifyItemRemoved(position);
        new Thread(() -> alarmDatabase.alarmDao().delete(alarmToDelete)).start();
        Toast.makeText(context, "Alarm removed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAlarms(List<Alarm> newAlarms) {
        this.alarmList = newAlarms;
        notifyDataSetChanged();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        final TextView alarmTime;
        final TextView alarmRepeatInfo;
        final MaterialSwitch alarmSwitch;
        final MaterialCardView alarmCard;

        AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTime = itemView.findViewById(R.id.alarmTime);
            alarmRepeatInfo = itemView.findViewById(R.id.alarmRepeatInfo);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            alarmCard = itemView.findViewById(R.id.alarmCard);
        }
    }
}
