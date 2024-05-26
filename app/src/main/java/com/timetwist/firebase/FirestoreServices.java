package com.timetwist.firebase;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.timetwist.events.Event;
import com.timetwist.events.EventStatus;
import com.timetwist.interfaces.QuintConsumer;
import com.timetwist.interfaces.TriConsumer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FirestoreServices {
    private static FirestoreServices mInstance;
    private final FirebaseFirestore mDb;
    private FirebaseUser mCurrentUser;

    private FirestoreServices() {
        mDb = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static synchronized FirestoreServices getInstance() {
        if (mInstance == null) mInstance = new FirestoreServices();
        return mInstance;
    }

    private void refreshCurrentUser() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void createUserInDB(final String username, final String email,
                               final String password) {
        refreshCurrentUser();
        if (mCurrentUser == null) {
            Log.e("FirestoreServices", "Current user is null during createUserInDB");
            return;
        }
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        mDb.collection("Users").document(mCurrentUser.getUid()).set(user);
    }

    public void checkIfUserIsAdmin(Consumer<Boolean> callback) {
        refreshCurrentUser();
        if (mCurrentUser == null) {
            callback.accept(false);
            return;
        }
        String userId = mCurrentUser.getUid();
        mDb.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                callback.accept(false);
                return;
            }
            Boolean isAdmin = task.getResult().getBoolean("admin");
            callback.accept(isAdmin != null && isAdmin);
        });
    }


    public void updateProfileUI(BiConsumer<String, String> callback) {
        refreshCurrentUser();
        if (mCurrentUser == null) return;
        String userId = mCurrentUser.getUid();

        mDb.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.d("FirebaseService", "get failed with ",
                        task.getException());
                return;
            }

            DocumentSnapshot document = task.getResult();
            String username = document.getString("username");
            String email = document.getString("email");
            if (callback != null) callback.accept(username, email);
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

                    task.getResult().forEach(document -> {
                        GeoPoint geoPoint = document.getGeoPoint("coordinates");
                        String name = document.getString("name");
                        String type = document.getString("type");

                        if (geoPoint == null || name == null || type == null) {
                            errorHandler.accept("Document data is incomplete: " + document.getId());
                            return;
                        }

                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        callback.accept(latLng, name, type);
                    });
                });
    }

    public void getUserCustomMarkers(QuintConsumer<LatLng, String, String, String, String>
                                             callback, Consumer<String> errorHandler) {
        refreshCurrentUser();
        if (mCurrentUser == null) return;
        mDb.collection("Users").document(mCurrentUser.getUid())
                .collection("Markers")
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        errorHandler.accept("Error getting user-specific documents: " +
                                Objects.requireNonNull(task.getException()).getMessage());
                        return;
                    }

                    task.getResult().forEach(document -> {
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
                    });
                });
    }

    public void getGlobalMarkerNames(Consumer<List<String>> onSuccess, Consumer<String> onFailure) {
        mDb.collection("Locations").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                onFailure.accept("Failed to fetch marker names: "
                        + Objects.requireNonNull(task.getException()).getMessage());
                return;
            }
            List<String> names = new ArrayList<>();

            task.getResult().forEach(document -> {
                String name = document.getString("name");
                if (name != null) {
                    names.add(name);
                }
            });
            onSuccess.accept(names);
        });
    }


    public void getFavoritePlaces(Consumer<List<String>> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) return;
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
                        if (Boolean.TRUE.equals(value)) favorites.add(key);
                    });

                    favorites.sort(String.CASE_INSENSITIVE_ORDER);
                    onSuccess.accept(favorites);
                })
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to fetch favorites: " + e.getMessage()));
    }

    public void makeFavoriteLocation(String title, Consumer<String> onSuccess,
                                     Consumer<String> onFailure) {
        if (mCurrentUser == null) return;
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
        if (mCurrentUser == null) return;
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
        if (mCurrentUser == null) return;
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

    public void addGlobalMarkerDb(String name, String type, GeoPoint coordinates,
                                  Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("Current user is null, cannot add marker.");
            return;
        }

        Map<String, Object> markerData = new HashMap<>();
        markerData.put("name", name);
        markerData.put("coordinates", coordinates);
        markerData.put("type", type);

        mDb.collection("Locations").document(name).set(markerData)
                .addOnSuccessListener(aVoid -> onSuccess
                        .accept("Marker saved with name: " + name))
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to save marker: " + e.getMessage()));
    }

    public void deleteCustomMarker(String markerId, Consumer<String> onSuccess,
                                   Consumer<String> onFailure) {

        if (mCurrentUser == null) return;
        FirebaseFirestore.getInstance().collection("Users")
                .document(mCurrentUser.getUid())
                .collection("Markers").document(markerId).delete()
                .addOnSuccessListener(command -> onSuccess
                        .accept("Marker was successfully deleted!"))
                .addOnFailureListener(e -> onFailure
                        .accept("Error deleting marker"));
    }

    public void updateCustomMarker(String markerId, String newTitle, String newDescription,
                                   Consumer<String> onSuccess, Consumer<String> errorHandler) {
        if (mCurrentUser == null) return;
        mDb.collection("Users")
                .document(mCurrentUser.getUid())
                .collection("Markers")
                .document(markerId)
                .update("name", newTitle, "description", newDescription)
                .addOnSuccessListener(task -> onSuccess
                        .accept("Marker updated successfully"))
                .addOnFailureListener(e -> errorHandler
                        .accept("Error changing marker data"));
    }


    public void makeEvent(String eventName, String username, String description,
                          Calendar calendar, String email, int maxPeople,
                          Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("User is not authenticated");
            return;
        }

        com.google.firebase.Timestamp timestamp
                = new com.google.firebase.Timestamp(calendar.getTime());
        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("username", username);
        event.put("description", description);
        event.put("dateTime", timestamp);
        event.put("maxPeople", maxPeople);
        event.put("joinedPeople", 1);
        event.put("email", email);
        event.put("userId", mCurrentUser.getUid());
        event.put("status", EventStatus.NONE.name());

        mDb.collection("Events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    mDb.collection("Users")
                            .document(mCurrentUser.getUid())
                            .update("events", FieldValue.arrayUnion(documentReference.getId()));
                    onSuccess.accept("Event created successfully!");
                })
                .addOnFailureListener(e -> onFailure.accept("Failed to create event: "
                        + e.getMessage()));
    }


    public void getNotVerifiedEvents(BiConsumer<List<Event>, List<Event>> callback,
                                     Consumer<String> errorHandler) {
        mDb.collection("Events").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        errorHandler.accept("Error getting Events documents: " +
                                Objects.requireNonNull(task.getException()).getMessage());
                        return;
                    }
                    List<Event> eventList = new ArrayList<>();
                    List<Event> randomEventList;

                    task.getResult().forEach(document -> {
                        EventStatus status = EventStatus.valueOf(document.getString("status"));
                        if (status == EventStatus.NONE) {
                            Event event = document.toObject(Event.class);
                            eventList.add(event);
                        }
                    });

                    Collections.shuffle(eventList);
                    int randomListSize = Math.min(eventList.size(), 10);
                    randomEventList = eventList.subList(0, randomListSize);

                    callback.accept(eventList, new ArrayList<>(randomEventList));
                });
    }

    public void getEvents(BiConsumer<List<Event>, List<Event>> callback,
                          Consumer<String> errorHandler) {
        mDb.collection("Users").document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> eventIds = (List<String>) documentSnapshot.get("joinedEvents");
                    mDb.collection("Events").get()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    errorHandler.accept("Error getting Events documents: " +
                                            Objects.requireNonNull(task.getException()).getMessage());
                                    return;
                                }
                                List<Event> eventList = new ArrayList<>();
                                List<Event> randomEventList;

                                task.getResult().forEach(document -> {
                                    EventStatus status = EventStatus
                                            .valueOf(document.getString("status"));
                                    if (status != EventStatus.ACCEPTED || eventIds == null
                                            || eventIds.contains(document.getId()))
                                        return;

                                    Event event = document.toObject(Event.class);
                                    if (event.getJoinedPeople() >= event.getMaxPeople() ||
                                            Objects.requireNonNull(document.getString("userId"))
                                                    .equals(mCurrentUser.getUid()))
                                        return;
                                    eventList.add(event);
                                });

                                Collections.shuffle(eventList);
                                int randomListSize = Math.min(eventList.size(), 10);
                                randomEventList = eventList.subList(0, randomListSize);

                                callback.accept(eventList, new ArrayList<>(randomEventList));
                            });
                });
    }

    public void getMyEvents(Consumer<List<Event>> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("User is not authenticated");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> eventIds = (List<String>) documentSnapshot.get("events");
                    if (eventIds == null || eventIds.isEmpty()) {
                        onSuccess.accept(new ArrayList<>());
                        return;
                    }

                    List<Event> myEvents = new ArrayList<>();
                    for (String eventId : eventIds) {
                        mDb.collection("Events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventSnapshot -> {
                                    Event event = eventSnapshot.toObject(Event.class);
                                    if (event != null) {
                                        myEvents.add(event);
                                    }
                                    if (myEvents.size() == eventIds.size()) {
                                        onSuccess.accept(myEvents);
                                    }
                                })
                                .addOnFailureListener(e -> onFailure
                                        .accept("Failed to fetch event: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to fetch user events: " + e.getMessage()));
    }

    public void getJoinedEvents(Consumer<List<Event>> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) {
            onFailure.accept("User is not authenticated");
            return;
        }

        mDb.collection("Users").document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> eventIds = (List<String>) documentSnapshot.get("joinedEvents");
                    if (eventIds == null || eventIds.isEmpty()) {
                        onSuccess.accept(new ArrayList<>());
                        return;
                    }

                    List<Event> myEvents = new ArrayList<>();
                    for (String eventId : eventIds) {
                        mDb.collection("Events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventSnapshot -> {
                                    Event event = eventSnapshot.toObject(Event.class);
                                    if (event != null) {
                                        myEvents.add(event);
                                    }
                                    if (myEvents.size() == eventIds.size()) {
                                        onSuccess.accept(myEvents);
                                    }
                                })
                                .addOnFailureListener(e -> onFailure
                                        .accept("Failed to fetch event: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> onFailure
                        .accept("Failed to fetch user events: " + e.getMessage()));
    }

    public void deleteEvent(String eventId, Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) return;
        mDb.collection("Events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        mDb.collection("Users")
                                .document(mCurrentUser.getUid())
                                .update("events", FieldValue.arrayRemove(eventId))
                                .addOnSuccessListener(aVoid1 -> onSuccess
                                        .accept("Event deleted successfully"))
                                .addOnFailureListener(e -> onFailure
                                        .accept("Event deleted, but failed to" +
                                                " remove event ID from user: " + e.getMessage())))
                .addOnFailureListener(e -> onFailure
                        .accept("Error deleting event: " + e.getMessage()));
    }

    public void joinEvent(String eventId, Consumer<String> onSuccess) {
        if (mCurrentUser == null) return;
        mDb.collection("Users")
                .document(mCurrentUser.getUid())
                .update("joinedEvents", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener(aVoid -> mDb.collection("Events")
                        .document(eventId)
                        .update("joinedPeople", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid1 -> onSuccess.accept("Event joined successfully!")));
    }

    public void leaveEvent(String eventId, Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (mCurrentUser == null) return;
        mDb.collection("Users")
                .document(mCurrentUser.getUid())
                .update("joinedEvents", FieldValue.arrayRemove(eventId))
                .addOnSuccessListener(aVoid -> mDb.collection("Events")
                        .document(eventId)
                        .update("joinedPeople", FieldValue.increment(-1))
                        .addOnSuccessListener(aVoid1 -> onSuccess.accept("Event left successfully"))
                        .addOnFailureListener(e -> onFailure.accept("Event left," +
                                " but failed to update joined people count: " + e.getMessage())))
                .addOnFailureListener(e -> onFailure.accept("Event left," +
                        " but failed to remove event ID from user: " + e.getMessage()));
    }

    public void acceptEvent(String eventId, Consumer<String> onSuccess) {
        if (mCurrentUser == null || eventId == null) return;
        mDb.collection("Events")
                .document(eventId)
                .update("status", EventStatus.ACCEPTED.name())
                .addOnSuccessListener(aVoid -> onSuccess.accept("Event accepted successfully"));
    }

    public void rejectEvent(String eventId, Consumer<String> onSuccess) {
        if (mCurrentUser == null || eventId == null) return;
        mDb.collection("Events")
                .document(eventId)
                .update("status", EventStatus.REJECTED.name())
                .addOnSuccessListener(aVoid -> onSuccess.accept("Event rejected successfully"));
    }
}