package com.timetwist.bottombar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.timetwist.R;
import com.timetwist.info.CreateMarker;
import com.timetwist.info.PlaceInfoDialog;
import com.timetwist.info.PlaceInfoDialog2;
import com.timetwist.info.WikipediaAPI;

import java.util.Arrays;
import java.util.Objects;


public class MapFragment extends Fragment {
    public static final int COMPASS_ID = 1;
    public static final int LOCATION_COMPASS_ID = 5;
    public static final int LAT_LNG_ZOOM = 15;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mMyLocationButton, mAddMarkerButton;
    private boolean mIsAtCurrentLocation;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMyLocationButton = view.findViewById(R.id.myLocationBtn);
        mAddMarkerButton = view.findViewById(R.id.addMarker);

        configureAutocomplete();
        configureFusedLocationClient();
    }

    private void onMapReady(GoogleMap map) {
        mMap = map;
        initializePlaces();
        tryEnablingMyLocation();
        configureUi();
        configureMyLocationButton();
        configureMap();
        updateLocation(() -> Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show());
    }

    private void configureFusedLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager().findFragmentById(R.id.map))).getMapAsync(this::onMapReady);
    }

    private void configureAutocomplete() {
        AutocompleteSupportFragment mAutocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        assert mAutocompleteFragment != null;
        mAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng != null && mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, LAT_LNG_ZOOM));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });
    }

    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.my_map_Api_key));
        }
    }

    private void tryEnablingMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void configureUi() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        View compass = requireView().findViewById(COMPASS_ID);

        if (compass == null) {
            return;
        }
        View locationCompass = ((View) compass.getParent()).findViewById(LOCATION_COMPASS_ID);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationCompass.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.setMargins(30, 0, 0, 100);
        locationCompass.setLayoutParams(layoutParams);
    }

    private void configureMyLocationButton() {
        mMyLocationButton.setOnClickListener(__ -> moveToCurrentLocation());
    }

    private void configureMap() {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason != GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                return;
            }
            mIsAtCurrentLocation = false;
            updateMyLocationButtonDrawable();
        });
        mAddMarkerButton.setOnClickListener(v -> mMap.setOnMapClickListener(this::addMarkerOnMapClick));
        addMarkersFromFirebase();
    }

    private void moveToCurrentLocation() {
        updateLocation(() -> {
            Log.d("MapFragment", "Current location is null.");
            Toast.makeText(requireContext(), "Pls on the Gps", Toast.LENGTH_LONG).show();
        });
    }

    private void updateLocation(Runnable ifLocationIsNull) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location == null) {
                ifLocationIsNull.run();
                return;
            }
            mIsAtCurrentLocation = true;
            updateMyLocationButtonDrawable();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, LAT_LNG_ZOOM));
        });
    }

    private void updateMyLocationButtonDrawable() {
        if (getContext() == null) {
            return;
        }
        mMyLocationButton.setBackground(ContextCompat.getDrawable(getContext(),
                mIsAtCurrentLocation ? R.drawable.my_location_visible : R.drawable.my_location_not_visible));
    }

    public void addMarkersFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("Locations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    String type = document.getString("type");
                    GeoPoint geoPoint = document.getGeoPoint("coordinates");
                    if (geoPoint != null && name != null && type != null) {
                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        try {
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .icon(getBitmapDescriptorFromVectorDrawable(getContext(), R.drawable.class.getField(type).getInt(null))));
                            marker.setTag("firebase");  // Tagging the marker as from Firebase
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.w("MapFragment", "Document data is incomplete: " + document.getId());
                    }
                }
            } else {
                Log.w("MapFragment", "Error getting documents.", task.getException());
            }
            configureMarkers();
        });

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId).collection("Markers")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String description = document.getString("description");
                                GeoPoint geoPoint = document.getGeoPoint("coordinates");
                                if (geoPoint != null && name != null) {
                                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(name)
                                            .snippet(description));
                                    marker.setTag(document.getId());
                                }
                            }
                        } else {
                            Log.w("MapFragment", "Error getting documents.", task.getException());
                        }
                    });
        } else {
            Toast.makeText(requireActivity(), "No authenticated user found.", Toast.LENGTH_SHORT).show();
        }
        configureMarkers();
    }


    private static BitmapDescriptor getBitmapDescriptorFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void configureMarkers() {
        mMap.setOnMarkerClickListener(marker -> {
            String title = marker.getTitle();
            String markerType = (String) marker.getTag();
            if ("firebase".equals(markerType)) {
                PlaceInfoDialog dialogFragment = new PlaceInfoDialog(title, WikipediaAPI.fetchArticle(marker.getTitle()));
                dialogFragment.show(getChildFragmentManager(), "PlaceInfoDialog");
            } else {
                PlaceInfoDialog2 dialogFragment = new PlaceInfoDialog2(title, marker.getSnippet(), this, (String) marker.getTag());
                dialogFragment.show(getChildFragmentManager(), "PlaceInfoDialog2");
            }
            return false;
        });
    }

    private void addMarkerOnMapClick(LatLng latLng) {
        CreateMarker createMarkerDialog = new CreateMarker(latLng, this);
        createMarkerDialog.show(getChildFragmentManager(), "firebase");
        mMap.setOnMapClickListener(null);
    }

    public GoogleMap getMap() {
        return mMap;
    }

}
