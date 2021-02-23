package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

/**
 * Created by toni_ on 20/05/2016.
 */
public class AccelerometerManager implements IAccelerometerManager {

    private boolean alertState;
    private boolean emergencyState;
    private long alertTime;

    public AccelerometerManager() {
        this.emergencyState = false;
        this.alertTime = 0;
        this.alertState = false;
    }

    @Override
    public void reStart()
    {
        this.emergencyState = false;
        this.alertTime = 0;
        this.alertState = false;
    }

    @Override
    public boolean isAlertState() {
        return alertState;
    }

    @Override
    public boolean isEmergencyState(){
        return emergencyState;
    }

    @Override
    public void setAlertState(boolean alertState) {
        this.alertState = alertState;
    }

    @Override
    public void setEmergencyState(boolean emergencyState) {
        this.emergencyState = emergencyState;
    }

    @Override
    public boolean elapsedTimeSinceCrash(long timeToWait, long actualTime)
    {
        long elapsedTime = actualTime - alertTime;
        if(elapsedTime>=timeToWait){
            return  true;
        }
        return false;
    }

    @Override
    public void activateEmergency(long time){
        alertState = true;
        emergencyState = true;
        alertTime = time;
    }
}