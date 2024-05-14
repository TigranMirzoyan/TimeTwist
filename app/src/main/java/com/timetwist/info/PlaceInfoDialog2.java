package com.timetwist.info;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.ui.manager.MarkerData;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.Base64Utils;

import java.util.Objects;

public class PlaceInfoDialog2 extends DialogFragment {
    private final String mTitle;
    private final String mDescription;
    private final LatLng mLatLng;
    private final String mMarkerId;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private Button mDeleteButton, mShareButton;

    public PlaceInfoDialog2(String title, String description, String markerId, LatLng latLng) {
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
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog2, null);
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mDeleteButton = view.findViewById(R.id.deleteBtn);
        mShareButton = view.findViewById(R.id.shareBtn);
        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);

        title.setText(mTitle);
        description.setText(mDescription.isEmpty() ? "You did not add text " +
                "when creating the Marker" : mDescription);
        cancelButton.setOnClickListener(v -> dismiss());
        configureDeleteButton();
        configureShareButton();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);
        
        return dialog;
    }

    public void configureDeleteButton() {
        mDeleteButton.setOnClickListener(v -> mFirestoreServices.deleteCustomMarker(mMarkerId,
                success -> {
                    Toast.makeText(requireContext(), success,
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                    mActivityUtils.MAP_FRAGMENT.refreshMapMarkers(false, mTitle);
                },
                error -> Toast.makeText(getContext(), error,
                        Toast.LENGTH_SHORT).show()));
    }

    public void configureShareButton() {
        mShareButton.setOnClickListener(v -> {
            String encodedData = Base64Utils.encode(new MarkerData(mLatLng.latitude, mLatLng.longitude));

            dismiss();
            ShareDialog dialogFragment = new ShareDialog(encodedData);
            dialogFragment.show(mActivityUtils.mFragmentManager, "ShareDialog");
        });
    }
}