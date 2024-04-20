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
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.events.MakeEventFragment;
import com.timetwist.events.ViewEventsFragment;

public class HomeFragment extends Fragment {
    private Button mMakeEvent, viewEvents;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();

        configureMakeEventButton();
        configureViewEvensButton();
    }

    private void configureMakeEventButton() {
        mMakeEvent.setOnClickListener(v -> {
            if (checkIfVerified()) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.replace(new MakeEventFragment());
                }
            } else {
                Toast.makeText(requireActivity(), "User isn't authorized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureViewEvensButton() {
        viewEvents.setOnClickListener(v -> {
            if (checkIfVerified()) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.replace(new ViewEventsFragment());
                }
            } else {
                Toast.makeText(requireActivity(), "User isn't authorized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkIfVerified() {
        return mAuth.getCurrentUser() != null;
    }
}