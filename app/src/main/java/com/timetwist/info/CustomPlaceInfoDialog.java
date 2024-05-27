package com.timetwist.info;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.timetwist.R;
import com.timetwist.databinding.FragmentCustomPlaceInfoDialogBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.ui.manager.MarkerData;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.Base64Utils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Objects;

public class CustomPlaceInfoDialog extends DialogFragment {
    private FragmentCustomPlaceInfoDialogBinding mBinding;
    private final String mTitle;
    private final String mDescription;
    private final LatLng mLatLng;
    private final String mMarkerId;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private boolean isEditing = false;

    public CustomPlaceInfoDialog(String title, String description, String markerId, LatLng latLng) {
        mTitle = title;
        mDescription = description;
        mMarkerId = markerId;
        mLatLng = latLng;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentCustomPlaceInfoDialogBinding.inflate(LayoutInflater.from(requireContext()));
        View view = mBinding.getRoot();

        mActivityUtils = ActivityUtils.getInstance();
        mFirestoreServices = FirestoreServices.getInstance();

        mBinding.placeTitle.setText(mTitle);
        mBinding.placeDescription.setText(mDescription.isEmpty() ? "You did not add text when creating the Marker" : mDescription);
        mBinding.cancelID.setOnClickListener(v -> dismiss());
        mBinding.deleteButton.setOnClickListener(v -> configureDeleteButton());
        mBinding.shareBtn.setOnClickListener(v -> configureShareButton());
        mBinding.editButton.setOnClickListener(v -> configureEditButton());
        mBinding.saveButton.setOnClickListener(v -> saveChanges());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void configureDeleteButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireActivity(), "Internet required");
            return;
        }
        mFirestoreServices.deleteCustomMarker(mMarkerId,
                success -> {
                    ToastUtils.show(requireContext(), success);
                    mActivityUtils.MAP_FRAGMENT.refreshCustomMarkers(false, mTitle);
                    dismiss();
                },
                error -> ToastUtils.show(requireContext(), error));
    }

    private void configureShareButton() {
        String encodedData = Base64Utils.encode(new MarkerData(mLatLng.latitude, mLatLng.longitude));
        dismiss();
        ShareDialog dialogFragment = new ShareDialog(encodedData);
        dialogFragment.show(mActivityUtils.mFragmentManager, "ShareDialog");
    }

    private void configureEditButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireActivity(), "Internet required");
            return;
        }

        isEditing = !isEditing;
        if (isEditing) {
            enableEditing();
            return;
        }
        offEditing();
    }

    private void enableEditing() {
        mBinding.placeTitle.setEnabled(true);
        mBinding.placeDescription.setEnabled(true);
        mBinding.saveButton.setVisibility(View.VISIBLE);
        updateEditButtonDrawable();
    }

    private void offEditing() {
        mBinding.placeTitle.setEnabled(false);
        mBinding.placeDescription.setEnabled(false);
        mBinding.saveButton.setVisibility(View.GONE);
        mBinding.placeTitle.setText(mTitle);
        mBinding.placeDescription.setText(mDescription);
        updateEditButtonDrawable();
    }

    private void saveChanges() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireActivity(), "Internet required");
            return;
        }

        String newTitle = mBinding.placeTitle.getText().toString();
        String newDescription = mBinding.placeDescription.getText().toString();
        mFirestoreServices.updateCustomMarker(mMarkerId, newTitle, newDescription,
                success -> {
                    ToastUtils.show(requireContext(), success);
                    dismiss();
                    mActivityUtils.MAP_FRAGMENT.refreshCustomMarkers(false, mTitle);
                    mActivityUtils.MAP_FRAGMENT.refreshCustomMarkers(true, newTitle);
                },
                error -> ToastUtils.show(requireContext(), error));
    }

    public void updateEditButtonDrawable() {
        mBinding.editButton.setBackground(ContextCompat.getDrawable(requireContext(),
                isEditing ? R.drawable.edit_off_button :
                        R.drawable.edit_button));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}