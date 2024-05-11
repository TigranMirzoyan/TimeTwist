package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.MainActivity;
import com.timetwist.R;

public class HomeFragment extends Fragment {
    private ActivityUtils mActivityUtils;
    private Button mMakeEvent, viewEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMakeEvent = view.findViewById(R.id.makeEvent);
        viewEvents = view.findViewById(R.id.viewEvents);
        mActivityUtils = ActivityUtils.getInstance();

        configureMakeEventButton();
        configureViewEventsButton();
    }

    private void configureMakeEventButton() {
        mMakeEvent.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null || !(requireActivity() instanceof MainActivity)) {
                Toast.makeText(requireActivity(), "User isn't authorized", Toast.LENGTH_SHORT).show();
                return;
            }
            mActivityUtils.replace(requireActivity().getSupportFragmentManager(), mActivityUtils.MAKE_EVENT_FRAGMENT,requireContext());
        });
    }

    private void configureViewEventsButton() {
        viewEvents.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null || !(requireActivity() instanceof MainActivity)) {
                Toast.makeText(requireActivity(), "User isn't authorized", Toast.LENGTH_SHORT).show();
                return;
            }
            mActivityUtils.replace(requireActivity().getSupportFragmentManager(), mActivityUtils.VIEW_EVENTS_FRAGMENT,requireContext());
        });
    }
}