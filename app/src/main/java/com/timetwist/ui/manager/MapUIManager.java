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
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.timetwist.R;
import com.timetwist.databinding.FragmentMapBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.info.CreateGlobalMarkerFragment;
import com.timetwist.info.CreateMarker;
import com.timetwist.info.CustomPlaceInfoDialog;
import com.timetwist.info.GlobalPlaceInfoDialog;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;
import com.timetwist.utils.WikipediaAPI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapUIManager {
    private static final int COMPASS_ID = 1;
    private static final int LOCATION_COMPASS_ID = 5;
    private static final int LAT_LNG_ZOOM = 15;
    private final FragmentMapBinding mBinding;
    private final ActivityUtils mActivityUtils;
    private final FirestoreServices mFirestoreServices;
    private final List<Marker> mGlobalMarkers = new LinkedList<>();
    private final List<Marker> mCustomMarkers = new LinkedList<>();
    private final List<String> mGlobalMarkerNames = new ArrayList<>();

    private final GoogleMap mMap;
    private final FirebaseAuth mAuth;
    private final Context mContext;
    private final Fragment mFragment;
    private final FusedLocationProviderClient mFusedLocationClient;
    private boolean mIsAtCurrentLocation = false;
    private boolean mChangeMarkers = false;
    private boolean mIsAdmin = false;
    private CompletableFuture<String> mArticle;

    public MapUIManager(Context context, Fragment fragment, FragmentMapBinding binding,
                        GoogleMap map, FusedLocationProviderClient fusedLocationClient) {
        mContext = context;
        mFragment = fragment;
        mBinding = binding;
        mMap = map;
        mFusedLocationClient = fusedLocationClient;

        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mBinding.progressBar.setVisibility(View.GONE);
    }

    public void configureMap() {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason != GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) return;
            this.mIsAtCurrentLocation = false;
            updateMyLocationButtonDrawable();
        });

        addMarkersFromFirebase();
        configureSnippedBeingInvisible();
        configureAdmin();
        configureAutoCompleteTextView();

        mBinding.addMarker.setOnClickListener(v -> configureAddMarkerButton());
        mBinding.myLocation.setOnClickListener(__ -> updateLocation(() ->
                ToastUtils.show(mContext, "Please enable GPS")));
        updateLocation(() -> ToastUtils.show(mContext, "Location unavailable"));
    }

    private void configureAdmin() {
        mActivityUtils.ifUserAdmin(mContext,
                () -> {
                    mBinding.changeMarkers.setVisibility(View.GONE);
                    mIsAdmin = true;
                },
                () -> {
                    mBinding.changeMarkers.setVisibility(View.VISIBLE);
                    mBinding.changeMarkers.setOnClickListener(v ->
                            configureChangeMarkersButton());
                    mIsAdmin = false;
                });
    }

    private void configureSnippedBeingInvisible() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mapView = ((FragmentActivity) mContext).findViewById(R.id.map);
                View view = inflater.inflate(R.layout.custom_info_window,
                        (ViewGroup) mapView, false);
                TextView title = view.findViewById(R.id.title);
                title.setText(marker.getTitle());

                return view;
            }
        });
    }

    private void configureAddMarkerButton() {
        if (mAuth.getCurrentUser() == null) {
            ToastUtils.show(mContext, "No authenticated user found.");
            return;
        }
        if (NetworkUtils.isInternetDisconnected(mContext)) {
            ToastUtils.show(mContext, "Internet required");
            return;
        }
        mMap.setOnMapClickListener(this::addMarkerOnMapClick);
    }

    @SuppressLint("SetTextI18n")
    private void configureChangeMarkersButton() {
        if (mAuth.getCurrentUser() == null) {
            ToastUtils.show(mContext, "No authenticated user found.");
            return;
        }

        if (!mChangeMarkers) {
            mBinding.changeMarkers.setText("Global Markers");
            mGlobalMarkers.forEach(marker -> marker.setVisible(false));
            mCustomMarkers.forEach(marker -> marker.setVisible(true));
            mChangeMarkers = !mChangeMarkers;
            return;
        }

        mBinding.changeMarkers.setText("My Markers");
        mGlobalMarkers.forEach(marker -> marker.setVisible(true));
        mCustomMarkers.forEach(marker -> marker.setVisible(false));
        mChangeMarkers = !mChangeMarkers;
    }

    private void addMarkerOnMapClick(LatLng latLng) {
        if (NetworkUtils.isInternetDisconnected(mContext)) {
            ToastUtils.show(mContext, "Internet required");
            mMap.setOnMapClickListener(null);
            return;
        }
        if (mIsAdmin) {
            CreateGlobalMarkerFragment createMarkerDialog
                    = new CreateGlobalMarkerFragment(latLng);
            createMarkerDialog.show(mFragment.getChildFragmentManager(), "firebase");
            mMap.setOnMapClickListener(null);
            return;
        }

        CreateMarker createMarkerDialog = new CreateMarker(latLng);
        createMarkerDialog.show(mFragment.getChildFragmentManager(), "firebase");
        mMap.setOnMapClickListener(null);
    }

    public void configureCompassPlace() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        View compass = mBinding.getRoot().findViewById(COMPASS_ID);
        if (compass == null) return;
        View locationCompass = ((View) compass.getParent()).findViewById(LOCATION_COMPASS_ID);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationCompass.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.setMargins(30, 0, 0, 200);
        locationCompass.setLayoutParams(layoutParams);
    }

    public void configureAutoCompleteTextView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_dropdown_item_1line, mGlobalMarkerNames);
        mBinding.searchView.setAdapter(adapter);
        mBinding.searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMarkerName = (String) parent.getItemAtPosition(position);
            zoomToMarkerByName(selectedMarkerName);
        });
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

    private void updateMyLocationButtonDrawable() {
        if (mContext == null) return;
        mBinding.myLocation.setBackground(ContextCompat.getDrawable(mContext,
                mIsAtCurrentLocation ? R.drawable.my_location_visible :
                        R.drawable.my_location_not_visible));
    }


    private void addMarkersFromFirebase() {
        showProgressBar();
        disableFragmentInteraction();
        addGlobalMarkers();
        addCustomMarkers();
        hideProgressBar();
        enableFragmentInteraction();
    }

    public void addGlobalMarkers() {
        mFirestoreServices.getGlobalMarkers((latLng, name, type) -> {
            try {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext,
                                R.drawable.class.getField(type).getInt(null))));
                marker.setTag("firebase");
                if (mChangeMarkers) marker.setVisible(false);
                mGlobalMarkers.add(marker);
                mGlobalMarkerNames.add(name);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }, message -> Log.w("MapUIManager", "Error fetching markers: " + message));
        configureMarkers();
    }

    public void addCustomMarkers() {
        if (mAuth.getCurrentUser() == null) {
            configureMarkers();
            ToastUtils.show(mContext, "No authenticated user found.");
            return;
        }

        mFirestoreServices.getUserCustomMarkers((latLng, name, description, type, documentId) -> {
            try {
                if (mCustomMarkers.stream().anyMatch(marker -> marker.getTitle().equals(name)))
                    return;

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .snippet(description)
                        .icon(getBitmapDescriptorFromVectorDrawable(mContext,
                                R.drawable.class.getField(type).getInt(null))));
                if (!mChangeMarkers) marker.setVisible(false);

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
            String title = marker.getTitle();
            showProgressBar();
            disableFragmentInteraction();

            if ("firebase".equals(marker.getTag())) {
                if (NetworkUtils.isInternetDisconnected(mContext)) {
                    ToastUtils.show(mContext, "Internet required");
                    hideProgressBar();
                    enableFragmentInteraction();
                    return false;
                }
                mArticle = CompletableFuture.supplyAsync(() -> WikipediaAPI.fetchArticle(title));
                boolean isFavorite = mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT
                        .getFavoriteLocationsList().stream().anyMatch(name -> name.equals(title));
                mArticle.thenAccept(result ->
                        configureGlobalMarkerDialog(title, result, isFavorite));
                return false;
            }

            configureCustomMarkerDialog(marker, title);
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

    private void disableFragmentInteraction() {
        new Handler(Looper.getMainLooper()).post(() -> mBinding.overlayView
                .setVisibility(View.VISIBLE));
    }

    private void enableFragmentInteraction() {
        new Handler(Looper.getMainLooper()).post(() -> mBinding.overlayView
                .setVisibility(View.GONE));
    }

    private void showProgressBar() {
        new Handler(Looper.getMainLooper()).post(() -> mBinding.progressBar
                .setVisibility(View.VISIBLE));
    }

    private void hideProgressBar() {
        new Handler(Looper.getMainLooper()).post(() -> mBinding.progressBar
                .setVisibility(View.GONE));
    }

    private void configureGlobalMarkerDialog(String title, String result, boolean isFavorite) {
        GlobalPlaceInfoDialog dialogFragment = new GlobalPlaceInfoDialog(title, result,
                isFavorite, new GlobalPlaceInfoDialog.OnFavoriteUpdateListener() {
            @Override
            public void onFavoriteAdded(String title) {
                mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT
                        .addFavoritePlace(title);
            }

            @Override
            public void onFavoriteRemoved(String title) {
                mActivityUtils.FAVORITE_LOCATIONS_FRAGMENT
                        .removeFavoritePlace(title);
            }
        });
        dialogFragment.show(mFragment.getParentFragmentManager(), "GlobalPlaceInfoDialog");
        hideProgressBar();
        enableFragmentInteraction();
    }

    private void configureCustomMarkerDialog(Marker marker, String title) {
        LatLng latLng = marker.getPosition();
        CustomPlaceInfoDialog dialogFragment = new CustomPlaceInfoDialog(title,
                marker.getSnippet(), (String) marker.getTag(), latLng);
        dialogFragment.show(mFragment.getParentFragmentManager(), "CustomPlaceInfoDialog");

        hideProgressBar();
        enableFragmentInteraction();
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

    public void zoomToMarkerByName(String markerName) {
        if (mChangeMarkers) mBinding.changeMarkers.callOnClick();

        showProgressBar();
        disableFragmentInteraction();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String searchName = markerName.toLowerCase();

            mGlobalMarkers.stream()
                    .filter(marker -> marker.getTitle().toLowerCase()
                            .equals(searchName))
                    .findAny().ifPresent(marker -> {
                        LatLng markerPosition = marker.getPosition();
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(markerPosition, LAT_LNG_ZOOM));
                    });
            hideProgressBar();
            enableFragmentInteraction();
        }, 500);
    }

    public List<Marker> getCustomMarkers() {
        return mCustomMarkers;
    }
    public List<Marker> getGlobalMarkers() {
        return mGlobalMarkers;
    }
}