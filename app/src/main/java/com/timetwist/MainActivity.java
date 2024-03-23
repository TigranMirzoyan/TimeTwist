package com.timetwist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.account.LoginRegisterFragment;
import com.timetwist.bottombar.HomeFragment;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.bottombar.ProfileFragment;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    private AnimatedBottomBar bottomBar;
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment profileFragment = new ProfileFragment();
    public final Fragment loginRegisterFragment = new LoginRegisterFragment();
    private final Fragment mapFragment = new MapFragment();
    private Fragment currentFragment = homeFragment;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomBar = findViewById(R.id.bottomBar);
        mAuth = FirebaseAuth.getInstance();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frameLayout, mapFragment)
                .hide(mapFragment)
                .commit();

        replace(currentFragment);
        chooseFragment();
        setupBottomBarItemSelection();

    }

    public void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (!fragment.isAdded()) {
            transaction.add(R.id.frameLayout, fragment);
        } else {
            transaction.show(fragment);
        }

        transaction.commit();
        currentFragment = fragment;
    }


    private void setupBottomBarItemSelection() {
        bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                Fragment selectedFragment = null;


                if (tab1.getId() == R.id.home) {
                    selectedFragment = homeFragment;

                } else if (tab1.getId() == R.id.profile) {
                    if (mAuth.getCurrentUser() == null) {
                        selectedFragment = loginRegisterFragment;
                    } else {
                        selectedFragment = profileFragment;
                    }
                } else if (tab1.getId() == R.id.map) {
                    selectedFragment = mapFragment;
                }

                assert selectedFragment != null;
                replace(selectedFragment);
            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {
            }
        });
    }

    public void chooseFragment() {
        boolean openProfileFragment = getIntent().getBooleanExtra("OpenProfileFragment", false);
        if (!openProfileFragment) {
            return;
        }

        bottomBar.selectTabById(R.id.profile, false);
        Fragment selectedFragment;
        if (mAuth.getCurrentUser() == null) {
            selectedFragment = loginRegisterFragment;
        } else {
            selectedFragment = profileFragment;
        }
        replace(selectedFragment);
    }
}