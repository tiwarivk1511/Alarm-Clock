package com.android.alarmclock.ui.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkerParameters;

import com.android.alarmclock.R;
import com.android.alarmclock.databinding.FragmentAlarmBinding;
import com.android.alarmclock.supportingAlarmClasses.Alarm;
import com.android.alarmclock.supportingAlarmClasses.AlarmAdapter;
import com.android.alarmclock.supportingAlarmClasses.AlarmDatabase;
import com.android.alarmclock.supportingAlarmClasses.AlarmReceiver;
import com.android.alarmclock.supportingAlarmClasses.AlarmScheduler;
import com.android.alarmclock.supportingAlarmClasses.AlarmWorker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.app.Activity.RESULT_OK;
import static android.app.AlarmManager.RTC_WAKEUP;

public class AlarmFragment extends Fragment {

    private FragmentAlarmBinding binding;
    private String selectedRingtoneUri;
    private BroadcastReceiver alarmRemovalReceiver;
    private boolean isReceiverRegistered = false;
    private List<Alarm> alarms = new ArrayList<>();
    private AlarmDatabase alarmDatabase;
    private AlarmAdapter alarmAdapter;
    private AlarmScheduler alarmScheduler;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the alarm database
        alarmDatabase = AlarmDatabase.getDatabase(requireContext());

        // Initialize the AlarmScheduler
        alarmScheduler = new AlarmScheduler(requireContext());

        // Set up RecyclerView and Adapter
        alarmAdapter = new AlarmAdapter(alarms, getContext(), alarmDatabase, this::showEditAlarmDialog);
        binding.alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.alarmRecyclerView.setAdapter(alarmAdapter);

        // Load alarms from the database
        new Thread(() -> {
            alarms = alarmDatabase.alarmDao().getAllAlarms();
            requireActivity().runOnUiThread(() -> alarmAdapter.updateAlarms(alarms));
        }).start();

        binding.addAlarmBtn.setOnClickListener(v -> showAddAlarmDialog());

