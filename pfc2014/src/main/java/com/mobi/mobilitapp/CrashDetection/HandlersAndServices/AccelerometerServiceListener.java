package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import com.mobi.mobilitapp.CrashDetection.DifferentTypes.PreferencesTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.EmergencySensorData;
import com.mobi.mobilitapp.CrashDetection.DifferentTypes.EmergencyTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.IEmergencySensorData;
import com.mobi.mobilitapp.CrashDetection.Entities.ILastLocationSaved;
import com.mobi.mobilitapp.CrashDetection.Entities.LastLocationSaved;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by toni_ on 20/05/2016.
 */

public class AccelerometerServiceListener extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private IAccelerometerManager IAccelerometerManager;
    private EmergencyManager emergencyManager;

    private long start_time, currentTime;

    private long time30Seconds;
    private long time60Seconds;
    private long time65Seconds;
    private long time90Seconds;

    String timestamp;
    double x, y, z;

    private static final String MyTAG = "MyTAG";
    private IEmergencySensorData lastSensorData;
    private final float THERESLHOLD = (float) 1;
    private float maximumAccelerationThreshold;
    private PowerManager.WakeLock mWakeLock;
    SharedPreferences prefs;
    ILastLocationSaved lastLocation;
    Context context;

    /*
     * Register this as a sensor event listener.
     */
    private void registerListener() {
        // Instance the accelerometer and gyroscope
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // registerListener(EventListener, Sensor, rate)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * Un-register this as a sensor event listener.
     */
    private void unregisterListener() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        time30Seconds = 30000;
        time60Seconds = 60000;
        time65Seconds = 65000;
        time90Seconds = 90000;
        mWakeLock = null;

        // Instance the SENSOR_SERVICE
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        registerListener();

        IAccelerometerManager = new AccelerometerManager();
        emergencyManager = new EmergencyManager();
        lastSensorData = null;

        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MyTAG);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        //Deprecated. To set different senseibility levels
        //{
            //String valueMaximumAccTh = prefs.getString(PreferencesTypes.sensitivityCrashMaximumThreshold.toString(), "16");
            //maximumAccelerationThreshold = Float.parseFloat(valueMaximumAccTh);
        //}

        maximumAccelerationThreshold = 19;
        lastLocation = new LastLocationSaved();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterListener();

        mWakeLock.release();
        stopForeground(true);

        //Stops timerRunnable
        timerHandler.removeCallbacks(timerRunnable);

        Log.v(MyTAG, "AccelerationData_OK");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Moment in which the accelerometer is going to store data
        start_time = System.currentTimeMillis();

        //call the run method of timerRunnable
        timerHandler.post(timerRunnable);

        mWakeLock.acquire();
        startForeground(android.os.Process.myPid(), new Notification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double deltaX = 0, deltaY = 0, deltaZ = 0;

            deltaX = x - event.values[0];
            deltaY = y - event.values[1];
            deltaZ = z - event.values[2];

            //alpha is the LPF constant, alpha = t/(t + dT); t = LPF time constant, dT = time interval between input data
            final float alpha = (float) 0.8;

            if (deltaX > THERESLHOLD || deltaY > THERESLHOLD || deltaZ > THERESLHOLD) {
                //Define a thereshold that means that the device is on moving

                //We collect data from acceleration peaks but we haven´t marked the end of each peak yet
                timestamp = time(currentTime, "mm:ss:SSS");

                if(lastSensorData==null){
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    lastSensorData = new EmergencySensorData();
                    lastSensorData.setValues(timestamp,x,y,z);

                } else {

                    if (deltaX > maximumAccelerationThreshold || deltaY > maximumAccelerationThreshold
                            || deltaZ > maximumAccelerationThreshold) {
                        IAccelerometerManager.activateEmergency(currentTime);
                        emergencyManager.askAboutEmergency(this);
                        emergencyManager.beepSound();
                    }
                    //This Try-Catch prevents from an unusual error when the accelerometer get a null value.
                    try{
                        //We use a low pass filter to remove short-terme fluctuations(Jitter) on our samples
                        //x = alpha * event.values[0] + (1 - alpha) * AccelerationData.get(position - 1).getX();
                        x = alpha * event.values[0] + (1 - alpha) * lastSensorData.getX();
                        y = alpha * event.values[1] + (1 - alpha) * lastSensorData.getY();
                        z = alpha * event.values[2] + (1 - alpha) * lastSensorData.getZ();
                    }catch (Exception e){
                        x = event.values[0];
                        y = event.values[1];
                        z = event.values[2];
                    }

                    if (IAccelerometerManager.isAlertState() == true) {
                        if (IAccelerometerManager.elapsedTimeSinceCrash(time60Seconds,currentTime)) {
                            IAccelerometerManager.reStart();
                            emergencyManager.reStart();
                            //Everything is okey, there is no emergency.

                        } else if (IAccelerometerManager.elapsedTimeSinceCrash(time30Seconds,currentTime)
                                && IAccelerometerManager.isEmergencyState()) {
                            IAccelerometerManager.setEmergencyState(false);
                            //The device is moving after a crash, we keep on alert state.
                        }
                    }
                }
                lastSensorData.setValues(timestamp, x, y, z);
            }

            else //The device doesn't move
            {
                if (IAccelerometerManager.isAlertState() == true) {

                    if (IAccelerometerManager.elapsedTimeSinceCrash(time65Seconds,currentTime)
                            && emergencyManager.isCallingEmergencies()==false) {
                        emergencyManager.beepSound();
                    }

                    if(IAccelerometerManager.elapsedTimeSinceCrash(time90Seconds, currentTime))
                    {//Crash detected
                            if(!emergencyManager.isCallingEmergencies()){
                                emergencyManager.setCallingEmergencies(true);
                                context = this;

                                //We send the message to the server with a Service to separate it from the principal goal: eCall()
                                prefs.edit().putString(EmergencyTypes.emergencyType.toString(),
                                        EmergencyTypes.EmergencyAccelerometer.toString()).commit();
                                startService(new Intent(this, EmergencyService.class));

                                prefs.edit().putString(PreferencesTypes.switchCrashOnOff.toString(),
                                        PreferencesTypes.off.toString()).commit();
                                this.stopSelf();
                            }
                    }
                }
            }
        }
    }

    //Gives format to a date
    //Creates a format class with a specific format pattern
    public static String time(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        //Get the current date and time
        Calendar calendar = Calendar.getInstance();
        //Converts the time into milliseconds
        calendar.setTimeInMillis(milliSeconds);
        String time = formatter.format(calendar.getTime()).toString();
        return time;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //this method emulates a timer
            currentTime = System.currentTimeMillis() - start_time;

            //Very Important! this Handler method calls the runnable again
            timerHandler.post(this);

        }
    };
}
