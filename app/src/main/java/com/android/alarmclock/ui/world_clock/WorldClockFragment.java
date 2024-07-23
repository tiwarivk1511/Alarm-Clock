package com.android.alarmclock.ui.world_clock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.alarmclock.R;
import com.android.alarmclock.databinding.FragmentWorldClockBinding;
import com.android.alarmclock.databinding.WorldClockLayoutBinding;
import com.android.alarmclock.supportingWorldClockClasses.WorldClock;
import com.android.alarmclock.supportingWorldClockClasses.WorldClockAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class WorldClockFragment extends Fragment {

    private FragmentWorldClockBinding binding;
    private RecyclerView recyclerView;
    private WorldClockAdapter adapter;
    private List<WorldClock> worldClockList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentWorldClockBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.fabAddWorldClock.setOnClickListener(v -> {
            // Show the add world clock dialog
            showAddWorldClockDialog();
        });

        return rootView;
    }

    private void showAddWorldClockDialog() {
        // Show the add world clock dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());  // Use 'this' if inside an Activity, otherwise use 'requireContext()' in Fragment
        View dialogView = getLayoutInflater().inflate(R.layout.search_world_time, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewWorldClock);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // Use 'this' if inside an Activity, otherwise use 'requireContext()' in Fragment

        EditText editTextSearch = dialogView.findViewById(R.id.editTextSearch);

        List<WorldClock> worldClockList = new ArrayList<>();
        List<WorldClock> filteredWorldClockList = new ArrayList<>();

        // Fetch all available time zone IDs
        String[] availableTimeZones = TimeZone.getAvailableIDs();
        for (String timeZone : availableTimeZones) {
            String cityName = timeZone.replace('_', ' '); // Convert underscore to space for better readability
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

        dialog.show();
    }

}
