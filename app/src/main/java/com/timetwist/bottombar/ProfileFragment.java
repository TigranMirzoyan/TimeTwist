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
import com.google.firebase.auth.FirebaseUser;
import com.timetwist.MainActivity;
import com.timetwist.R;

public class ProfileFragment extends Fragment {
    private Button mLogOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mLogOut = view.findViewById(R.id.logOutBtn);
        TextView mEmail = view.findViewById(R.id.userEmail);
        FirebaseUser mUser = mAuth.getCurrentUser();

        assert mUser != null;
        mEmail.setText(mUser.getEmail());
        configureLogOutButton();

    }

    public void configureLogOutButton() {
        mLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.replace(mainActivity.loginRegisterFragment);
            }
        });
    }
}