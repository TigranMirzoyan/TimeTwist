package com.timetwist.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.MainActivity;
import com.timetwist.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ViewEventsFragment extends Fragment {
    private final List<Event> randomEventList = new ArrayList<>();
    private final List<Event> eventList = new ArrayList<>();
    private ActivityUtils mActivityUtils;
    private Button mBack;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore db;
    private FirebaseUser mCurrentUser;
    private SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button refreshButton = view.findViewById(R.id.refresh);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        //mMyEvents = view.findViewById(R.id.myEvents);
        mBack = view.findViewById(R.id.closeFragment);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mSearchView = view.findViewById(R.id.searchView);
        mSearchView.clearFocus();

        fetchRandomEvents();
        refreshButton.setOnClickListener(v -> fetchRandomEvents());
        //configureMyEventsButton();
        configureBackButton();
        configureSearchView();
    }

    private void fetchRandomEvents() {
        if (mCurrentUser != null) {

            db.collection("Events").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Event event = document.toObject(Event.class);
                                eventList.add(event);
                            }

                            int eventListSize = eventList.size();
                            randomEventList.clear();

                            while (randomEventList.size() < Math.min(eventListSize, 10)) {
                                Event randomEvent = eventList.get(new Random().nextInt(eventListSize));
                                if (!randomEventList.contains(randomEvent)) {
                                    randomEventList.add(randomEvent);
                                }
                            }

                            ShowEventFragment adapter = new ShowEventFragment(requireContext(), randomEventList);
                            mRecyclerView.setAdapter(adapter);
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        }
                    });
        }
    }

    private void configureBackButton() {
        mBack.setOnClickListener(v -> {
            if (mCurrentUser != null) {
                if (requireActivity() instanceof MainActivity) {
                    mActivityUtils.replace(requireActivity().getSupportFragmentManager(), mActivityUtils.HOME_FRAGMENT);
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
        for (Event event : eventList) {
            if (event.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(event);
            }
        }

        ShowEventFragment adapter = new ShowEventFragment(requireContext(), filteredList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }



    /*
    private void configureMyEventsButton() {
        mMyEvents.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.replace(new MakeEventFragment());
                }
            } else {
                Toast.makeText(requireActivity(), "User isn't authorized", Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}