package com.timetwist;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;

import java.util.Objects;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    private ActivityUtils mActivityUtils;
    private AnimatedBottomBar mBottomBar;
    private boolean mIsMapInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomBar = findViewById(R.id.bottomBar);
        mActivityUtils = ActivityUtils.getInstance();
        mActivityUtils.replace(getSupportFragmentManager(), mActivityUtils.HOME_FRAGMENT);
        mActivityUtils.chooseFragment(getSupportFragmentManager(), mBottomBar, getIntent());
        mActivityUtils.initializeFragments(getSupportFragmentManager());

        setupBottomBarItemSelection();
    }

    private void setupBottomBarItemSelection() {
        mBottomBar.setOnTabInterceptListener((lastIndex, lastTab, newIndex, newTab) -> {
            int tabId = newTab.getId();
            if (!mIsMapInitialized && NetworkUtils.isWifiDisconnected(MainActivity.this)
                    && R.id.map == tabId) {
                Toast.makeText(MainActivity.this,
                        "Internet connection is required to view the map",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            if (R.id.map == tabId) {
                mIsMapInitialized = true;
            }
            if (R.id.map == Objects.requireNonNull(lastTab).getId()) {
                mActivityUtils.MAP_FRAGMENT.cancelDialog();
            }
            mActivityUtils.selectFragment(getSupportFragmentManager(), tabId);
            return true;
        });
    }
}