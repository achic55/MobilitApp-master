package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 20/06/2016.
 */
public interface IEmergencyData {
    double getLongitude();

    void setLongitude(double longitude);

    double getLatitude();

    void setLatitude(double latitude);

    String getTime();

    void setTime(String time);

    String getType();

    void setType(String type);

    void setData(String time, double latitude, double longitude, String type);
}
