package com.timetwist.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.utils.ActivityUtils;
import com.timetwist.R;
import com.timetwist.firebase.FirebaseLoginRegister;

public class LoginActivity extends AppCompatActivity {
    private FirebaseLoginRegister mLoginRegister;
    private EditText mEmail, mPassword;
    private Button mLogin;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView mClose = findViewById(R.id.closeActivity);
        TextView mSwitchToRegister = findViewById(R.id.switchToRegister);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.loginButton);
        mLoginRegister = new FirebaseLoginRegister(this);

        mSwitchToRegister.setOnClickListener(v -> ActivityUtils
                .changeToRegisterActivity(this));
        mClose.setOnClickListener(v -> ActivityUtils
                .changeToMainActivity(this));
        configureLoginButton();
    }

    private void configureLoginButton() {
        mLogin.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            boolean check = true;

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required");
                check = false;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required");
                check = false;
            } else if (password.length() < 8) {
                mPassword.setError("Password must be more than 7 letters");
                check = false;
            }

            if (check) {
                mLoginRegister.loginUser(email, password);
            }
        });
    }
}