package com.mobi.mobilitapp.CrashDetection.Entities;

/**
 * Created by toni_ on 20/06/2016.
 */
public class EmergencySensorData implements IEmergencySensorData{

    private String time;
    private double x;
    private double y;
    private double z;

    @Override
    public String getTime(){
        return time;
    }
    @Override
    public void setTime(String time){
        this.time = time;
    }
    @Override
    public double getX() {
        return x;
    }
    @Override
    public void setX(double x) {
        this.x = x;
    }
    @Override
    public double getY() {
        return y;
    }
    @Override
    public void setY(double y) {
        this.y = y;
    }
    @Override
    public double getZ() {
        return z;
    }
    @Override
    public void setZ(double z) {
        this.z = z;
    }

    public void setValues(String time, double x, double y, double z){
        setTime(time);
        setX(x);
        setY(y);
        setZ(z);
    }

}
