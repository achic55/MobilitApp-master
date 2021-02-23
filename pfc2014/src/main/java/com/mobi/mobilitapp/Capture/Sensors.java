package com.mobi.mobilitapp.Capture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.koushikdutta.ion.builder.Builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Fran Medina on 4/10/17.
 */

public class Sensors extends Service implements SensorEventListener{

    private static int SENSOR_SAMPLING_PERIOD = 10000;
    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/Fran/sensor";
    private Context mContext;
    private long startTime, currentTime;

    private PowerManager mManager;
    private PowerManager.WakeLock wakeLock;
    private SensorManager mSensorManager;
    private Sensor sAccelerometer;
    private Sensor sMagneticfield;

    private List<SensorSample> accelerometerData;
    private List<SensorSample> magneticfieldData;
    private long timestamp;

    private static final Object send = new Object();

    private static final String MyTag = "SENSOR";


    public Sensors(Context context) {
        this.mContext = context;
        mManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = mManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKELOCK");

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMagneticfield = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.d(MyTag, "Sensors are initialized");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        timestamp = new Date().getTime();
        Log.d("CHANGE", "Hola " + x);

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData.add(new SensorSample(timestamp, x, y, z));
            Log.d(MyTag, "Accelerometer saved");
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticfieldData.add(new SensorSample(timestamp, x, y, z));
            Log.d(MyTag, "Magnetic Field saved");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void newCapture() {

        MyRunnable run = new MyRunnable();
        Thread mThread = new Thread(run);

        wakeLock = mManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKELOCK");
        wakeLock.acquire();
        Log.d(MyTag, "Capturing...");

        accelerometerData = new ArrayList<SensorSample>();
        magneticfieldData = new ArrayList<SensorSample>();

        mSensorManager.registerListener(this, sAccelerometer, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sMagneticfield, SENSOR_SAMPLING_PERIOD, new Handler());

        new OneMinute(this).execute(this);

        // WAIT FOR GATHER DATA
        /*mThread.start();
        synchronized (send) {
            Log.d(MyTag, "Waiting for the mThread to finish");
            try {
                send.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

/*        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(MyTag, "Saving files... " + accelerometerData);
                String FILENAME_FORMAT = "%s" + ".csv";

                // ACCELEROMETER
                String filename = String.format(FILENAME_FORMAT, "ACCELEROMETER");
                CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                csv.writeLine("timestamp,x,y,z");
                for (SensorSample p : accelerometerData)
                    csv.writeLine(Long.valueOf(p.timestamp) +
                            "," + p.x + "," + p.y + "," + p.z);
                csv.close();

                // MAGNETIC FIELD
                filename = String.format(FILENAME_FORMAT, "MAGNETICFIELD");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                csv.writeLine("timestamp,x,y,z");
                for (SensorSample p : magneticfieldData)
                    csv.writeLine(Long.valueOf(p.timestamp) +
                            "," + p.x + "," + p.y + "," + p.z);
                csv.close();
            }
        }).start();

        mSensorManager.unregisterListener(this);
        wakeLock.release();*/

    }

    public void oneMinute () {
        MyRunnable run = new MyRunnable();
        Thread mThread = new Thread(run);

                // WAIT FOR GATHER DATA
        mThread.start();
        synchronized (send) {
            Log.d(MyTag, "Waiting for the mThread to finish");
            try {
                send.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(MyTag, "Saving files... " + accelerometerData);
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

                // MAGNETIC FIELD
                filename = String.format(FILENAME_FORMAT, "MAGNETICFIELD");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                csv.writeLine("timestamp,x,y,z");
                for (SensorSample p : magneticfieldData)
                    csv.writeLine(Long.valueOf(p.timestamp) +
                            "," + p.x + "," + p.y + "," + p.z);
                csv.close();
            }
        }).start();

        mSensorManager.unregisterListener(this);
        wakeLock.release();

    }

    public class MyRunnable implements Runnable {

        @Override
        public void run() {

            synchronized (send) {
                send.notify();
            }

            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class OneMinute extends AsyncTask<Sensors, Void, String> {

        Sensors sensors;

        private OneMinute (Sensors sensors) {
            this.sensors = sensors;
        }

        @Override
        protected String doInBackground(Sensors... sensor) {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String string) {

            mSensorManager.unregisterListener(this.sensors);
            wakeLock.release();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.v(MyTag, "Saving files... " + accelerometerData);
                    String FILENAME_FORMAT = "%s" + ".csv";

                    // ACCELEROMETER
                    String filename = String.format(FILENAME_FORMAT, "ACCELEROMETER");
                    CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
                    csv.open();
                    csv.writeLine("timestamp,x,y,z");
                    for (SensorSample p : accelerometerData)
                        csv.writeLine(Long.valueOf(p.timestamp) +
                                "," + p.x + "," + p.y + "," + p.z);
                    csv.close();

                    // MAGNETIC FIELD
                    filename = String.format(FILENAME_FORMAT, "MAGNETICFIELD");
                    csv = new CSVFile(FILE_STORE_DIR, filename);
                    csv.open();
                    csv.writeLine("timestamp,x,y,z");
                    for (SensorSample p : magneticfieldData)
                        csv.writeLine(Long.valueOf(p.timestamp) +
                                "," + p.x + "," + p.y + "," + p.z);
                    csv.close();
                }
            }).start();

        }

    }
}
