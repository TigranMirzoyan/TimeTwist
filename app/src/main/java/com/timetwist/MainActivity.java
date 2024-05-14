package com.timetwist;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.custom.interfaces.OnMarkerSelectedListener;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;

import java.util.Objects;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity implements OnMarkerSelectedListener {
    private ActivityUtils mActivityUtils;
    private AnimatedBottomBar mBottomBar;
    private boolean mIsMapInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomBar = findViewById(R.id.bottomBar);
        mActivityUtils = ActivityUtils.getInstance();

        mActivityUtils.getFragmentManager(getSupportFragmentManager());
        mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, this);
        mActivityUtils.chooseFragment(mBottomBar, getIntent(), this);
        mIsMapInitialized = mActivityUtils.initialiseFragments(this);
        setupBottomBarItemSelection();
    }

    private void setupBottomBarItemSelection() {
        mBottomBar.setOnTabInterceptListener((lastIndex, lastTab, newIndex, newTab) -> {
            int tabId = newTab.getId();
            if (!mIsMapInitialized && NetworkUtils.isInternetDisconnected(MainActivity.this)
                    && R.id.map == tabId) {
                Toast.makeText(MainActivity.this,
                        "Internet connection is required to view the map",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            if (R.id.map == tabId) mIsMapInitialized = true;
            if (R.id.map == Objects.requireNonNull(lastTab).getId())
                mActivityUtils.MAP_FRAGMENT.cancelDialogAndMapClickListener();
            mActivityUtils.selectFragment(tabId, this);
            return true;
        });
    }

    public void onMarkerSelected(String markerName) {
        if (NetworkUtils.isInternetDisconnected(this)) {
            Toast.makeText(this, "Please turn on Wifi", Toast.LENGTH_SHORT).show();
            return;
        }

        mBottomBar.selectTabById(R.id.map, true);
        mActivityUtils.MAP_FRAGMENT.prepareZoomToFavoriteMarker(markerName);
    }

    public AnimatedBottomBar getBottomBar() {
        return mBottomBar;
    }
}