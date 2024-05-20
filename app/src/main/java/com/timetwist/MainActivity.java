package com.timetwist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.timetwist.databinding.ActivityMainBinding;
import com.timetwist.interfaces.OnMarkerSelectedListener;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Objects;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity implements OnMarkerSelectedListener {
    private ActivityMainBinding mBinding;
    private ActivityUtils mActivityUtils;
    private boolean mFragmentsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mActivityUtils = ActivityUtils.getInstance();

        setupBottomBarItemSelection();
        mActivityUtils.setFragmentManager(getSupportFragmentManager());
        mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, this);
        mActivityUtils.chooseFragment(mBinding.bottomBar, getIntent(), this);
        mFragmentsInitialized = mActivityUtils.initialiseFragments(this);
    }

    private void setupBottomBarItemSelection() {
        mBinding.bottomBar.setOnTabInterceptListener((lastIndex, lastTab, newIndex, newTab) -> {
            int tabId = newTab.getId();
            if (!mFragmentsInitialized && NetworkUtils.isInternetDisconnected(this)
                    && R.id.map == tabId) {
                ToastUtils.show(this,
                        "Internet connection is required to view the map");
                return false;
            }

            if (R.id.map == tabId) mFragmentsInitialized = true;
            if (R.id.map == Objects.requireNonNull(lastTab).getId())
                mActivityUtils.MAP_FRAGMENT.cancelDialogAndMapClickListener();
            mActivityUtils.selectFragment(tabId, this);
            return true;
        });
    }

    public void onMarkerSelected(String markerName) {
        if (NetworkUtils.isInternetDisconnected(this)) {
            ToastUtils.show(this, "Internet required");
            return;
        }

        mBinding.bottomBar.selectTabById(R.id.map, true);
        mActivityUtils.MAP_FRAGMENT.prepareZoomToFavoriteMarker(markerName);
    }

    public AnimatedBottomBar getBottomBar() {
        return mBinding.bottomBar;
    }
}