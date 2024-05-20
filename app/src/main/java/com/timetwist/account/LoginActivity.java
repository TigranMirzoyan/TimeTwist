package com.timetwist.account;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.databinding.ActivityLoginBinding;
import com.timetwist.firebase.FirebaseLoginRegister;
import com.timetwist.utils.ActivityUtils;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding mBinding;
    private FirebaseLoginRegister mLoginRegister;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mLoginRegister = new FirebaseLoginRegister(this);

        mBinding.switchToRegister.setOnClickListener(v -> ActivityUtils
                .changeToRegisterActivity(this));
        mBinding.closeActivity.setOnClickListener(v -> ActivityUtils
                .changeToMainActivity(this));
        mBinding.loginButton.setOnClickListener(v -> configureLoginButton());
    }

    private void configureLoginButton() {
        String email = mBinding.email.getText().toString().trim();
        String password = mBinding.password.getText().toString().trim();
        boolean check = true;

        if (TextUtils.isEmpty(email)) {
            mBinding.email.setError("Email is Required");
            check = false;
        }

        if (TextUtils.isEmpty(password)) {
            mBinding.password.setError("Password is Required");
            check = false;
        } else if (password.length() < 8) {
            mBinding.password.setError("Password must be more than 7 letters");
            check = false;
        }

        if (check) mLoginRegister.loginUser(email, password);
    }
}