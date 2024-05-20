package com.timetwist.info;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.timetwist.R;
import com.timetwist.databinding.FragmentGlobalPlaceInfoDialogBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Objects;

public class GlobalPlaceInfoDialog extends DialogFragment {
    private final OnFavoriteUpdateListener mUpdateListener;
    private final String mTitle;
    private final String mDescription;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private FragmentGlobalPlaceInfoDialogBinding mBinding;
    private boolean mButtonClicked;

    public interface OnFavoriteUpdateListener {
        void onFavoriteAdded(String title);

        void onFavoriteRemoved(String title);
    }

    public GlobalPlaceInfoDialog(String title, String description, Boolean bool,
                                 OnFavoriteUpdateListener updateListener) {
        mTitle = title;
        mDescription = description;
        mUpdateListener = updateListener;
        mButtonClicked = bool;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentGlobalPlaceInfoDialogBinding.inflate(LayoutInflater.from(requireContext()));
        View view = mBinding.getRoot();
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mBinding.placeDescription.setMovementMethod(new ScrollingMovementMethod());
        mBinding.placeTitle.setText(mTitle);
        mBinding.placeDescription.setText(mDescription);
        mBinding.cancelID.setOnClickListener(v -> dismiss());
        mBinding.favoriteLocation.setOnClickListener(v -> configureFavoriteButton());
        mBinding.readMore.setOnClickListener(v -> configureReadMoreButton());
        changeDrawable();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void configureFavoriteButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mButtonClicked = !mButtonClicked;
        changeDrawable();

        if (mButtonClicked) {
            mFirestoreServices.makeFavoriteLocation(mTitle,
                    success -> {
                        ToastUtils.show(requireContext(), success);
                        if (mUpdateListener != null) mUpdateListener.onFavoriteAdded(mTitle);
                    },
                    error -> ToastUtils.show(requireContext(), error));
            return;
        }

        mFirestoreServices.findFavoriteMarkerAndDelete(mTitle,
                success -> {
                    if (mUpdateListener == null) return;
                    mUpdateListener.onFavoriteRemoved(mTitle);
                    ToastUtils.show(requireContext(), success);
                },
                error -> ToastUtils.show(requireContext(), error));
    }

    private void changeDrawable() {
        mBinding.favoriteLocation.setBackground(ContextCompat.getDrawable(requireContext(),
                mButtonClicked ? R.drawable.favorite_button_clicked : R.drawable.favorite_button_not_clicked));
    }
    private void configureReadMoreButton(){
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        String url = "https://en.wikipedia.org/wiki/" + mTitle.replaceAll(" ", "_");
        mActivityUtils.MAP_FRAGMENT.configureWebView(url);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}