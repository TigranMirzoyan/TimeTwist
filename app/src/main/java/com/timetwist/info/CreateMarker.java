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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;

import java.util.HashMap;
import java.util.Map;

public class CreateMarker extends DialogFragment {
    private EditText placeName, placeDescription;
    private final LatLng markerLatLng;
    private final MapFragment mapFragment;

    public CreateMarker(LatLng latLng, MapFragment mapFragment) {
        markerLatLng = latLng;
        this.mapFragment = mapFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_marker, container, false);

        placeName = view.findViewById(R.id.placeName);
        placeDescription = view.findViewById(R.id.placeDescription);
        Button saveButton = view.findViewById(R.id.save);
        Button closeButton = view.findViewById(R.id.close);

        closeButton.setOnClickListener(v -> dismiss());
        saveButton.setOnClickListener(v -> saveMarkerToFireStore());

        return view;
    }

    private void saveMarkerToFireStore() {
        String name = placeName.getText().toString().trim();
        String description = placeDescription.getText().toString().trim();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && !name.isEmpty() && markerLatLng != null) {
            String uid = currentUser.getUid();
            Map<String, Object> markerData = new HashMap<>();
            markerData.put("name", name);
            markerData.put("description", description);
            GeoPoint geoPoint = new GeoPoint(markerLatLng.latitude, markerLatLng.longitude);
            markerData.put("coordinates", geoPoint);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(uid).collection("Markers").add(markerData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Marker saved!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save marker.",
                            Toast.LENGTH_SHORT).show());

            mapFragment.getMap().clear();
            mapFragment.addMarkersFromFirebase();
        } else {
            Toast.makeText(getContext(), "Marker name is required and user must be logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

