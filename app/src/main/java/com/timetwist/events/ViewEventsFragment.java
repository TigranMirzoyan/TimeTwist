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

import com.timetwist.databinding.FragmentViewEventsBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class ViewEventsFragment extends Fragment {
    private FragmentViewEventsBinding mBinding;
    private final List<Event> mEventList = new ArrayList<>();
    private final List<Event> mDisplayedEvents = new ArrayList<>();
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private ShowEventFragment mAdapter;
    private int mEventState = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentViewEventsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mBinding.back.setOnClickListener(v ->
                mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, requireContext()));
        mBinding.refresh.setOnClickListener(v -> configureRefreshButton());
        mBinding.globalEvents.setOnClickListener(v -> configureGlobalEventsButton());
        mBinding.myEvents.setOnClickListener(v -> configureChangeEventsButton());
        mBinding.joinedEvents.setOnClickListener(v -> configureJoinedEventsButton());
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mAdapter = new ShowEventFragment(requireContext(), mDisplayedEvents, mEventState);
        mBinding.recyclerView.setAdapter(mAdapter);

        getEvents();
        setupSearchView();
    }

    private void getEvents() {
        mFirestoreServices.getEvents((eventList, randomEventList) -> {
            mEventList.clear();
            mEventList.addAll(eventList);
            filter("");
        }, error -> ToastUtils.show(requireContext(), "Error: " + error));
    }

    private void getMyEvents() {
        mFirestoreServices.getMyEvents(myEvents -> {
            mEventList.clear();
            mEventList.addAll(myEvents);
            filter("");
        }, error -> ToastUtils.show(requireContext(), "Error: " + error));
    }

    private void getJoinedEvents() {
        mFirestoreServices.getJoinedEvents(myEvents -> {
            mEventList.clear();
            mEventList.addAll(myEvents);
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
        switch (mEventState) {
            case 1:
                getMyEvents();
                break;
            case 2:
                getJoinedEvents();
                break;
            default:
                getEvents();
                break;
        }
    }

    private void configureGlobalEventsButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mEventState = 0;
        getEvents();
        mAdapter.updateEventState(mEventState);
    }

    private void configureChangeEventsButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mEventState = 1;
        getMyEvents();
        mAdapter.updateEventState(mEventState);
    }

    private void configureJoinedEventsButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mEventState = 2;
        getJoinedEvents();
        mAdapter.updateEventState(mEventState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}