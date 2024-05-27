package com.timetwist.info;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.databinding.FragmentCreateMarkerBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Objects;

public class CreateMarker extends DialogFragment {
    private final LatLng mMarkerLatLng;
    private FragmentCreateMarkerBinding mBinding;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private FirebaseUser mCurrentUser;
    private String mType;

    public CreateMarker(LatLng markerLatLng) {
        mMarkerLatLng = markerLatLng;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentCreateMarkerBinding.inflate(LayoutInflater.from(requireContext()));
        View view = mBinding.getRoot();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();
        setTypeSelectionListeners();

        mBinding.close.setOnClickListener(v -> dismiss());
        mBinding.save.setOnClickListener(v -> configureSaveButton());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void setTypeSelectionListeners() {
        mBinding.church.setOnClickListener(v -> setType("church", "Church"));
        mBinding.prehistoricSite.setOnClickListener(v -> setType("temple", "Temple"));
        mBinding.tree.setOnClickListener(v -> setType("nature", "Nature"));
        mBinding.defaultMarker.setOnClickListener(v -> setType("default_marker", "Default"));
    }

    private void setType(String type, String displayName) {
        mType = type;
        mBinding.showMarkerName.setText(displayName);
    }

    private void configureSaveButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }

        String name = mBinding.markerName.getText().toString().trim();
        String description = mBinding.markerDescription.getText().toString().trim();
        String type = mType;
        boolean check = false;

        if (mActivityUtils.MAP_FRAGMENT.checkIfMarkerWithSameNameExists(name)) {
            ToastUtils.show(requireContext(), "You have already marker with this name");
            check = true;
        }
        if (name.isEmpty()) {
            ToastUtils.show(requireContext(), "Marker name is required");
            check = true;
        } else if (mType == null) {
            ToastUtils.show(requireContext(), "You must select a type for the marker");
            check = true;
        }
        if (check) return;

        GeoPoint geoPoint = new GeoPoint(mMarkerLatLng.latitude, mMarkerLatLng.longitude);
        mFirestoreServices.addMarkerDb(mCurrentUser.getUid(), name, description, type, geoPoint,
                success -> {
                    Toast.makeText(requireContext(), success,
                            Toast.LENGTH_SHORT).show();
                    mActivityUtils.MAP_FRAGMENT.refreshCustomMarkers(true, name);
                    dismiss();
                },
                error -> Toast.makeText(requireContext(), error,
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}