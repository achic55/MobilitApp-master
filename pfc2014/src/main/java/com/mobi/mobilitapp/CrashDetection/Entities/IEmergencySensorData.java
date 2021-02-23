package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 20/06/2016.
 */
public interface IEmergencySensorData {

    String getTime();

    void setTime(String time);

    double getX();

    void setX(double x);

    double getY();

    void setY(double y);

    double getZ();

    void setZ(double z);

    void setValues(String time, double x, double y, double z);

}
