package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 04/06/2016.
 */
public class EmergencyData implements IEmergencyData {

    private String time;
    private double latitude;
    private double longitude;
    private String type;

    public EmergencyData() {
        this.time = "";
        this.latitude = 0;
        this.longitude = 0;
        this.type = "";
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setData(String time, double latitude, double longitude, String type){
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }
}
