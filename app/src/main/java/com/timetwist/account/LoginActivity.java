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
import com.google.firebase.auth.FirebaseUser;
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
        mSwitchToReg = findViewById(R.id.switchToRegister);
        mLogin = findViewById(R.id.loginButton);
        mClose = findViewById(R.id.closeActivity);

        mAuth = FirebaseAuth.getInstance();

        configureRegisterToggleBtn();
        configureLoginButton();
        configureCloseBtn();
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
                loginUser(email, password);
            }
        });
    }


    private void configureRegisterToggleBtn() {
        mSwitchToReg.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });
    }

    private void changeActivities() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("OpenProfileFragment", true);
        startActivity(intent);
        finish();
    }

    private void configureCloseBtn() {
        mClose.setOnClickListener(v -> changeActivities());
    }


    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null || !currentUser.isEmailVerified()) {
                mAuth.signOut();
                mPassword.setText("");
                Toast.makeText(LoginActivity.this, "Email isn't verified", Toast.LENGTH_SHORT).show();
            } else {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged in Successfully",
                            Toast.LENGTH_SHORT).show();
                    changeActivities();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}