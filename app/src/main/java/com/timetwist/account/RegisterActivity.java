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
import com.google.firebase.firestore.FirebaseFirestore;
import com.timetwist.MainActivity;
import com.timetwist.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText mUsername, mEmail, mPassword;
    private Button mRegister;
    private TextView mSwitchToLogin;
    private ImageView mClose;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgister);
        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSwitchToLogin = findViewById(R.id.switchToLogin);
        mRegister = findViewById(R.id.registerButton);
        mClose = findViewById(R.id.closeActivity);
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        configureLoginToggleBtn();
        configureRegisterBtn();
        configureCloseBtn();
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

            if (check){
                createUser(username, email, password);
            }
        });
    }

    private void configureLoginToggleBtn() {
        mSwitchToLogin.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }

    private void changeActivities() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.putExtra("OpenProfileFragment", true);
        startActivity(intent);
        finish();
    }

    private void configureCloseBtn() {
        mClose.setOnClickListener(v -> changeActivities());
    }


    private void createUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Objects.requireNonNull(currentUser).sendEmailVerification().addOnCompleteListener(task1 -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Error! " +
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(RegisterActivity.this, "User registered successfully." +
                                    " Please verify your Email",
                            Toast.LENGTH_SHORT).show();
                    mEmail.setText("");
                    mPassword.setText("");

                    createUserInDB(username, email, password, currentUser);
                    changeActivities();
                });
            } else {
                Toast.makeText(RegisterActivity.this, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createUserInDB(final String username, final String email, final String password, FirebaseUser currentUser) {
        String userId = currentUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        db.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    mUsername.setText("");
                    mEmail.setText("");
                    mPassword.setText("");
                });
    }
}