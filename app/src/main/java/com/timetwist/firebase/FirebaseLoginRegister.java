package com.timetwist.firebase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Objects;

public class FirebaseLoginRegister {
    private final FirebaseAuth mAuth;
    private final FirestoreServices mFirestoreServices;
    private final Context mContext;

    public FirebaseLoginRegister(Context context) {
        mContext = context;

        mAuth = FirebaseAuth.getInstance();
        mFirestoreServices = FirestoreServices.getInstance();
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
                mAuth.signOut();
                ToastUtils.show(mContext, "Email isn't verified");
                return;
            }
            if (!task.isSuccessful()) {
                ToastUtils.show(mContext, "Login failed: " +
                        Objects.requireNonNull(task.getException()).getMessage());
                return;
            }

            ToastUtils.show(mContext, "Logged in Successfully");
            new Handler(Looper.getMainLooper()).postDelayed(() -> ActivityUtils
                    .configureLoginRegisterToMainActivity(mContext), 250);

        });
    }

    public void createUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                ToastUtils.show(mContext, "The email address is already in use by another account");
                return;
            }
            Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task1 -> {
                if (!task.isSuccessful()) {
                    ToastUtils.show(mContext, "Error! Maybe this account already exists?");
                    return;
                }

                ToastUtils.show(mContext, "User registered successfully.");
                mFirestoreServices.createUserInDB(username, email, password);
                new Handler(Looper.getMainLooper()).postDelayed(() -> ActivityUtils
                        .configureLoginRegisterToMainActivity(mContext), 250);
            });
        });
    }
}