package com.android.alarmclock.ui.world_clock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.alarmclock.R;
import com.android.alarmclock.databinding.FragmentWorldClockBinding;
import com.android.alarmclock.supportingWorldClockClasses.WorldClock;
import com.android.alarmclock.supportingWorldClockClasses.WorldClockAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class WorldClockFragment extends Fragment {

    private FragmentWorldClockBinding binding;
    private RecyclerView recyclerView;
    private WorldClockAdapter adapter;
    private List<WorldClock> worldClockList;
    private Handler handler = new Handler();
    private Runnable updateTimeRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorldClockBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        worldClockList = new ArrayList<>();
        adapter = new WorldClockAdapter(worldClockList);
        recyclerView = binding.recyclerViewWorldClock;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        startUpdatingTime(); // Start periodic updates for world clocks

        EditText editTextSearch = binding.editTextSearch;

        List<WorldClock> worldClockList = new ArrayList<>();
        List<WorldClock> filteredWorldClockList = new ArrayList<>();

        String[] availableTimeZones = TimeZone.getAvailableIDs();
        for (String timeZone : availableTimeZones) {
            String cityName = timeZone.replace('_', ' ');
            WorldClock worldClock = new WorldClock(cityName, timeZone);
            worldClockList.add(worldClock);
            filteredWorldClockList.add(worldClock);
        }

        WorldClockAdapter adapter = new WorldClockAdapter(filteredWorldClockList);
        recyclerView.setAdapter(adapter);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().toLowerCase();
                filteredWorldClockList.clear();
                if (searchText.isEmpty()) {
                    filteredWorldClockList.addAll(worldClockList);
                } else {
                    for (WorldClock worldClock : worldClockList) {
                        if (worldClock.getCityName().toLowerCase().contains(searchText)) {
                            filteredWorldClockList.add(worldClock);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }

    private void startUpdatingTime() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                for (WorldClock worldClock : worldClockList) {
                    worldClock.updateTime();
                }
                adapter.notifyDataSetChanged();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(updateTimeRunnable);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable); // Stop updating time when fragment is destroyed
    }
}
