package com.timetwist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.annotations.NotNull;


import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    AnimatedBottomBar bottomBar;
    Fragment homeFragment = new HomeFragment();
    Fragment profileFragment = new ProfileFragment();
    Fragment currentFragment = homeFragment; // Keep track of the current fragment
    Fragment mapFragment = new MapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomBar = findViewById(R.id.bottom_bar);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.framelayout, mapFragment)
                .hide(mapFragment)
                .commit();

        replace(currentFragment);
        setupBottomBarItemSelection();

    }//============================onCreate End============================

    //----------------------------------Logic----------------------------------
    private void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(currentFragment)
                .commit();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!fragment.isAdded()) {
            transaction.add(R.id.framelayout, fragment);
        }

        transaction.show(fragment);
        transaction.commit();

        currentFragment = fragment;
    }

    private void setupBottomBarItemSelection() {
        bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NotNull AnimatedBottomBar.Tab tab1) {
                Fragment selectedFragment = null;

                if (tab1.getId() == R.id.home) {
                    selectedFragment = homeFragment;

                } else if (tab1.getId() == R.id.profile) {
                    selectedFragment = profileFragment;

                } else if (tab1.getId() == R.id.map) {
                    selectedFragment = mapFragment;
                }

                replace(selectedFragment);
            }

            @Override
            public void onTabReselected(int i, @NotNull AnimatedBottomBar.Tab tab) {
            }
        });
    }
    //--------------------------------Logic End--------------------------------

}//==============================Code End==============================