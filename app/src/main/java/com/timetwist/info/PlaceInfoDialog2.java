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

import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.firebase.FirestoreServices;

import java.util.Objects;

public class PlaceInfoDialog2 extends DialogFragment {
    private final MapFragment mMapFragment;
    private final String mTitle;
    private final String mDescription;
    private final String mMarkerId;
    private FirestoreServices mFirestoreServices;
    private Button mDeleteButton;

    public PlaceInfoDialog2(String mTitle, String mDescription,
                            MapFragment mMapFragment, String mMarkerId) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mMapFragment = mMapFragment;
        this.mMarkerId = mMarkerId;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog2, null);
        mFirestoreServices = FirestoreServices.getInstance();

        mDeleteButton = view.findViewById(R.id.deleteBtn);
        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);

        title.setText(mTitle);
        description.setText(mDescription.isEmpty() ? "You did not add text " +
                "when creating the Marker" : mDescription);
        cancelButton.setOnClickListener(v -> dismiss());
        configureDeleteButton();

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
                    mMapFragment.refreshMapMarkers();
                },
                error -> Toast.makeText(getContext(), error,
                        Toast.LENGTH_SHORT).show()));
    }
}