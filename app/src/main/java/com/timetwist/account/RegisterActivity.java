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

public class RegisterActivity extends AppCompatActivity {
    private FirebaseLoginRegister mLoginRegister;
    private EditText mUsername, mEmail, mPassword;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgister);

        TextView mSwitchToLogin = findViewById(R.id.switchToLogin);
        ImageView mClose = findViewById(R.id.closeActivity);
        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRegister = findViewById(R.id.registerButton);
        mLoginRegister = new FirebaseLoginRegister(this);

        mSwitchToLogin.setOnClickListener(v -> ActivityUtils.changeToLoginActivity(this));
        mClose.setOnClickListener(v -> ActivityUtils.changeToMainActivity(this));
        configureRegisterBtn();
    }

    private void configureRegisterBtn() {
        mRegister.setOnClickListener(v -> {
            String username = mUsername.getText().toString().trim();
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            boolean check = true;

            if (username.length() < 4) {
                mUsername.setError("Name should be at  4 letters");
                check = false;
            }

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required");
                check = false;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required");
                check = false;
            }

            if (password.length() < 8) {
                mPassword.setError("Password must be more than 7 letters");
                check = false;
            }

            if (check) {
                mLoginRegister.createUser(username, email, password);
            }
        });
    }
}