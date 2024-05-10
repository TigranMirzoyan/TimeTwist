package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.NetworkUtils;

public class ProfileFragment extends Fragment {
    private ActivityUtils mActivityUtils;
    private Button mLogOut, mFavoritePlaces;
    private TextView mUsernameTextView, mEmailTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogOut = view.findViewById(R.id.logOutButton);
        mFavoritePlaces = view.findViewById(R.id.favorite_places);
        mUsernameTextView = view.findViewById(R.id.username);
        mEmailTextView = view.findViewById(R.id.email);

        mActivityUtils = ActivityUtils.getInstance();
        FirestoreServices mFirestoreServices = FirestoreServices.getInstance();

        configureLogOutButton();
        configureFavoritePlacesButton();
        mFirestoreServices.updateProfileUI((username, email) -> {
            if (isAdded()) {
                mUsernameTextView.setText(username);
                mEmailTextView.setText(email);
            }
        });
    }

    private void configureLogOutButton() {
        mLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mUsernameTextView.setText("");
            mEmailTextView.setText("");
            ActivityUtils.changeToLoginActivity(requireActivity());
        });
    }

    private void configureFavoritePlacesButton() {
        mFavoritePlaces.setOnClickListener(v -> {
            if (NetworkUtils.isWifiDisconnected(requireContext())) {
                Toast.makeText(requireContext(), "Wifi Required",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mActivityUtils.replace(requireActivity().getSupportFragmentManager(),
                    mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT);
        });
    }
}