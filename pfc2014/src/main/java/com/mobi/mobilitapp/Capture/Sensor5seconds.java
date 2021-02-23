package com.mobi.mobilitapp.Capture;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fran Medina on 29/09/17.
 */

public class Sensor5seconds extends Service implements SensorEventListener {

    private static int SENSOR_SAMPLING_PERIOD = 10000;
    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/5seconds";
    public String [] className = {"Walk","Run","Bicycle","Motorbike","Car","Bus","Metro","Train","Tram","Stationary"};
    private Context mContext;

    public static final Object sendData = new Object();

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SensorManager mSensorManager;
    private Sensor sAccelerometer;
    private Sensor sMagneticField;

    private List<SensorSample> accelerometerData;
    private List<SensorSample> magneticFieldData;
    private long timestamp;


    public Sensor5seconds(Context mContext) {

        this.mContext = mContext;
        powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLockSensorCapture");
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.v("SENSOR", "Initialized");
    }

    public String startCapturing() {

        int sel = 9;
        MyRunnable r = new MyRunnable();
        Thread t = new Thread(r);

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");

        wakeLock.acquire();

        Log.v("SENSOR", "Start Capture");

        accelerometerData = new ArrayList<SensorSample>();
        magneticFieldData = new ArrayList<SensorSample>();

        mSensorManager.registerListener(this, sAccelerometer, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sMagneticField, SENSOR_SAMPLING_PERIOD, new Handler());

        // Stop and wait
        t.start();
        synchronized (sendData) {
            try {
                Log.v("SENSOR", "Waiting for sendData to finish...");
                sendData.wait();
                Log.v("SENSOR", "Data is ready...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mSensorManager.unregisterListener(this);
        wakeLock.release();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v("SENSOR", "Saving files...");
                String FILENAME_FORMAT = "_%s" + ".csv";

                // ACCELEROMETER
                String filename = String.format(FILENAME_FORMAT, "ACCELEROMETER");
                CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                csv.writeLine("timestamp,x,y,z");
                for (SensorSample p : accelerometerData)
                    csv.writeLine(Long.valueOf(p.timestamp) +
                    "," + p.x + "," + p.y + "," + p.z);
                csv.close();

                // MAGNETICFIELD
                filename = String.format(FILENAME_FORMAT, "MAGNETICFIELD");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                csv.writeLine("timestamp,x,y,z");
                for (SensorSample p : magneticFieldData)
                    csv.writeLine(Long.valueOf(p.timestamp) +
                            "," + p.x + "," + p.y + "," + p.z);
                csv.close();
            }
        }).start();

        Log.v("SENSOR", "Data writed!");



        return className[sel];
    }

    public static class MyRunnable implements Runnable {

        @Override
        public void run() {
            Log.v("RUN", "Begin the timer");
            synchronized (sendData) {
                Log.v("RUN", "Sync");
                sendData.notify();
                Log.v("RUN", "Exiting from Sync");
                try {
                    Thread.currentThread().sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.v("RUN", "Exiting from run()");
        }
    }

    // TIMER
    // After X seconds it will notify to the main thread
/*    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v("HOLA", "Counting down... " + currentTime);
            currentTime = System.currentTimeMillis() - startTime;
            timerHandler.post(this);
            Log.v("SENSOR", "Runnable...");

            if (currentTime > PERIOD) {
                synchronized (sendData) {
                    sendData.notify();
                }
            }
        }
    };*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        timestamp = new Date().getTime();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            accelerometerData.add(new SensorSample(timestamp, x, y, z));
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            magneticFieldData.add(new SensorSample(timestamp, x, y, z));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
