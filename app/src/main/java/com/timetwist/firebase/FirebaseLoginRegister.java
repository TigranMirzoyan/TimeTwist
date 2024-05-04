package com.timetwist.firebase;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.timetwist.utils.ActivityUtils;

import java.util.Objects;

public class FirebaseLoginRegister {
    private final FirebaseAuth mAuth;
    private final FirestoreServices mCreateUser;
    private final Context mContext;

    public FirebaseLoginRegister(Context context) {
        this.mContext = context;
        this.mAuth = FirebaseAuth.getInstance();
        mCreateUser = new FirestoreServices();
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (!Objects.requireNonNull(currentUser).isEmailVerified()) {
                mAuth.signOut();
                Toast.makeText(mContext, "Email isn't verified", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!task.isSuccessful()) {
                Toast.makeText(mContext, "Login failed: " + Objects.
                        requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(mContext, "Logged in Successfully", Toast.LENGTH_SHORT).show();
            ActivityUtils.changeToMainActivity(mContext);
        });
    }

    public void createUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(mContext, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Objects.requireNonNull(currentUser).sendEmailVerification().addOnCompleteListener(task1 -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(mContext, "Error! Maybe this account already exists?", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(mContext, "User registered successfully." +
                        "Please verify your Email", Toast.LENGTH_SHORT).show();
                mCreateUser.createUserInDB(username, email, password, currentUser);
                ActivityUtils.changeToMainActivity(mContext);
            });
        });
    }
}
