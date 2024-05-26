package com.timetwist.account;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.databinding.ActivityRegisterBinding;
import com.timetwist.firebase.FirebaseLoginRegister;
import com.timetwist.utils.ActivityUtils;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding mBinding;
    private FirebaseLoginRegister mFirebaseLoginRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mFirebaseLoginRegister = new FirebaseLoginRegister(this);

        mBinding.switchToLogin.setOnClickListener(v -> ActivityUtils
                .changeToLoginActivity(this));
        mBinding.close.setOnClickListener(v -> ActivityUtils
                .changeToMainActivity(this));
        mBinding.register.setOnClickListener(v -> configureRegisterBtn());
    }

    private void configureRegisterBtn() {
        String username = mBinding.username.getText().toString().trim();
        String email = mBinding.email.getText().toString().trim();
        String password = mBinding.password.getText().toString().trim();


        if (checkForErrors(username, email, password))
            mFirebaseLoginRegister.createUser(username, email, password);
    }

    private boolean checkForErrors(String username, String email, String password) {
        boolean check = true;

        if (username.length() < 4) {
            mBinding.username.setError("Username should be >= 4 letters");
            check = false;
        } else if (username.length() > 16) {
            mBinding.username.setError("Username should be <= 16 letters");
            check = false;
        }

        if (TextUtils.isEmpty(email)) {
            mBinding.email.setError("Email is Required");
            check = false;
        }

        if (TextUtils.isEmpty(password)) {
            mBinding.password.setError("Password is Required");
            check = false;
        } else if (password.length() < 8) {
            mBinding.password.setError("Password must be >= 8 letters");
            check = false;
        } else if (password.length() > 50) {
            mBinding.password.setError("Password must be <= 50 letters");
            check = false;
        }
        return check;
    }
}