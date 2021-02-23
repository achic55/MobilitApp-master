package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 04/06/2016.
 */
public class LastLocationSaved implements ILastLocationSaved {
    String timeAtLastLocation;
    String latitudeAtLastLocation;
    String longitudeAtLastLocation;

    public LastLocationSaved() {
        this.longitudeAtLastLocation = "";
        this.latitudeAtLastLocation = "";
        this.timeAtLastLocation = "";
    }

    @Override
    public String getLatitudeAtLastLocation() {
        return latitudeAtLastLocation;
    }

    @Override
    public void setLatitudeAtLastLocation(String latitudeAtLastLocation) {
        this.latitudeAtLastLocation = latitudeAtLastLocation;
    }

    @Override
    public String getLongitudeAtLastLocation() {
        return longitudeAtLastLocation;
    }

    @Override
    public void setLongitudeAtLastLocation(String longitudeAtLastLocation) {
        this.longitudeAtLastLocation = longitudeAtLastLocation;
    }

    @Override
    public String getTimeAtLastLocation() {
        return timeAtLastLocation;
    }

    @Override
    public void setTimeAtLastLocation(String timeAtLastLocation) {
        this.timeAtLastLocation = timeAtLastLocation;
    }

    @Override
    public void refreshData(String time, String latitude, String longitude){
        setLatitudeAtLastLocation(latitude);
        setLongitudeAtLastLocation(longitude);
        setTimeAtLastLocation(time);
    }
}
