package com.timetwist.bottombar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.databinding.FragmentMapBinding;
import com.timetwist.ui.manager.MapHelper;
import com.timetwist.ui.manager.MapUIManager;

public class MapFragment extends Fragment {
    private FragmentMapBinding mBinding;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private MapUIManager mMapUIManager;
    private String mPendingMarkerName;
    private boolean mIsMapReady = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMapBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeMap();
    }

    private void initializeMap() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this::onMapReady);
    }

    private void onMapReady(GoogleMap map) {
        mMap = map;
        initializeMapHelper();
        setupMapUIManager();
        mIsMapReady = true;

        if (mPendingMarkerName != null) {
            zoomToFavoriteMarker(mPendingMarkerName);
            mPendingMarkerName = null;
        }
    }

    private void initializeMapHelper() {
        MapHelper.initializePlaces(requireActivity().getApplicationContext(),
                getString(R.string.my_map_Api_key));
        MapHelper.enableMyLocationIfPermitted(mMap, requireContext());
    }

    private void setupMapUIManager() {
        mMapUIManager = new MapUIManager(requireContext(), this, mBinding, mMap, mFusedLocationClient);
        mMapUIManager.configureMap();
        mMapUIManager.configureCompassPlace();
        mMapUIManager.configureAutocomplete(getChildFragmentManager());
    }

    public void refreshMapMarkers(Boolean deleteOrAdd, String name) {
        if (mMap == null) return;
        if (deleteOrAdd) mMapUIManager.addCustomMarkers();

        mMapUIManager.getCustomMarkers().stream().filter(marker ->
                marker.getTitle().equals(name)).findAny().ifPresent(marker -> {
            marker.remove();
            mMapUIManager.getCustomMarkers().remove(marker);
        });
    }

    public boolean checkIfMarkerWithSameNameExists(String name) {
        return mMapUIManager.getCustomMarkers().stream()
                .anyMatch(marker -> marker.getTitle().equals(name));
    }

    public boolean checkIfMarkerWithSameLocationExists(GeoPoint geoPoint) {
        return mMapUIManager.getCustomMarkers().stream()
                .anyMatch(marker -> marker.getPosition().latitude == geoPoint.getLatitude() &&
                        marker.getPosition().longitude == geoPoint.getLongitude());
    }

    public void prepareZoomToFavoriteMarker(String markerName) {
        if (mIsMapReady) {
            zoomToFavoriteMarker(markerName);
            return;
        }
        mPendingMarkerName = markerName;
    }

    public void zoomToFavoriteMarker(String name) {
        if (mMapUIManager == null) {
            Log.e("MapFragment", "MapUIManager is not initialized.");
            return;
        }
        ((MainActivity) requireActivity()).getBottomBar().selectTabById(R.id.map, true);
        mMapUIManager.zoomToMarkerByName(name);
    }

    public void cancelDialogAndMapClickListener() {
        mMapUIManager.cancelDialog();
        mMap.setOnMapClickListener(null);
    }

    public void configureWebView(String url) {
        mBinding.webview.setVisibility(View.VISIBLE);
        mBinding.webview.loadUrl(url);
        WebSettings webSettings = mBinding.webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mBinding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}