        // Initialize and register the receiver
        alarmRemovalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle the received intent
                String action = intent.getAction();
                if (action != null && action.equals("YOUR_ACTION")) {
                    // Perform the removal operation

                    // Remove the item from the RecyclerView
                    int position = intent.getIntExtra("position", -1);
                    if (position != -1) {
                        alarmAdapter.notifyItemRemoved(position);
                    }

                    // Remove the item from the list
                    int alarmId = intent.getIntExtra("alarmId", -1);
                    if (alarmId != -1) {
                        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                        Intent alarmIntent = new Intent(requireContext(), AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmId, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                        alarmManager.cancel(pendingIntent);
                    }

                    // Show a toast message
                    Toast.makeText(requireContext(), "Alarm removed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter("YOUR_ACTION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireContext().registerReceiver(alarmRemovalReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
        isReceiverRegistered = true;

        // Set up ItemTouchHelper for swipe-to-delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    alarmAdapter.removeItem(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(Color.RED)
                        .addSwipeRightActionIcon(R.drawable.baseline_delete_outline_24)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.alarmRecyclerView);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(alarmRemovalReceiver);
            isReceiverRegistered = false;
        }
    }

    @SuppressLint("DefaultLocale")
    private void showAddAlarmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_alarm, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        MaterialButton buttonPickTime = dialogView.findViewById(R.id.buttonPickTime);
        Spinner spinnerRepeatOption = dialogView.findViewById(R.id.spinnerRepeat);
        MaterialSwitch switchVibrate = dialogView.findViewById(R.id.switchVibrate);
        MaterialButton buttonSelectRingtone = dialogView.findViewById(R.id.buttonPickRingtone);
        EditText editTextTaskLabel = dialogView.findViewById(R.id.editTextTaskLabel);
        MaterialCardView buttonSaveAlarm = dialogView.findViewById(R.id.buttonSaveAlarm);

        // Time selection
        final int[] hour = new int[1];
        final int[] minute = new int[1];

        buttonPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setHour(currentHour)
                    .setMinute(currentMinute)
                    .setTitleText("Select Alarm Time")
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                int hourOfDay = timePicker.getHour();
                int minuteOfDay = timePicker.getMinute();

                // Convert the 24-hour format to 12-hour format
                int displayHour = hourOfDay % 12;
                if (displayHour == 0) displayHour = 12; // Handle midnight hour
                String period = hourOfDay < 12 ? "AM" : "PM";

                hour[0] = hourOfDay;
                minute[0] = minuteOfDay;
                buttonPickTime.setText(String.format("%02d:%02d %s", displayHour, minuteOfDay, period));
            });

            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        // Repeat option spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeatOption.setAdapter(adapter);
        final String[] repeatOption = new String[1];
        spinnerRepeatOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatOption[0] = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Select ringtone
        buttonSelectRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
            startActivityForResult(intent, 1);
        });

        // Save alarm
        buttonSaveAlarm.setOnClickListener(v -> {
            String taskLabel = editTextTaskLabel.getText().toString();
            boolean vibrate = switchVibrate.isChecked();
            if (hour[0] != 0 && minute[0] != 0 && taskLabel != null && !taskLabel.isEmpty()) {
                saveAlarm(hour[0], minute[0], repeatOption[0], vibrate, selectedRingtoneUri, taskLabel);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditAlarmDialog(Alarm alarm) {

        // Show the dialog
        // Variables for the dialog
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.edit_alarm, null);
        TextView textViewTitle = dialogView.findViewById(R.id.textViewTitle);
        TextView textViewTime = dialogView.findViewById(R.id.textViewTime);
        MaterialButton buttonPickTime = dialogView.findViewById(R.id.buttonPickTime); // Ensure this ID matches a MaterialButton in your layout
        TextView textViewRepeat = dialogView.findViewById(R.id.textViewRepeat);
        Spinner spinnerRepeat = dialogView.findViewById(R.id.spinnerRepeat);
        MaterialButton buttonPickRingtone = dialogView.findViewById(R.id.buttonPickRingtone); // Ensure this ID matches a MaterialButton in your layout
        MaterialSwitch switchVibrate = dialogView.findViewById(R.id.switchVibrate);
        TextInputEditText editTextTaskLabel = dialogView.findViewById(R.id.editTextTaskLabel);
        MaterialButton buttonSaveAlarm = dialogView.findViewById(R.id.buttonSaveAlarm); // Ensure this ID matches a MaterialButton in your layout

        // Populate fields with alarm data
        textViewTitle.setText("Edit Alarm");
        textViewTime.setText(alarm.getFormattedTime());
        switchVibrate.setChecked(alarm.isVibrate());
        editTextTaskLabel.setText(alarm.getTaskLabel());
        textViewRepeat.setText(alarm.getRepeatOption());

        // Set up repeat options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapter);

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Pick time button click listener
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
                String amPm = hour >= 12 ? "PM" : "AM";
                int displayHour = hour % 12;
                displayHour = displayHour == 0 ? 12 : displayHour;
                String time = String.format("%02d:%02d %s", displayHour, minute, amPm);
                textViewTime.setText(time);
                alarm.setHour(hour);
                alarm.setMinute(minute);
            });

            // Show the time picker dialog
            timePicker.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(), "TIME_PICKER");
        });

        // Pick ringtone button click listener
        buttonPickRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
            ((AppCompatActivity) requireContext()).startActivityForResult(intent, 1); // Use startActivityForResult if using Activity
        });

        // Save alarm button click listener
        buttonSaveAlarm.setOnClickListener(v -> {
            // Save the updated alarm details
            alarm.setTaskLabel(editTextTaskLabel.getText().toString());
            alarm.setVibrate(switchVibrate.isChecked());
            alarm.setRepeatOption(spinnerRepeat.getSelectedItem().toString());

            new Thread(() -> alarmDatabase.alarmDao().update(alarm)).start();

            // Update the alarm in the AlarmScheduler
            updateAlarm(alarm.getId(), alarm.getHour(), alarm.getMinute(), alarm.getRepeatOption(), alarm.isVibrate(), alarm.getRingtoneUri(), alarm.getTaskLabel());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                alarmScheduler.scheduleAlarm(alarm.getId(), Calendar.getInstance());
            }

            Toast.makeText(requireContext(), "Alarm updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Save alarm
    private void saveAlarm(int hour, int minute, String repeatOption, boolean vibrate, String ringtoneUri, String taskLabel) {
        Alarm newAlarm = new Alarm();
        newAlarm.setHour(hour);
        newAlarm.setMinute(minute);
        newAlarm.setRepeatOption(repeatOption);
        newAlarm.setVibrate(vibrate);
        newAlarm.setRingtoneUri(ringtoneUri);
        newAlarm.setTaskLabel(taskLabel);
        newAlarm.setEnabled(true); // Set default value for enabled state

        // Save the alarm in the database
        new Thread(() -> {
            alarmDatabase.alarmDao().insert(newAlarm);

            // Update the RecyclerView on the main thread
            requireActivity().runOnUiThread(() -> {
                alarms.add(newAlarm);
                alarmAdapter.updateAlarms(alarms);
            });
            // Schedule the alarm with AlarmManager
            scheduleAlarm(newAlarm);
        }).start();
    }

    private void scheduleAlarm(@NonNull Alarm alarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Schedule the main alarm with AlarmManager
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        alarmIntent.putExtra("ALARM_ID", alarm.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                alarm.getId(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // Schedule the notification 5 minutes before the alarm
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        notificationCalendar.set(Calendar.MINUTE, alarm.getMinute() - 5);
        notificationCalendar.set(Calendar.SECOND, 0);
        notificationCalendar.set(Calendar.MILLISECOND, 0);

        long delay = notificationCalendar.getTimeInMillis() - System.currentTimeMillis();

        // Use WorkManager to handle the notification
        WorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(AlarmWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder().putInt(AlarmWorker.ALARM_ID, alarm.getId()).build())
                .build();

        WorkManager.getInstance(getContext()).enqueue(notificationWorkRequest);
    }


    private void updateAlarm(int id, int hour, int minute, String repeatOption, boolean vibrate, String ringtoneUri, String taskLabel) {
        new Thread(() -> {
            // Fetch the existing alarm from the database
            Alarm existingAlarm = alarmDatabase.alarmDao().getAlarmById(id);
            if (existingAlarm != null) {
                existingAlarm.setHour(hour);
                existingAlarm.setMinute(minute);
                existingAlarm.setRepeatOption(repeatOption);
                existingAlarm.setVibrate(vibrate);
                existingAlarm.setRingtoneUri(ringtoneUri);
                existingAlarm.setTaskLabel(taskLabel);

                // Update the alarm in the database
                alarmDatabase.alarmDao().update(existingAlarm);

                // Update the RecyclerView on the main thread
                requireActivity().runOnUiThread(() -> {
                    int position = findAlarmPosition(id);
                    if (position != -1) {
                        alarms.set(position, existingAlarm);
                        alarmAdapter.updateAlarms(alarms);
                    }
                });

                // Reschedule the alarm
                scheduleAlarm(existingAlarm);
            }
        }).start();
    }

    private int findAlarmPosition(int id) {
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtoneUri != null) {
                selectedRingtoneUri = ringtoneUri.toString();
                // Show a toast message or update UI to reflect selected ringtone
                Toast.makeText(requireContext(), "Ringtone selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}