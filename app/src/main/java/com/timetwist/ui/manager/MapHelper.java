package com.timetwist.ui.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.libraries.places.api.Places;

public class MapHelper {

    public static boolean hasLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void initializePlaces(Context context, String apiKey) {
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey);
        }
    }

    public static void enableMyLocationIfPermitted(GoogleMap map, Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }
}