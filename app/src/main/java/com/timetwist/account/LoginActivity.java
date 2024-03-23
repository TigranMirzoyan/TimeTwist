package com.timetwist.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.MainActivity;
import com.timetwist.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mSwitchToReg;
    private ImageView mClose;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSwitchToReg = findViewById(R.id.switchToReg);
        mLogin = findViewById(R.id.loginBtn);
        mClose = findViewById(R.id.closeActivity);

        mAuth = FirebaseAuth.getInstance();

        configureLoginToggleBtn();
        configureRegisterBtn();
        configureCloseBtn();
    }

    private void configureRegisterBtn() {
        mLogin.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required");
                return;
            } else if (password.length() < 8) {
                mPassword.setError("Password must be more than 7 letters");
                return;
            }

            createUser(email, password);
        });
    }


    private void configureLoginToggleBtn() {
        mSwitchToReg.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void configureCloseBtn() {
        mClose.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("OpenProfileFragment", true); // Extra to indicate opening the profile fragment
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }


    private void createUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Logged in Successfully",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Error!" +
                        Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}