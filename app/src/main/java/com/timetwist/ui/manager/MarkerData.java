package com.timetwist.ui.manager;

import java.io.Serializable;

public class MarkerData implements Serializable {
    private final double lat;
    private final double lng;

    public MarkerData(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
