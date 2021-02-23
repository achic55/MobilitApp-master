package com.mobi.mobilitapp.Capture;

/**
 * Created by Mahmoud Elbayoumy on 10/24/2017.
 */

public class LocationCoordinates {

    long timestamp;
    float lat;
    float lon;

    public LocationCoordinates(long timestamp, float lat, float lon){
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
    }
}
