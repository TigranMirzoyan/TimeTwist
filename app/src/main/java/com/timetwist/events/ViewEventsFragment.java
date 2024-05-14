package com.timetwist.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

public class ViewEventsFragment extends Fragment {
    private final List<Event> mRandomEventList = new ArrayList<>();
    private final List<Event> mEventList = new ArrayList<>();
    private ActivityUtils mActivityUtils;
    private FirestoreServices mFirestoreServices;
    private FirebaseUser mCurrentUser;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private Button mBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button refreshButton = view.findViewById(R.id.refresh);
        mActivityUtils = ActivityUtils.getInstance();
        mFirestoreServices = FirestoreServices.getInstance();

        mBack = view.findViewById(R.id.closeFragment);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mSearchView = view.findViewById(R.id.searchView);
        mSearchView.clearFocus();

        putRandomEvents();
        refreshButton.setOnClickListener(v -> putRandomEvents());
        configureBackButton();
        configureSearchView();
    }

    private void putRandomEvents() {
        if (mCurrentUser == null) {
            Toast.makeText(requireContext(), "No Wi-Fi connection", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestoreServices.getRandomEvents(
                (eventList, randomEventList) -> {
                    mEventList.clear();
                    mRandomEventList.clear();
                    mEventList.addAll(eventList);
                    mRandomEventList.addAll(randomEventList);

                    if (eventList.isEmpty()) {
                        Toast.makeText(getContext(), "No events found", Toast.LENGTH_LONG).show();
                    } else {
                        ShowEventFragment adapter = new ShowEventFragment(requireContext(), mRandomEventList);
                        mRecyclerView.setAdapter(adapter);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    }

                    Log.e("Event Size", "Total events: " + mEventList.size() + ", Random events: " + mRandomEventList.size());
                },
                error -> Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show()
        );
    }

    private void configureBackButton() {
        mBack.setOnClickListener(v -> {
            if (mCurrentUser != null) {
                if (requireActivity() instanceof MainActivity) {
                    mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, requireContext());
                }
            }
        });
    }

    private void configureSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });
    }

    private void filterList(String text) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : mEventList) {
            if (event.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(event);
            }
        }

        ShowEventFragment adapter = new ShowEventFragment(requireContext(), filteredList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
}