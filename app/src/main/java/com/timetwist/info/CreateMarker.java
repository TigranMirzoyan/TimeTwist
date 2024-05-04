package com.timetwist.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.firebase.FirestoreServices;

import java.util.Objects;

public class CreateMarker extends DialogFragment {
    private final FirestoreServices mFirestoreServices;
    private final MapFragment mMapFragment;
    private final FirebaseUser mCurrentUser;
    private final LatLng mMarkerLatLng;
    private EditText mPlaceName, mPlaceDescription;
    private Button saveButton;
    private String mType;

    public CreateMarker(LatLng mMarkerLatLng, MapFragment mMapFragment) {
        this.mMarkerLatLng = mMarkerLatLng;
        this.mMapFragment = mMapFragment;
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestoreServices = new FirestoreServices();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_marker, container, false);

        mPlaceName = view.findViewById(R.id.markerName);
        mPlaceDescription = view.findViewById(R.id.markerDescription);
        Button mChurch = view.findViewById(R.id.church);
        Button mPrehistoricSite = view.findViewById(R.id.prehistoricSite);
        saveButton = view.findViewById(R.id.save);
        Button closeButton = view.findViewById(R.id.close);

        mChurch.setOnClickListener(v -> mType = "church");
        mPrehistoricSite.setOnClickListener(v -> mType = "temple");
        closeButton.setOnClickListener(v -> dismiss());
        configureSaveButton();

        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return view;
    }

    private void saveMarkerToFireStore() {
        String name = mPlaceName.getText().toString().trim();
        String description = mPlaceDescription.getText().toString().trim();
        String type = mType;

        if (mCurrentUser == null || name.isEmpty() || mMarkerLatLng == null) {
            Toast.makeText(getContext(), "Marker name is required and user must be logged in.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        GeoPoint geoPoint = new GeoPoint(mMarkerLatLng.latitude, mMarkerLatLng.longitude);
        mFirestoreServices.addMarkerDb(mCurrentUser.getUid(), name, description, type, geoPoint,
                success -> {
                    Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
                    mMapFragment.refreshMapMarkers();
                    dismiss();
                },
                error -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
        );
    }

    private void configureSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (mType != null && !mType.isEmpty()) {
                saveMarkerToFireStore();
                return;
            }
            Toast.makeText(getContext(), "You must select a type for the marker.", Toast.LENGTH_SHORT).show();
        });
    }
}