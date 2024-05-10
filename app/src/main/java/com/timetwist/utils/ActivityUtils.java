package com.timetwist.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.account.LoginActivity;
import com.timetwist.account.LoginRegisterFragment;
import com.timetwist.account.RegisterActivity;
import com.timetwist.bottombar.FavoriteLocationsFragment;
import com.timetwist.bottombar.HomeFragment;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.bottombar.ProfileFragment;
import com.timetwist.events.MakeEventFragment;
import com.timetwist.events.ViewEventsFragment;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class ActivityUtils {
    private static ActivityUtils mInstance;
    public final HomeFragment HOME_FRAGMENT = new HomeFragment();
    public final MapFragment MAP_FRAGMENT = new MapFragment();
    public final ProfileFragment PROFILE_FRAGMENT = new ProfileFragment();
    public final LoginRegisterFragment LOGIN_REGISTER_FRAGMENT = new LoginRegisterFragment();
    public final MakeEventFragment MAKE_EVENT_FRAGMENT = new MakeEventFragment();
    public final ViewEventsFragment VIEW_EVENTS_FRAGMENT = new ViewEventsFragment();
    public final FavoriteLocationsFragment FAVORITE_LOCATIONS_FRAGMENT = new FavoriteLocationsFragment();
    private Fragment mCurrentFragment;

    private ActivityUtils() {
    }

    public static synchronized ActivityUtils getInstance() {
        if (mInstance == null) mInstance = new ActivityUtils();
        return mInstance;
    }

    public static void changeToRegisterActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    public static void changeToLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public void initializeFragments(FragmentManager fragmentManager) {
        if (fragmentManager == null) return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frameLayout, PROFILE_FRAGMENT).hide(PROFILE_FRAGMENT);
        transaction.add(R.id.frameLayout, FAVORITE_LOCATIONS_FRAGMENT).hide(FAVORITE_LOCATIONS_FRAGMENT);
        transaction.commit();
    }

    public static void changeToMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OpenProfileFragment", true);
        context.startActivity(intent);
    }

    public void replace(FragmentManager fragmentManager, Fragment newFragment) {
        if (fragmentManager == null) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mCurrentFragment != null) transaction.hide(mCurrentFragment);

        mCurrentFragment = newFragment;
        if (!mCurrentFragment.isAdded()) transaction.add(R.id.frameLayout, mCurrentFragment);

        transaction.show(mCurrentFragment);
        transaction.commit();
    }


    public void selectFragment(FragmentManager fragmentManager, int tabId) {
        Fragment selectedFragment = null;

        if (tabId == R.id.home) {
            selectedFragment = HOME_FRAGMENT;
            Log.w("ActivityUtils", "HomeFragment " + tabId);
        } else if (tabId == R.id.profile) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                selectedFragment = LOGIN_REGISTER_FRAGMENT;
                replace(fragmentManager, selectedFragment);
                return;
            }
            selectedFragment = PROFILE_FRAGMENT;
        } else if (tabId == R.id.map) {
            selectedFragment = MAP_FRAGMENT;
            Log.w("ActivityUtils", "MapFragment " + tabId);
        }
        if (selectedFragment == null) return;

        replace(fragmentManager, selectedFragment);
    }

    public void chooseFragment(FragmentManager fragmentManager,
                               AnimatedBottomBar mBottomBar, Intent intent) {
        if (!intent.getBooleanExtra("OpenProfileFragment", false)) return;

        Fragment selectedFragment = FirebaseAuth.getInstance().getCurrentUser() == null ?
                LOGIN_REGISTER_FRAGMENT : PROFILE_FRAGMENT;

        replace(fragmentManager, selectedFragment);
        mBottomBar.selectTabById(R.id.profile, false);
    }
}