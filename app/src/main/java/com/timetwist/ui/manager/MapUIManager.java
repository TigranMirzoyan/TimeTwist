package com.timetwist.ui.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.info.CreateMarker;
import com.timetwist.info.PlaceInfoDialog;
import com.timetwist.info.PlaceInfoDialog2;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.WikipediaAPI;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapUIManager {
    private static final int COMPASS_ID = 1;
    private static final int LOCATION_COMPASS_ID = 5;
    private static final int LAT_LNG_ZOOM = 15;
    private final ActivityUtils mActivityUtils;
    private final FirestoreServices mFirestoreServices;
    private final List<Marker> mGlobalMarkers = new LinkedList<>();
    private final List<Marker> mCustomMarkers = new LinkedList<>();
    private final GoogleMap mMap;
    private final FirebaseAuth mAuth;
    private final Context mContext;
    private final Fragment mFragment;
    private final View mRootView;
    private final Button mMyLocationButton;
    private final TextView mChangeMarkers;
    private final FusedLocationProviderClient mFusedLocationClient;
    private final ProgressBar mProgressBar;
    private CompletableFuture<String> mArticle;
    private boolean mIsAtCurrentLocation = false;
    private boolean mIsButtonClicked = false;

    public MapUIManager(Context context, Fragment fragment, View rootView,
                        Button myLocationButton, TextView changeMarkers, GoogleMap map,
                        FusedLocationProviderClient fusedLocationClient) {
        mContext = context;
        mFragment = fragment;
        mRootView = rootView;
        mMyLocationButton = myLocationButton;
        mChangeMarkers = changeMarkers;
        mMap = map;
        mFusedLocationClient = fusedLocationClient;

        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
    }

    public void configureMap(Button addMarkerButton) {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason != GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) return;
            this.mIsAtCurrentLocation = false;
            updateMyLocationButtonDrawable();
        });

        addMarkersFromFirebase();
        configureSnippedBeingInvisible();
        configureAddMarkerButton(addMarkerButton);
        mChangeMarkers.setOnClickListener(v -> configureChangeMarkersButton());
        mMyLocationButton.setOnClickListener(__ -> updateLocation(() -> Toast.makeText(mContext,
                "Please enable GPS", Toast.LENGTH_LONG).show()));
        updateLocation(() -> Toast.makeText(mContext,
                "Location unavailable", Toast.LENGTH_SHORT).show());
    }

    private void configureSnippedBeingInvisible() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mapView = ((FragmentActivity) mContext).findViewById(R.id.map);
                View view = inflater.inflate(R.layout.custom_info_window,
                        (ViewGroup) mapView, false);
                TextView title = view.findViewById(R.id.title);
                title.setText(marker.getTitle());

                return view;
            }
        });
    }

    public void configureAddMarkerButton(Button addMarkerButton) {
        addMarkerButton.setOnClickListener(v -> {
                    if (mAuth.getCurrentUser() == null) {
                        Toast.makeText(mContext, "No authenticated user found.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else if (NetworkUtils.isInternetDisconnected(mContext)) {
                        Toast.makeText(mContext, "Wifi Required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mMap.setOnMapClickListener(this::addMarkerOnMapClick);
                }
        );
    }

    @SuppressLint("SetTextI18n")
    public void configureChangeMarkersButton() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(mContext, "No authenticated user found.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mIsButtonClicked) {
            mChangeMarkers.setText("Global Markers");
            mGlobalMarkers.forEach(marker -> marker.setVisible(false));
            mCustomMarkers.forEach(marker -> marker.setVisible(true));
            mIsButtonClicked = !mIsButtonClicked;
            return;
        }

        mChangeMarkers.setText("My Markers");
        mGlobalMarkers.forEach(marker -> marker.setVisible(true));
        mCustomMarkers.forEach(marker -> marker.setVisible(false));
        mIsButtonClicked = !mIsButtonClicked;
    }

    public void addMarkerOnMapClick(LatLng latLng) {
        if (NetworkUtils.isInternetDisconnected(mContext)) {
            Toast.makeText(mContext, "Wifi Required",
                    Toast.LENGTH_SHORT).show();
            mMap.setOnMapClickListener(null);
            return;
        }
        CreateMarker createMarkerDialog = new CreateMarker(latLng, (MapFragment) mFragment);
        createMarkerDialog.show(mFragment.getChildFragmentManager(), "firebase");
        mMap.setOnMapClickListener(null);
    }

    public void configureCompassPlace() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        View compass = mRootView.findViewById(COMPASS_ID);
        if (compass == null) return;
        View locationCompass = ((View) compass.getParent()).findViewById(LOCATION_COMPASS_ID);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) locationCompass.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.setMargins(30, 0, 0, 200);
        locationCompass.setLayoutParams(layoutParams);
    }

    @SuppressLint("MissingPermission")
    public void updateLocation(Runnable ifLocationIsNull) {
        if (!MapHelper.hasLocationPermissions(mContext)) return;
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
        if (mContext == null) return;
        mMyLocationButton.setBackground(ContextCompat.getDrawable(mContext,
                mIsAtCurrentLocation ? R.drawable.my_location_visible :
                        R.drawable.my_location_not_visible));
    }

    public void configureAutocomplete(FragmentManager fragmentManager) {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                fragmentManager.findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment == null) {
            Log.e("MapUIManager", "AutocompleteSupportFragment not found.");
            return;
        }

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng == null || mMap == null) return;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, LAT_LNG_ZOOM));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("MapUIManager", "Autocomplete error: " + status.getStatusMessage());
            }
        });
    }

    private void addMarkersFromFirebase() {
        showProgressBar();
        disableFragmentInteraction();
        addGlobalMarkers();
        addCustomMarkers();
        hideProgressBar();
        enableFragmentInteraction();
    }

    private void addGlobalMarkers() {
        mFirestoreServices.getGlobalMarkers((latLng, name, type) -> {
            try {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext,
                                R.drawable.class.getField(type).getInt(null))));
                marker.setTag("firebase");
                if (mIsButtonClicked) marker.setVisible(false);
                mGlobalMarkers.add(marker);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }, message -> Log.w("MapUIManager", "Error fetching markers: " + message));
        configureMarkers();
    }

    public void addCustomMarkers() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            configureMarkers();
            Toast.makeText(mContext, "No authenticated user found.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestoreServices.getUserCustomMarkers((latLng, name, description,
                                                 type, documentId) -> {
            try {
                if (mCustomMarkers.stream().anyMatch(marker ->
                        marker.getTitle().equals(name))) return;

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .snippet(description)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext,
                                R.drawable.class.getField(type).getInt(null))));
                if (!mIsButtonClicked) marker.setVisible(false);

                marker.setTag(documentId);
                mCustomMarkers.add(marker);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }, message -> Log.e("MapUIManager", "Error adding user markers: " + message));
        configureMarkers();
    }

    private void configureMarkers() {
        mMap.setOnMarkerClickListener(marker -> {
            if (NetworkUtils.isInternetDisconnected(mContext)) {
                Toast.makeText(mContext,
                        "Please turn on the WiFi", Toast.LENGTH_LONG).show();
                return false;
            }
            String title = marker.getTitle();

            dismissExistingDialogIfExists();
            showProgressBar();
            disableFragmentInteraction();

            if ("firebase".equals(marker.getTag())) {
                mArticle = CompletableFuture.supplyAsync(() -> WikipediaAPI
                        .fetchArticle(title));
                boolean isFavorite = mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT
                        .getFavoriteLocationsList().stream()
                        .anyMatch(name -> name.equals(title));

                mArticle.thenAccept(result -> {
                    if (NetworkUtils.isInternetDisconnected(mContext)) {
                        Toast.makeText(mContext, "Please turn on the WiFi",
                                Toast.LENGTH_LONG).show();
                        hideProgressBar();
                        enableFragmentInteraction();
                        return;
                    }
                    PlaceInfoDialog dialogFragment =
                            new PlaceInfoDialog(title, result, isFavorite, mContext,
                                    new PlaceInfoDialog.OnFavoriteUpdateListener() {
                                        @Override
                                        public void onFavoriteAdded(String title) {
                                            mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT.addFavoritePlace(title);
                                        }

                                        @Override
                                        public void onFavoriteRemoved(String title) {
                                            mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT.removeFavoritePlace(title);
                                        }
                                    });
                    dialogFragment.show(mFragment.getParentFragmentManager(),
                            "PlaceInfoDialog");

                    hideProgressBar();
                    enableFragmentInteraction();
                });
                return false;
            }

            if (NetworkUtils.isInternetDisconnected(mContext)) {
                Toast.makeText(mContext, "Please turn on the WiFi",
                        Toast.LENGTH_LONG).show();
                hideProgressBar();
                enableFragmentInteraction();
                return false;
            }

            LatLng latLng = marker.getPosition();
            PlaceInfoDialog2 dialogFragment = new PlaceInfoDialog2(title,
                    marker.getSnippet(), (String) marker.getTag(), latLng);
            dialogFragment.show(mFragment.getChildFragmentManager(), "PlaceInfoDialog2");

            hideProgressBar();
            enableFragmentInteraction();
            return false;
        });
    }

    public void cancelDialog() {
        if (mArticle == null) {
            hideProgressBar();
            enableFragmentInteraction();
            return;
        }
        mArticle.cancel(true);
        mArticle = null;
    }

    public void cancelMapClickListener() {
        mMap.setOnMapClickListener(null);
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
        new Handler(Looper.getMainLooper()).post(() -> {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
        });
    }

    private void hideProgressBar() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        });
    }


    private void dismissExistingDialogIfExists() {
        FragmentManager fragmentManager = mFragment.getChildFragmentManager();
        Fragment existingDialog = fragmentManager.findFragmentByTag("PlaceInfoDialog");
        Fragment existingDialog2 = fragmentManager.findFragmentByTag("PlaceInfoDialog2");
        if (existingDialog == null || existingDialog2 == null) return;
        fragmentManager.beginTransaction().remove(existingDialog).commit();
    }

    private static BitmapDescriptor getBitmapDescriptorFromVectorDrawable
            (Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void zoomToMarkerByName(String markerName) {
        if (mIsButtonClicked) {
            mChangeMarkers.callOnClick();
        }

        showProgressBar();
        disableFragmentInteraction();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String searchName = markerName.toLowerCase();

            mGlobalMarkers.stream().filter(marker -> marker.getTitle()
                            .toLowerCase().equals(searchName))
                    .findAny().ifPresent(marker -> {
                        LatLng markerPosition = marker.getPosition();
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(markerPosition, LAT_LNG_ZOOM));
                    });
            hideProgressBar();
            enableFragmentInteraction();
        }, 1000);
    }

    public List<Marker> getCustomMarkers() {
        return mCustomMarkers;
    }
}