package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 20/06/2016.
 */
public interface ILastLocationSaved {
    String getLatitudeAtLastLocation();

    void setLatitudeAtLastLocation(String latitudeAtLastLocation);

    String getLongitudeAtLastLocation();

    void setLongitudeAtLastLocation(String longitudeAtLastLocation);

    String getTimeAtLastLocation();

    void setTimeAtLastLocation(String timeAtLastLocation);

    void refreshData(String time, String latitude, String longitude);
}
