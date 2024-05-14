package com.timetwist.info;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.NetworkUtils;

import java.util.Objects;

public class CreateMarker extends DialogFragment {
    private final MapFragment mMapFragment;
    private final LatLng mMarkerLatLng;
    private FirestoreServices mFirestoreServices;
    private FirebaseUser mCurrentUser;
    private EditText mPlaceName, mPlaceDescription;
    private Button mSaveButton;
    private TextView mShowMarkerName;
    private String mType;

    public CreateMarker(LatLng markerLatLng, MapFragment mapFragment) {
        mMarkerLatLng = markerLatLng;
        mMapFragment = mapFragment;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_create_marker, null);

        mPlaceName = view.findViewById(R.id.markerName);
        mPlaceDescription = view.findViewById(R.id.markerDescription);
        mSaveButton = view.findViewById(R.id.save);
        mShowMarkerName = view.findViewById(R.id.showMarkerName);
        Button church = view.findViewById(R.id.church);
        Button prehistoricSite = view.findViewById(R.id.prehistoricSite);
        Button tree = view.findViewById(R.id.tree);
        Button closeButton = view.findViewById(R.id.close);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestoreServices = FirestoreServices.getInstance();

        church.setOnClickListener(v -> {
            mType = "church";
            mShowMarkerName.setText("Church");
        });
        prehistoricSite.setOnClickListener(v -> {
            mType = "temple";
            mShowMarkerName.setText("Temple");
        });
        tree.setOnClickListener(v -> {
            mType = "nature";
            mShowMarkerName.setText("Nature");
        });

        closeButton.setOnClickListener(v -> dismiss());
        configureSaveButton();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void saveMarkerToFireStore() {
        String name = mPlaceName.getText().toString().trim();
        String description = mPlaceDescription.getText().toString().trim();
        String type = mType;
        if (mMapFragment.checkIfMarkerExist(name)) {
            Toast.makeText(getContext(), "You have already marker with this name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || mMarkerLatLng == null) {
            Toast.makeText(getContext(), "Marker name is required",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mType == null) {
            Toast.makeText(getContext(), "You must select a type for the marker",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint geoPoint = new GeoPoint(mMarkerLatLng.latitude, mMarkerLatLng.longitude);
        mFirestoreServices.addMarkerDb(mCurrentUser.getUid(), name, description, type, geoPoint,
                success -> {
                    Toast.makeText(getContext(), success,
                            Toast.LENGTH_SHORT).show();
                    mMapFragment.refreshMapMarkers(true, name);
                    dismiss();
                },
                error -> Toast.makeText(getContext(), error,
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void configureSaveButton() {
        mSaveButton.setOnClickListener(v -> {
            if (NetworkUtils.isInternetDisconnected(requireContext())) {
                Toast.makeText(requireContext(), "Wifi Required",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            saveMarkerToFireStore();
        });
    }
}