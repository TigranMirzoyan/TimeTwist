package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.databinding.FragmentProfileBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding mBinding;
    private ActivityUtils mActivityUtils;
    private FirestoreServices mFirestoreServices;
    private String mUsername;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivityUtils = ActivityUtils.getInstance();
        mFirestoreServices = FirestoreServices.getInstance();
        updateAdminVisibility();

        mBinding.favoritePlaces.setOnClickListener(v -> configureFavoritePlacesButton());
        mBinding.logOut.setOnClickListener(v -> configureLogOutButton());
        loadProfileData();
    }

    private void loadProfileData() {
        mFirestoreServices.updateProfileUI((username, email) -> {
            mUsername = username;
            mBinding.username.setText(username);
            mBinding.email.setText(email);
        });
    }

    private void updateAdminVisibility() {
        mActivityUtils.ifUserAdmin(requireContext(),
                () -> mBinding.favoritePlaces.setVisibility(View.GONE),
                () -> mBinding.favoritePlaces.setVisibility(View.VISIBLE));
    }

    private void configureFavoritePlacesButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mActivityUtils.replace(mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT, requireContext());
    }

    private void configureLogOutButton() {
        FirebaseAuth.getInstance().signOut();
        ActivityUtils.changeToLoginActivity(requireActivity());
    }

    public String getUsername() {
        return mUsername;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}