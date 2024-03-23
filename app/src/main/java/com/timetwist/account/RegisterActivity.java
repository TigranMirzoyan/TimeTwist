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

public class RegisterActivity extends AppCompatActivity {
    private EditText  mEmail, mPassword;
    private Button mRegister;
    private TextView mSwitchToLogin;
    private ImageView mClose;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgister);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSwitchToLogin = findViewById(R.id.switchToLogin);
        mRegister = findViewById(R.id.registerBtn);
        mClose = findViewById(R.id.closeActivity);

        mAuth = FirebaseAuth.getInstance();

        configureLoginToggleBtn();
        configureRegisterBtn();
        configureCloseBtn();
    }

    private void configureRegisterBtn() {
        mRegister.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required");
                return;
            }

            if (password.length() < 8) {
                mPassword.setError("Password must be more than 7 letters");
                return;
            }

            createUser(email, password);
        });
    }

    private void configureLoginToggleBtn() {
        mSwitchToLogin.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void configureCloseBtn() {
        mClose.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User Created",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();

            } else {
                Toast.makeText(RegisterActivity.this, "Error! " +
                                Objects.requireNonNull(task.getException()).getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}