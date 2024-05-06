package com.timetwist.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    public static boolean isWifiDisconnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return true;
        }

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return true;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities == null || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}
