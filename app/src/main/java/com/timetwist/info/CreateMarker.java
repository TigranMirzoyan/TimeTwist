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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateMarker extends DialogFragment {
    private EditText placeName, placeDescription;
    private final LatLng markerLatLng;
    private String name, description;
    private final MapFragment mapFragment;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final Map<String, Object> markerData = new HashMap<>();
    private GeoPoint geoPoint;

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


        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return view;
    }

    private void saveMarkerToFireStore() {
        name = placeName.getText().toString().trim();
        description = placeDescription.getText().toString().trim();

        if (currentUser != null && !name.isEmpty() && markerLatLng != null) {
            geoPoint = new GeoPoint(markerLatLng.latitude, markerLatLng.longitude);

            checkAdmin();
        } else {
            Toast.makeText(getContext(), "Marker name is required and user must be logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void checkAdmin() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert currentUser != null;
        db.collection("Users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getBoolean("admin") != null && Boolean.TRUE.equals(document.getBoolean("admin"))) {
                            saveMarkerToLocations();
                        } else {
                            addMarkerDb();
                        }
                        mapFragment.getMap().clear();
                        mapFragment.addMarkersFromFirebase();
                    } else {
                        Toast.makeText(getContext(), "Failed to check admin status.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveMarkerToLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        name = placeName.getText().toString().trim();

        markerData.put("name", name);
        markerData.put("coordinates", geoPoint);
        markerData.put("type", "m_church");
        db.collection("Locations").document(name)
                .set(markerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Marker saved to Locations with name: " + name, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save marker to Locations.", Toast.LENGTH_SHORT).show());
    }

    private void addMarkerDb() {
        assert currentUser != null;
        String uid = currentUser.getUid();
        markerData.put("name", name);
        markerData.put("description", description);
        markerData.put("coordinates", geoPoint);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(uid).collection("Markers").add(markerData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Marker saved!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save marker.",
                        Toast.LENGTH_SHORT).show());
    }
}

