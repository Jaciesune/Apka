package com.example.explorex;

import com.google.android.gms.maps.model.LatLng;

public class GroupedLatLng {
    private LatLng latLng;
    private int group;

    public GroupedLatLng(LatLng latLng, int group) {
        this.latLng = latLng;
        this.group = group;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getLatitude() {
        return latLng.latitude;
    }

    public double getLongitude() {
        return latLng.longitude;
    }

    public int getGroup() {
        return group;
    }
}
