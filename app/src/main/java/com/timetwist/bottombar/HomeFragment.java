package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.databinding.FragmentHomeBinding;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding mBinding;
    private ActivityUtils mActivityUtils;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivityUtils = ActivityUtils.getInstance();
        updateAdminVisibility();

    }

    private void configureButtons(String type) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ToastUtils.show(requireContext(), "User isn't authorized");
            return;
        }
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet connection is required");
            return;
        }

        switch (type) {
            case "MAKE_EVENT":
                mActivityUtils.replace(mActivityUtils.MAKE_EVENT_FRAGMENT, requireContext());
                break;
            case "VIEW_EVENTS":
                mActivityUtils.replace(mActivityUtils.VIEW_EVENTS_FRAGMENT, requireContext());
                break;
            case "ADD_MARKER":
                mActivityUtils.replace(mActivityUtils.ADD_MARKER_WITH_KEY, requireContext());
                break;
            case "VIEW_UNVERIFIED_EVENTS":
                mActivityUtils.replace(mActivityUtils.NOT_VERIFIED_EVENTS_FRAGMENT, requireContext());
                break;
        }
    }

    private void updateAdminVisibility() {
        mActivityUtils.ifUserAdmin(requireContext(),
                this::setupAdminView,
                this::setupRegularView);
    }

    private void setupAdminView() {
        mBinding.adminSection.setVisibility(View.VISIBLE);
        mBinding.markerSection.setVisibility(View.GONE);
        mBinding.eventsSection.setVisibility(View.GONE);
        mBinding.acceptEvents.setOnClickListener(v -> configureButtons("VIEW_UNVERIFIED_EVENTS"));
    }

    private void setupRegularView() {
        mBinding.adminSection.setVisibility(View.GONE);
        mBinding.markerSection.setVisibility(View.VISIBLE);
        mBinding.eventsSection.setVisibility(View.VISIBLE);
        mBinding.createEvent.setOnClickListener(v -> configureButtons("MAKE_EVENT"));
        mBinding.viewEvents.setOnClickListener(v -> configureButtons("VIEW_EVENTS"));
        mBinding.addMarker.setOnClickListener(v -> configureButtons("ADD_MARKER"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
