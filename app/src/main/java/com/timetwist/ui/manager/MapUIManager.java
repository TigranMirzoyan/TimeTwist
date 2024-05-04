package com.timetwist.ui.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.info.CreateMarker;
import com.timetwist.info.PlaceInfoDialog;
import com.timetwist.info.PlaceInfoDialog2;
import com.timetwist.info.WikipediaAPI;
import com.timetwist.utils.NetworkUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapUIManager {
    private static final int COMPASS_ID = 1;
    private static final int LOCATION_COMPASS_ID = 5;
    private static final int LAT_LNG_ZOOM = 15;
    private final ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();
    private final Context mContext;
    private final Fragment mFragment;
    private final View mRootView;
    private final Button mMyLocationButton;
    private final GoogleMap mMap;
    private final FusedLocationProviderClient mFusedLocationClient;
    private final FirestoreServices mFirestoreServices;
    private boolean mIsAtCurrentLocation = false;
    private final ProgressBar mProgressBar;

    public MapUIManager(Context mContext, Fragment mFragment, View mRootView,
                        Button mMyLocationButton, GoogleMap mMap,
                        FusedLocationProviderClient mFusedLocationClient) {
        this.mContext = mContext;
        this.mFragment = mFragment;
        this.mRootView = mRootView;
        this.mMyLocationButton = mMyLocationButton;
        this.mMap = mMap;
        this.mFusedLocationClient = mFusedLocationClient;
        this.mFirestoreServices = new FirestoreServices();

        mProgressBar = mRootView.findViewById(R.id.progressBar);
    }

    public void configureCompassPlace() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        View compass = mRootView.findViewById(COMPASS_ID);
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

    @SuppressLint("MissingPermission")
    public void updateLocation(Runnable ifLocationIsNull) {
        if (!MapHelper.hasLocationPermissions(mContext)) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
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

    public void updateMyLocationButtonDrawable() {
        if (mContext == null) {
            return;
        }
        mMyLocationButton.setBackground(ContextCompat.getDrawable(mContext,
                mIsAtCurrentLocation ? R.drawable.my_location_visible : R.drawable.my_location_not_visible));
    }

    public void setIsAtCurrentLocation(boolean isAtCurrentLocation) {
        this.mIsAtCurrentLocation = isAtCurrentLocation;
    }

    public void configureAutocomplete(FragmentManager fragmentManager) {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                fragmentManager.findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment == null) {
            Log.e("MapUIManager", "AutocompleteSupportFragment not found.");
            return;
        }

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng == null || mMap == null) {
                    return;
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, LAT_LNG_ZOOM));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("MapUIManager", "Autocomplete error: " + status.getStatusMessage());
            }
        });
    }

    public void addMarkersFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestoreServices.getGlobalMarkers((latLng, name, type) -> {
            try {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext, R.drawable.class.getField(type).getInt(null))));
                marker.setTag("firebase");
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }, message -> Log.w("MapUIManager", "Error fetching markers: " + message));

        if (currentUser == null) {
            configureMarkers();
            Toast.makeText(mContext, "No authenticated user found.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestoreServices.getUserCustomMarkers((latLng, name, type) -> {
            try {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext, R.drawable.class.getField(type).getInt(null))));
                marker.setTag("custom");
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }, message -> Log.e("MapUIManager", "Error adding user markers: " + message));

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
            if (!NetworkUtils.isWifiConnected(mContext)) {
                Toast.makeText(mContext, "Please turn on the WiFi", Toast.LENGTH_LONG).show();
                return false;
            }

            String title = marker.getTitle();
            String markerType = (String) marker.getTag();

            dismissExistingDialogIfExists();
            showProgressBar();
            disableFragmentInteraction();

            mCachedThreadPool.execute(() -> {

                if ("firebase".equals(markerType)) {

                    WikipediaAPI.fetchArticle(title, article -> new Handler(Looper.getMainLooper()).post(() -> {
                        if (!NetworkUtils.isWifiConnected(mContext)) {
                            Toast.makeText(mContext, "Please turn on the WiFi", Toast.LENGTH_LONG).show();
                            hideProgressBar();
                            enableFragmentInteraction();
                            return;
                        }
                        PlaceInfoDialog dialogFragment = new PlaceInfoDialog(title, article, markerType);
                        dialogFragment.show(mFragment.getChildFragmentManager(), "PlaceInfoDialog");

                        hideProgressBar();
                        enableFragmentInteraction();
                    }));
                    return;
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!NetworkUtils.isWifiConnected(mContext)) {
                        Toast.makeText(mContext, "Please turn on the WiFi", Toast.LENGTH_LONG).show();
                        hideProgressBar();
                        enableFragmentInteraction();
                        return;
                    }
                    PlaceInfoDialog2 dialogFragment = new PlaceInfoDialog2(title, marker.getSnippet(), (MapFragment) mFragment, markerType);
                    dialogFragment.show(mFragment.getChildFragmentManager(), "PlaceInfoDialog2");

                    hideProgressBar();
                    enableFragmentInteraction();
                });
            });

            return false;
        });
    }

    private void disableFragmentInteraction() {
        new Handler(Looper.getMainLooper()).post(() -> {
            View overlayView = mRootView.findViewById(R.id.overlayView);
            overlayView.setVisibility(View.VISIBLE);
        });
    }

    private void enableFragmentInteraction() {
        new Handler(Looper.getMainLooper()).post(() -> {
            View overlayView = mRootView.findViewById(R.id.overlayView);
            overlayView.setVisibility(View.GONE);
        });
    }

    private void showProgressBar() {
        new Handler(Looper.getMainLooper()).post(() -> mProgressBar.setVisibility(View.VISIBLE));
    }

    private void hideProgressBar() {
        new Handler(Looper.getMainLooper()).post(() -> mProgressBar.setVisibility(View.GONE));
    }

    private void dismissExistingDialogIfExists() {
        FragmentManager fragmentManager = mFragment.getChildFragmentManager();
        Fragment existingDialog = fragmentManager.findFragmentByTag("PlaceInfoDialog");
        Fragment existingDialog2 = fragmentManager.findFragmentByTag("PlaceInfoDialog2");
        if (existingDialog == null || existingDialog2 == null) {
            return;
        }
        fragmentManager.beginTransaction().remove(existingDialog).commit();
    }

    public void moveToCurrentLocation() {
        updateLocation(() -> Toast.makeText(mContext, "Please enable GPS", Toast.LENGTH_LONG).show());
    }

    public void configureMap(Button mAddMarkerButton) {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason != GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                return;
            }
            setIsAtCurrentLocation(false);
            updateMyLocationButtonDrawable();
        });
        mAddMarkerButton.setOnClickListener(v -> mMap.setOnMapClickListener(this::addMarkerOnMapClick));
        addMarkersFromFirebase();
    }

    public void addMarkerOnMapClick(LatLng latLng) {
        CreateMarker createMarkerDialog = new CreateMarker(latLng, (MapFragment) mFragment);
        createMarkerDialog.show(mFragment.getChildFragmentManager(), "firebase");
        mMap.setOnMapClickListener(null);
    }
}