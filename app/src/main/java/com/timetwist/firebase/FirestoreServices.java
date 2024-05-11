package com.timetwist.firebase;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.timetwist.custom.interfaces.QuintConsumer;
import com.timetwist.custom.interfaces.TriConsumer;
import com.timetwist.events.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FirestoreServices {
    private static FirestoreServices mInstance;
    private final FirebaseFirestore mDb;
    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    private FirestoreServices() {
        mDb = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreServices getInstance() {
        if (mInstance == null) {
            mInstance = new FirestoreServices();
        }
        return mInstance;
    }

    public void createUserInDB(final String username, final String email,
                               final String password, FirebaseUser currentUser) {
        String userId = currentUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        mDb.collection("Users").document(userId).set(user);
    }

    public void updateProfileUI(BiConsumer<String, String> callback) {
        if (mCurrentUser == null) {
            return;
        }
        String userId = mCurrentUser.getUid();

        mDb.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.d("FirebaseService", "get failed with ",
                        task.getException());
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

    public void getGlobalMarkers(TriConsumer<LatLng, String,
            String> callback, Consumer<String> errorHandler) {
        mDb.collection("Locations").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        errorHandler.accept("Error getting documents: " +
                                Objects.requireNonNull(task.getException()).getMessage());
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

    public void getUserCustomMarkers(QuintConsumer<LatLng, String, String, String, String>
                                             callback, Consumer<String> errorHandler) {
        if (mCurrentUser == null) {
            errorHandler.accept("No authenticated user found.");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .collection("Markers")
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        errorHandler.accept("Error getting user-specific documents: " +
                                Objects.requireNonNull(task.getException()).getMessage());
                        return;
                    }

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        GeoPoint geoPoint = document.getGeoPoint("coordinates");
                        String name = document.getString("name");
                        String description = document.getString("description");
                        String type = document.getString("type");

                        if (geoPoint == null || name == null || type == null) {
                            errorHandler.accept("Document data is incomplete: "
                                    + document.getId());
                            return;
                        }

                        LatLng latLng = new LatLng(geoPoint.getLatitude(),
                                geoPoint.getLongitude());
                        callback.accept(latLng, name, description, type, document.getId());
                    }
                });
    }

    public void getGlobalMarkerNames(Consumer<List<String>> onSuccess, Consumer<String> onFailure) {
        mDb.collection("Locations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> names = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            if (name != null) {
                                names.add(name);
                            }
                        }
                        onSuccess.accept(names);
                    } else {
                        onFailure.accept("Failed to fetch marker names: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }


    public void getFavoritePlaces(Consumer<List<String>> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("No authenticated user found.");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .collection("FavoriteMarkers").document("GlobalMarkers")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        onFailure.accept("No favorites document found");
                        return;
                    }
                    Map<String, Object> fields = documentSnapshot.getData();
                    if (fields == null) {
                        onSuccess.accept(new ArrayList<>());
                        return;
                    }
                    List<String> favorites = new ArrayList<>();
                    fields.forEach((key, value) -> {
                        if (Boolean.TRUE.equals(value)) {
                            favorites.add(key);
                        }
                    });

                    favorites.sort(String.CASE_INSENSITIVE_ORDER);
                    onSuccess.accept(favorites);
                })
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to fetch favorites: " + e.getMessage()));
    }

    public void makeFavoriteLocation(String title, Consumer<String> onSuccess,
                                     Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("No authenticated user found.");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .collection("FavoriteMarkers").document("GlobalMarkers")
                .set(Collections.singletonMap(title, true), SetOptions.merge())
                .addOnSuccessListener(command -> onSuccess
                        .accept("Marker set as favorite!"))
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to set marker as favorite: " + e.getMessage()));
    }

    public void findFavoriteMarkerAndDelete(String title, Consumer<String> onSuccess,
                                            Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("No authenticated user found.");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .collection("FavoriteMarkers").document("GlobalMarkers")
                .update(title, false)
                .addOnSuccessListener(command -> onSuccess
                        .accept("Marker was successfully deleted from Favorites!"))
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to set marker as not favorite: " + e.getMessage()));
    }

    public void addMarkerDb(String uid, String name, String description,
                            String type, GeoPoint coordinates,
                            Consumer<String> onSuccess, Consumer<String> onFailure) {
        Map<String, Object> markerData = new HashMap<>();
        markerData.put("name", name);
        markerData.put("description", description);
        markerData.put("coordinates", coordinates);
        markerData.put("type", type);
        mDb.collection("Users").document(uid)
                .collection("Markers").add(markerData)
                .addOnSuccessListener(command -> onSuccess
                        .accept("Marker saved!"))
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to save marker."));
    }

    public void deleteCustomMarker(String markerId, Consumer<String> onSuccess,
                                   Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("Pls turn on wifi");
            return;
        }

        FirebaseFirestore.getInstance().collection("Users")
                .document(mCurrentUser.getUid())
                .collection("Markers").document(markerId).delete()
                .addOnSuccessListener(command -> onSuccess
                        .accept("Marker was successfully deleted!"))
                .addOnFailureListener(e -> onFailure
                        .accept("Error deleting marker"));
    }

    public void makeEvent(String name, String username, String description, Calendar calendar,
                          int number, Consumer<String> onSuccess,
                          Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("User is not authenticated");
            return;
        }

        com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(calendar.getTime());
        Map<String, Object> event = new HashMap<>();
        event.put("name", name);
        event.put("username", username);
        event.put("description", description);
        event.put("dateTime", timestamp);
        event.put("AEmail", mCurrentUser.getEmail());
        event.put("numberOfPeople", number);

        mDb.collection("Events")
                .get()
                .addOnSuccessListener(command -> mDb.collection("Events")
                        .add(event)
                        .addOnSuccessListener(documentReference ->
                                onSuccess.accept("Event created successfully!"))
                        .addOnFailureListener(e ->
                                onFailure.accept("Failed to create event")))
                .addOnFailureListener(e ->
                        onFailure.accept("Failed to find user by email"));

    }

    public void getRandomEvents(BiConsumer<List<Event>, List<Event>> callback, Consumer<String> errorHandler) {
        mDb.collection("Events").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        errorHandler.accept("Error getting Events documents: " +
                                Objects.requireNonNull(task.getException()).getMessage());
                        return;
                    }
                    List<Event> eventList = new ArrayList<>();
                    List<Event> randomEventList = new ArrayList<>();

                    task.getResult().forEach(document -> {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                    });
                    int eventListSize = eventList.size();

                    while (randomEventList.size() < Math.min(eventListSize, 10)) {
                        Event randomEvent = eventList.get(new Random().nextInt(eventListSize));
                        if (!randomEventList.contains(randomEvent)) {
                            randomEventList.add(randomEvent);
                        }
                    }
                    callback.accept(eventList, randomEventList);
                });
    }
}