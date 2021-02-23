package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

/**
 * Created by toni_ on 20/06/2016.
 */
public interface IAccelerometerManager {
    void reStart();

    boolean isAlertState();

    boolean isEmergencyState();

    void setAlertState(boolean alertState);

    void setEmergencyState(boolean emergencyState);

    boolean elapsedTimeSinceCrash(long timeToWait, long actualTime);

    void activateEmergency(long time);
}
