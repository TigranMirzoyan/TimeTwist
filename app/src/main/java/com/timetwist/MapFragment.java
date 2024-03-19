package com.timetwist;

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

import java.util.Arrays;


public class MapFragment extends Fragment {
    private GoogleMap mMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private Button myLocationButton;
    private boolean isAtCurrentLocation = false;
    private LatLng currentLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        myLocationButton = view.findViewById(R.id.myLocationBtn);

        return view;


    }//==========================onCreateView End==========================

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompletePlace();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                permissionscheck();
                UiSettings();
                Buttons();
                setupMapListeners();
                getCurrentLocation();
            }
        });
    }//==========================onViewCreated End==========================

    //-----------------------------Permissions---------------------------------
    public void permissionscheck() {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.my_map_Api_key));
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
    //---------------------------Permissions End-------------------------------

    //--------------------------AutoComplete Fragment--------------------------
    public void autocompletePlace() {
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng != null && mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });
    }
    //------------------------AutoComplete Fragment End------------------------

    //---------------------------UiSettings Control----------------------------
    public void UiSettings() {

        View compass = getView().findViewById(Integer.parseInt("1"));
        if (compass != null) {
            View locationCompass = ((View) compass.getParent()).findViewById(Integer.parseInt("5"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationCompass.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.setMargins(30, 0, 0, 100);
            locationCompass.setLayoutParams(layoutParams);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }
    //-------------------------UiSettings Control End--------------------------

    //---------------------------------Buttons---------------------------------
    public void Buttons() {
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToCurrentLocation();
            }
        });
    }
    //-------------------------------Buttons End-------------------------------

    //----------------------------------Logic----------------------------------

        //****************************MyLocation Button****************************
    private void moveToCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                isAtCurrentLocation = true;
                updateMyLocationButtonDrawable();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            } else {
                Log.d("MapFragment", "Current location is null.");
                Toast.makeText(requireContext(), "Pls on the Gps", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupMapListeners() {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isAtCurrentLocation = false;
                updateMyLocationButtonDrawable();
            }
        });
    }

    private void updateMyLocationButtonDrawable() {
        if (isAtCurrentLocation) {
            myLocationButton.setBackground(getResources().getDrawable(R.drawable.my_location_visible));
        } else {
            myLocationButton.setBackground(getResources().getDrawable(R.drawable.my_location_not_visible));
        }
    }

    private void getCurrentLocation() {

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                isAtCurrentLocation = true;
                updateMyLocationButtonDrawable();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

            } else {

                Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }
        //****************************MyLocation Button End*************************

    //--------------------------------Logic End--------------------------------

}//==============================Code End==============================