package com.timetwist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.account.LoginActivity;
import com.timetwist.account.LoginRegisterFragment;
import com.timetwist.account.RegisterActivity;
import com.timetwist.bottombar.HomeFragment;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.bottombar.ProfileFragment;
import com.timetwist.events.MakeEventFragment;
import com.timetwist.events.NotVerifiedEventsFragment;
import com.timetwist.events.ViewEventsFragment;
import com.timetwist.favoritelocations.FavoriteLocationsFragment;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.info.AddMarkerWithKeyFragment;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class ActivityUtils {
    private static ActivityUtils mInstance;
    public final HomeFragment HOME_FRAGMENT = new HomeFragment();
    public final MapFragment MAP_FRAGMENT = new MapFragment();
    public final ProfileFragment PROFILE_FRAGMENT = new ProfileFragment();
    public final LoginRegisterFragment LOGIN_REGISTER_FRAGMENT = new LoginRegisterFragment();
    public final MakeEventFragment MAKE_EVENT_FRAGMENT = new MakeEventFragment();
    public final ViewEventsFragment VIEW_EVENTS_FRAGMENT = new ViewEventsFragment();
    public final NotVerifiedEventsFragment NOT_VERIFIED_EVENTS_FRAGMENT = new NotVerifiedEventsFragment();
    public final FavoriteLocationsFragment FAVORITE_LOCATIONS_FRAGMENT = new FavoriteLocationsFragment();
    public final AddMarkerWithKeyFragment ADD_MARKER_WITH_KEY = new AddMarkerWithKeyFragment();
    public FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;

    public static synchronized ActivityUtils getInstance() {
        if (mInstance == null) mInstance = new ActivityUtils();
        return mInstance;
    }

    public static void changeToRegisterActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
        mInstance = null;
    }

    public static void changeToLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
        mInstance = null;
    }

    public static void changeToMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OpenProfileFragment", true);
        context.startActivity(intent);
    }

    public static void configureLoginRegisterToMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public boolean initialiseFragments(Context context) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (NetworkUtils.isInternetDisconnected(context)) return false;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;

        if (!MAKE_EVENT_FRAGMENT.isAdded()) {
            transaction.add(R.id.frameLayout, MAKE_EVENT_FRAGMENT)
                    .hide(MAKE_EVENT_FRAGMENT);
        }
        if (!VIEW_EVENTS_FRAGMENT.isAdded()) {
            transaction.add(R.id.frameLayout, VIEW_EVENTS_FRAGMENT)
                    .hide(VIEW_EVENTS_FRAGMENT);
        }
        if (!FAVORITE_LOCATIONS_FRAGMENT.isAdded()) {
            transaction.add(R.id.frameLayout, FAVORITE_LOCATIONS_FRAGMENT)
                    .hide(FAVORITE_LOCATIONS_FRAGMENT);
        }
        if (!MAP_FRAGMENT.isAdded()) {
            transaction.add(R.id.frameLayout, MAP_FRAGMENT)
                    .hide(MAP_FRAGMENT);
        }
        if (!PROFILE_FRAGMENT.isAdded()) {
            transaction.add(R.id.frameLayout, PROFILE_FRAGMENT)
                    .hide(PROFILE_FRAGMENT);
        }
        transaction.commit();
        return true;
    }

    public void replace(Fragment newFragment, Context context) {
        View view = ((Activity) context).getCurrentFocus();
        if (view != null) KeyboardUtils.hideKeyboardFrom(context, view);
        if (mFragmentManager == null) return;

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mCurrentFragment != null) transaction.hide(mCurrentFragment);

        mCurrentFragment = newFragment;
        if (!mCurrentFragment.isAdded())
            transaction.add(R.id.frameLayout, mCurrentFragment);

        transaction.show(mCurrentFragment);
        transaction.commitNow();
    }


    public void selectFragment(int tabId, Context context) {
        Fragment selectedFragment = null;

        if (tabId == R.id.home) {
            selectedFragment = HOME_FRAGMENT;
        } else if (tabId == R.id.profile) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                selectedFragment = LOGIN_REGISTER_FRAGMENT;
                replace(selectedFragment, context);
                return;
            }
            selectedFragment = PROFILE_FRAGMENT;
        } else if (tabId == R.id.map) {
            selectedFragment = MAP_FRAGMENT;
            Log.w("ActivityUtils", "MapFragment " + tabId);
        }
        if (selectedFragment == null) return;

        replace(selectedFragment, context);
    }

    public void chooseFragment(AnimatedBottomBar bottomBar, Intent intent, Context context) {
        if (!intent.getBooleanExtra("OpenProfileFragment", false)) return;
        Fragment selectedFragment = FirebaseAuth.getInstance().getCurrentUser() == null ?
                LOGIN_REGISTER_FRAGMENT : PROFILE_FRAGMENT;

        replace(selectedFragment, context);
        bottomBar.selectTabById(R.id.profile, false);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void ifUserAdmin(Context context, Runnable ifAdmin, Runnable ifNotAdmin) {
        FirestoreServices.getInstance().checkIfUserIsAdmin(isAdmin -> {
            if (isAdmin) {
                ToastUtils.show(context, "Logged as Admin");
                ifAdmin.run();
                return;
            }
            ifNotAdmin.run();
        });
    }
}