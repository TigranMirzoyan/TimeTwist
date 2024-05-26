package com.timetwist.events;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.timetwist.databinding.FragmentNotVerifiedEventsBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class NotVerifiedEventsFragment extends Fragment {
    private FragmentNotVerifiedEventsBinding mBinding;
    private final List<Event> mEventList = new ArrayList<>();
    private final List<Event> mDisplayedEvents = new ArrayList<>();
    private FirestoreServices mFirestoreServices;
    private ShowNotVerifiedEventsFragment mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentNotVerifiedEventsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        ActivityUtils activityUtils = ActivityUtils.getInstance();

        mBinding.back.setOnClickListener(v ->
                activityUtils.replace(activityUtils.HOME_FRAGMENT, requireContext()));
        mBinding.refresh.setOnClickListener(v -> configureRefreshButton());
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mAdapter = new ShowNotVerifiedEventsFragment(requireContext(), mDisplayedEvents);
        mBinding.recyclerView.setAdapter(mAdapter);

        getEvents();
        setupSearchView();
    }

    private void getEvents() {
        mFirestoreServices.getNotVerifiedEvents((eventList, randomEventList) -> {
            mEventList.clear();
            mEventList.addAll(eventList);
            filter("");
        }, error -> ToastUtils.show(requireContext(), "Error: " + error));
    }

    private void setupSearchView() {
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filter(String text) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : mEventList) {
            if (event.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(event);
            }
        }
        mDisplayedEvents.clear();
        mDisplayedEvents.addAll(filteredList);
        mAdapter.notifyDataSetChanged();
    }

    private void configureRefreshButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        getEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}