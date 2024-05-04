package com.timetwist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.utils.ActivityUtils;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    private AnimatedBottomBar mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomBar = findViewById(R.id.bottomBar);

        ActivityUtils.replace(this, ActivityUtils.HOME_FRAGMENT);
        ActivityUtils.chooseFragment(this, mBottomBar);
        setupBottomBarItemSelection();

    }

    private void setupBottomBarItemSelection() {
        mBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                int tabId = tab1.getId();
                ActivityUtils.selectFragment(MainActivity.this, tabId);
            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {
            }
        });
    }
}