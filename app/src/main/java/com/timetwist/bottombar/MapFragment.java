package com.timetwist.bottombar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.timetwist.R;
import com.timetwist.ui.manager.MapHelper;
import com.timetwist.ui.manager.MapUIManager;

import java.util.Objects;

public class MapFragment extends Fragment {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mMyLocationButton, mAddMarkerButton;
    private TextView mChangeMarkers;
    private MapUIManager mMapUIManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMyLocationButton = view.findViewById(R.id.myLocationBtn);
        mAddMarkerButton = view.findViewById(R.id.addMarker);
        mChangeMarkers = view.findViewById(R.id.changeMarkers);

        configureFusedLocationClient();
    }

    private void onMapReady(GoogleMap map) {
        mMap = map;
        MapHelper.initializePlaces(requireActivity().getApplicationContext(), getString(R.string.my_map_Api_key));
        MapHelper.enableMyLocationIfPermitted(mMap, requireContext());
        mMapUIManager = new MapUIManager(requireContext(), this,
                requireView(), mMyLocationButton, mMap, mFusedLocationClient);
        mMapUIManager.configureMap(mAddMarkerButton, mChangeMarkers);
        mMapUIManager.configureCompassPlace();
        mMapUIManager.configureAutocomplete(getChildFragmentManager());
    }

    private void configureFusedLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                .findFragmentById(R.id.map))).getMapAsync(this::onMapReady);
    }

    public void refreshMapMarkers() {
        if (mMap == null) {
            Log.e("MapUIManager", "Cannot refresh markers ");
            return;
        }

        mMapUIManager.startMarkerDownloadingProcess();
        mMapUIManager.mCustomMarkers.forEach(Marker::remove);
        mMapUIManager.mCustomMarkers.clear();
        mMapUIManager.addMarkersFromFirebase();
        mMapUIManager.finishMarkerDownloadingProcess();
    }

    public void cancelDialog() {
        mMapUIManager.cancelDialog();
    }
}