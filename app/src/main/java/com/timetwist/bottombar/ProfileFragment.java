package com.timetwist.bottombar;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timetwist.MainActivity;
import com.timetwist.R;

public class ProfileFragment extends Fragment {
    private Button mLogOut;
    private TextView usernameTextView;
    private TextView emailTextView;
    private FirebaseAuth mAuth;
    private String mUsername;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogOut = view.findViewById(R.id.logOutButton);
        usernameTextView = view.findViewById(R.id.username);
        emailTextView = view.findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();

        configureLogOutButton();
        placeUsernameAndEmail();
    }

    private void configureLogOutButton() {
        mLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.replace(mainActivity.loginRegisterFragment);
                mainActivity.getMapFragment().getMap().clear();
                mainActivity.getMapFragment().addMarkersFromFirebase();
            }
            mAuth.signOut();
        });
    }

    private void placeUsernameAndEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        mUsername = document.getString("username");
                        String email = document.getString("email");

                        usernameTextView.setText(mUsername);
                        emailTextView.setText(email);
                    } else {
                        Log.d("ProfileFragment", "No such user");
                    }
                } else {
                    Log.d("ProfileFragment", "get failed with ", task.getException());
                }
            });
        }
    }

    public String getUsername(){
        return mUsername;
    }
}