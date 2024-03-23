package com.timetwist.bottombar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.timetwist.R;

import java.util.Arrays;


public class MapFragment extends Fragment {
    public static final int COMPASS_ID = 1;
    public static final int LOCATION_COMPASS_ID = 5;
    public static final int LAT_LNG_ZOOM = 15;

    private GoogleMap mMap;
    private AutocompleteSupportFragment mAutocompleteFragment;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mMyLocationButton;
    private boolean mIsAtCurrentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMyLocationButton = view.findViewById(R.id.myLocationBtn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureAutocomplete();
        configureFusedLocationClient();
    }

    private void configureFusedLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this::onMapReady);
    }

    private void configureAutocomplete() {
        mAutocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
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

    private void onMapReady(GoogleMap map) {
        mMap = map;
        initializePlaces();
        tryEnablingMyLocation();
        configureUi();
        configureMyLocationButton();
        configureMap();
        updateLocation(() -> Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show());
    }

    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.my_map_Api_key));
        }
    }

    private void tryEnablingMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void configureUi() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        View compass = getView().findViewById(COMPASS_ID);
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
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mIsAtCurrentLocation = false;
                updateMyLocationButtonDrawable();
            }
        });
    }

    private void moveToCurrentLocation() {
        updateLocation(() -> {
            Log.d("MapFragment", "Current location is null.");
            Toast.makeText(requireContext(), "Pls on the Gps", Toast.LENGTH_LONG).show();
        });
    }

    private void updateLocation(Runnable ifLocationIsNull) {
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
        mMyLocationButton.setBackground(getResources().getDrawable(mIsAtCurrentLocation
                ? R.drawable.my_location_visible
                : R.drawable.my_location_not_visible));
    }

}//==============================Code End==============================