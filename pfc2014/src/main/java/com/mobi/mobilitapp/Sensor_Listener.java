package com.mobi.mobilitapp;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Gerard Marrugat on 28/02/2016.
 */
public class Sensor_Listener extends IntentService implements SensorEventListener {

    double x, y, z,x_gyro,y_gyro,z_gyro;

    private final float THERESLHOLD = (float) 0.4;

    private final float THERESLHOLD_GYRO = (float) 0.05;

    private boolean first_sample = true;
    private boolean first_sample_gyro = true;

    String timestamp;

    private long start_time, currentTime;

    private int position = 0;
    private int position_gyro = 0;

    //Specify the object type which will be stored in the ArrayList
    private SensorDataList AccelerationData;

    //Specify the object type which will be stored in the ArrayList
    private SensorDataList GyroscopeData;

    private static final String MyTAG = "MyTAG";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private PowerManager.WakeLock mWakeLock = null;

    Object send_data = new Object();


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Sensor_Listener(String name) {
        super(name);
    }

    public Sensor_Listener() {
        super("Sensor_Listener");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Instance the SENSOR_SERVICE
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Log.v(MyTAG, "Sensor Manager");

        registerListener();

        Log.v(MyTAG, "Listener registered");

        //ResultReceiver rec = intent.getParcelableExtra("receiverTag");

       // Log.v(MyTAG, "Receiver registered" + rec.toString());

        first_sample = true;
        first_sample_gyro = true;

        // Creates an arraylist where the accelerometer data will be stored
        AccelerationData = new SensorDataList("AccelerationData");

        // Creates an arraylist where the accelerometer data will be stored
        GyroscopeData = new SensorDataList("GyroscopeData");

        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MyTAG);

        // Moment in which the accelerometer is going to store data
        start_time = System.currentTimeMillis();

        //call the run method of timerRunnable
        timerHandler.post(timerRunnable);

        mWakeLock.acquire();
        startForeground(android.os.Process.myPid(), new Notification());

        //Stop and wait until 20 seconds would have passed
        synchronized (send_data){
            try {
                Log.v(MyTAG,"Wait for Data");
                send_data.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        unregisterListener();

        mWakeLock.release();
        stopForeground(true);

        //Stops timerRunnable
        timerHandler.removeCallbacks(timerRunnable);

        //Call the function in the LocationService that will store the data in the server
        new LocationService().StoreDataSensor(AccelerationData);

         Log.v(MyTAG, "AccelerationData_OK");

        new LocationService().StoreDataSensor(GyroscopeData);

         Log.v(MyTAG, "GyroscopeData_OK");



    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            double deltaX = 0, deltaY = 0, deltaZ = 0;

            deltaX = x - event.values[0];
            deltaY = y - event.values[1];
            deltaZ = z - event.values[2];

            //alpha is the LPF constant, alpha = t/(t + dT); t = LPF time constant, dT = time interval between input data
            final float alpha = (float) 0.8;

            if (deltaX > THERESLHOLD || deltaY > THERESLHOLD || deltaZ > THERESLHOLD) {
                //Define a thereshold that identifies the end of the acceleration period

                //We collect data from acceleration peaks but we haven´t marked the end of each peak yet

                if (first_sample) {

                    timestamp = time(currentTime, "mm:ss:SSS");
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    first_sample = false;
                    position++;

                } else {

                    try {


                    //We use a low pass filter to remove short-terme fluctuations(Jitter) on our samples
                    timestamp = time(currentTime, "mm:ss:SSS");
                    x = alpha * event.values[0] + (1 - alpha) * AccelerationData.get(position - 1).getX();
                    y = alpha * event.values[1] + (1 - alpha) * AccelerationData.get(position - 1).getY();
                    z = alpha * event.values[2] + (1 - alpha) * AccelerationData.get(position - 1).getZ();

                    position++;
                    }
                    catch(Exception ex)
                    {
                        Log.v(MyTAG, "Sensor_Listener_error");
                        first_sample = true;
                    }
                }


                //When the accelerometer receives new values a SensorData object is created with these values, after that this object will be
                // stored in an array

                SensorData data_accelerometer = new SensorData(timestamp, x, y, z);

                try{
                    AccelerationData.add(data_accelerometer);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Log.v(MyTAG, "Collecting Accelerometer´s Data" + currentTime);

            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            double deltaX_gyro = 0, deltaY_gyro = 0, deltaZ_gyro = 0;

            deltaX_gyro = x_gyro - event.values[0];
            deltaY_gyro = y_gyro - event.values[1];
            deltaZ_gyro = z_gyro - event.values[2];

            //alpha is the LPF constant, alpha = t/(t + dT); t = LPF time constant, dT = time interval between input data
            final float alpha = (float) 0.8;

            if (deltaX_gyro > THERESLHOLD_GYRO || deltaY_gyro > THERESLHOLD_GYRO || deltaZ_gyro > THERESLHOLD_GYRO) {
                //Define a thereshold that identifies the end of the acceleration period

                if (first_sample_gyro) {

                    timestamp = time(currentTime, "mm:ss:SSS");
                    x_gyro = event.values[0];
                    y_gyro = event.values[1];
                    z_gyro = event.values[2];

                    first_sample_gyro = false;
                    position_gyro++;

                } else {

                    //We use a low pass filter to remove short-terme fluctuations(Jitter) on our samples
                    timestamp = time(currentTime, "mm:ss:SSS");
                    x_gyro = alpha * event.values[0] + (1 - alpha) * GyroscopeData.get(position_gyro - 1).getX();
                    y_gyro = alpha * event.values[1] + (1 - alpha) * GyroscopeData.get(position_gyro - 1).getZ();
                    z_gyro = alpha * event.values[2] + (1 - alpha) * GyroscopeData.get(position_gyro - 1).getZ();

                    position_gyro++;
                }

                //When the accelerometer receives new values a SensorData object is created with these values, after that this object will be
                // stored in an array

                SensorData data_gyro = new SensorData(timestamp, x_gyro, y_gyro, z_gyro);

                GyroscopeData.add(data_gyro);

                Log.v(MyTAG, "Collecting Gyroscope´s Data" + currentTime);

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    //Timer
    // After twenty seconds it will notify to the main thread and the code won´t listen the sensors after two minutes
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //this method emulates a timer

            currentTime = System.currentTimeMillis() - start_time;

            //Very Important! this Handler method calls the runnable again
            timerHandler.post(this);

            if(currentTime > 20000){

                synchronized (send_data){
                    send_data.notify();
                }
            }

        }
    };

    /*
     * Register this as a sensor event listener.
     */
    private void registerListener() {
        // Instance the accelerometer and gyroscope
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // registerListener(EventListener, Sensor, rate)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * Un-register this as a sensor event listener.
     */
    private void unregisterListener() {
        mSensorManager.unregisterListener(this);
    }


}
