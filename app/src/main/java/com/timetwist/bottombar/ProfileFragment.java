package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;

public class ProfileFragment extends Fragment {
    private Button mLogOut;
    private TextView mUsernameTextView, mEmailTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogOut = view.findViewById(R.id.logOutButton);
        mUsernameTextView = view.findViewById(R.id.username);
        mEmailTextView = view.findViewById(R.id.email);
        FirestoreServices service = new FirestoreServices();

        configureLogOutButton();
        service.updateProfileUI((username, email) -> {
            if(isAdded()) {
                mUsernameTextView.setText(username);
                mEmailTextView.setText(email);
            }
        });
    }

    private void configureLogOutButton() {
        mLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            clearUserData();
            ActivityUtils.changeToLoginActivity(requireActivity());
        });
    }

    private void clearUserData() {
        mUsernameTextView.setText("");
        mEmailTextView.setText("");
    }
}