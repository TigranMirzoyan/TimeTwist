package com.timetwist.firebase;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.timetwist.ui.manager.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FirestoreServices {
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    public void createUserInDB(final String username, final String email, final String password, FirebaseUser currentUser) {
        String userId = currentUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        mDb.collection("Users").document(userId).set(user);
    }

    public void updateProfileUI(BiConsumer<String, String> callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();

        mDb.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.d("FirebaseService", "get failed with ", task.getException());
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (!document.exists()) {
                Log.d("FirebaseService", "No such user");
            }

            String username = document.getString("username");
            String email = document.getString("email");
            if (callback != null) {
                callback.accept(username, email);
            }
        });
    }

    public void getGlobalMarkers(TriConsumer<LatLng, String, String> callback, Consumer<String> errorHandler) {
        mDb.collection("Locations").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                errorHandler.accept("Error getting documents: " + Objects.requireNonNull(task.getException()).getMessage());
                return;
            }

            for (QueryDocumentSnapshot document : task.getResult()) {
                GeoPoint geoPoint = document.getGeoPoint("coordinates");
                String name = document.getString("name");
                String type = document.getString("type");

                if (geoPoint == null || name == null || type == null) {
                    errorHandler.accept("Document data is incomplete: " + document.getId());
                    return;
                }

                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                callback.accept(latLng, name, type);
            }
        });
    }

    public void getUserCustomMarkers(TriConsumer<LatLng, String, String> callback, Consumer<String> errorHandler) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorHandler.accept("No authenticated user found.");
            return;
        }

        String userId = currentUser.getUid();
        mDb.collection("Users").document(userId).collection("Markers").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                errorHandler.accept("Error getting user-specific documents: " + Objects.requireNonNull(task.getException()).getMessage());
                return;
            }

            for (QueryDocumentSnapshot document : task.getResult()) {
                GeoPoint geoPoint = document.getGeoPoint("coordinates");
                String name = document.getString("name");
                String type = document.getString("type");

                if (geoPoint == null || name == null || type == null) {
                    errorHandler.accept("Document data is incomplete: " + document.getId());
                    return;
                }

                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                callback.accept(latLng, name, type);
            }
        });
    }

    public void addMarkerDb(String uid, String name, String description, String type,
                            GeoPoint coordinates, Consumer<String> onSuccess, Consumer<String> onFailure) {
        Map<String, Object> markerData = new HashMap<>();
        markerData.put("name", name);
        markerData.put("description", description);
        markerData.put("coordinates", coordinates);
        markerData.put("type", type);
        mDb.collection("Users").document(uid).collection("Markers").add(markerData)
                .addOnSuccessListener(documentReference -> onSuccess.accept("Marker saved!"))
                .addOnFailureListener(e -> onFailure.accept("Failed to save marker."));
    }
}