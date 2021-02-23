package com.mobi.mobilitapp.Capture;

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
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;

/**
 * Created by rokslamek on 04/04/17.
 * Edited by Fran Medina on 29/09/17.
 */
public class SensorLoger extends Service implements SensorEventListener {

    private static int SENSOR_SAMPLING_PERIOD = 1000;
    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";


    private Context mContext;
    private int captureID;
    private String activityType;

    private PowerManager powerManager;
    private WakeLock wakeLock;
    private SensorManager mSensorManager;
    private Sensor sAccelerometer;
    private Sensor sMagneticField;
    private Sensor sPressure;
    private Sensor sHeartRate;
    private Sensor sStepCounter;

    private List<SensorSample> accelerometerData;
    private List<SensorSample> magneticFieldData;
    private List<SensorSampleSingleInput> pressureData;
    private List<SensorSampleSingleInput> heartRateData;
    private List<SensorSampleSingleInput> stepCounterData;
    //private List<String> allSensorsFlat;
    private long timestamp;

    private boolean isCaptureRunning = false;
    private boolean isCaptureSaved = true;


    public SensorLoger(Context mContext) {

        this.mContext = mContext;

        powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLockSensorCapture");

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


        Log.d("SENSOR", "Initialized");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

      //  float acc_x = 0, acc_y = 0, acc_z = 0, mag_x = 0, mag_y = 0, mag_z = 0, press_x = 0, heart_x = 0, step_x = 0;

        float x, y, z;

       /* x = event.values[0];
        y = event.values[1];
        z = event.values[2];*/

        //Log.d("CHANGE", "X = " + x);

        timestamp = new Date().getTime();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        //    acc_x = event.values[0];
        //    acc_y = event.values[1];
        //    acc_z = event.values[2];

            accelerometerData.add(new SensorSample(timestamp, x, y, z));
            //Log.d("BAYO: ACC" , x + "," + y + "," + z);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        //    mag_x = event.values[0];
        //    mag_y = event.values[1];
        //    mag_z = event.values[2];

            magneticFieldData.add(new SensorSample(timestamp, x, y, z));
            //Log.d("BAYO: MAG" , x + "," + y + "," + z);
        }
        else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {

         //   press_x = event.values[0];
            x = event.values[0];
            pressureData.add(new SensorSampleSingleInput(timestamp, x));
            //Log.d("BAYO: PRE" , x + "," + y + "," + z);
        }
        else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

         //   heart_x = event.values[0];
            x = event.values[0];
            heartRateData.add(new SensorSampleSingleInput(timestamp, x));
            //Log.d("BAYO: HEA" , x + "," + y + "," + z);
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

        //    step_x = event.values[0];
            x = event.values[0];
            stepCounterData.add(new SensorSampleSingleInput(timestamp, x));
            //Log.d("BAYO: STE" , x + "," + y + "," + z);
        }

        //allSensorsFlat.add(Long.toString(timestamp) + "," + acc_x + "," + acc_y + "," + acc_z + ","
          //      + mag_x + "," + mag_y + "," + mag_z + "," + press_x + "," + heart_x + "," + step_x);
    }

    public boolean isCaptureRunning() {

        return isCaptureRunning;
    }

    public boolean isReadyToSave() {

        return !isCaptureRunning && !isCaptureSaved;
    }
    public boolean isCaptureSaved() {

        return isCaptureSaved;
    }

    public boolean isNewCaptureAvailable() {

        return !isCaptureRunning && isCaptureSaved;
    }

    public List<SensorSample> getAccelerometerData() {
        return accelerometerData;
    }

    public void startNewCapture(int captureID, String activityType) {

        if (isCaptureRunning()) {
            Toast.makeText(this, "Capture start failed! Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
        wakeLock.acquire();

        Log.d("SENSOR", "Start capture");


        accelerometerData = new ArrayList<SensorSample>();
        magneticFieldData  = new ArrayList<SensorSample>();
        pressureData  = new ArrayList<SensorSampleSingleInput>();
        heartRateData  = new ArrayList<SensorSampleSingleInput>();
        stepCounterData  = new ArrayList<SensorSampleSingleInput>();
        //allSensorsFlat = new ArrayList<String>();

        this.captureID = captureID;
        this.activityType = activityType;

        mSensorManager.registerListener(this, sAccelerometer, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sMagneticField, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sPressure, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sHeartRate, SENSOR_SAMPLING_PERIOD, new Handler());
        mSensorManager.registerListener(this, sStepCounter, SENSOR_SAMPLING_PERIOD, new Handler());

        // Commented because we don't need the 'beep' to notice that is gathering data (Fran)
/*        // PLAY SOUND
        int duration = 22100;
        int sampleRate = 44100;
        double frequency = 5000;
        // AudioTrack definition
        int mBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave
        short[] mBuffer = new short[duration];
        for (int i = 0; i < mBuffer.length; i++) {
            double s = Math.sin((2.0*Math.PI * i/(sampleRate/frequency)));
            mBuffer[i] = (short) (s*Short.MAX_VALUE);
        }


        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        mAudioTrack.play();

        mAudioTrack.write(mBuffer, 0, mBuffer.length);
        mAudioTrack.stop();
        mAudioTrack.release();*/


        this.isCaptureRunning = true;
        this.isCaptureSaved = false;
    }

    public void stopCapture() {

        if (!isCaptureRunning()) return;

        Log.d("SENSOR", "Capture finished");

        mSensorManager.unregisterListener(this);

        wakeLock.release();

        isCaptureRunning = false;
        isCaptureSaved = false;
    }

    public void saveCapture() {

        if (!isReadyToSave()) {
            return;
        }

/*
        File dir = new File(FILE_STORE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            //publishProgress("Directory created");
            Log.d("BAYO" , "Sensor Directory Created!");
            Log.d("BAYO" , FILE_STORE_DIR);
        } else {
            Log.d("BAYO" , "Sensor Directory is already there!");
            Log.d("BAYO" , FILE_STORE_DIR);
            //publishProgress("Directory already exists");
        }*/

        new Thread(new Runnable() {
            public void run() {

                Log.d("SENSOR", "Saving sensor data to files");

                final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

                                /* All sensors in 1 file
                String filename = String.format(FILENAME_FORMAT, "ALL");
                CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
                csv.open();
                try {
                    for (String p : allSensorsFlat)
                        csv.writeLine(p);
                }catch (Exception e){
                    e.printStackTrace();
                }
                csv.close();
*/
                // ACCELEROEMTER
                int filePart = 0;
                String FILENAME_FORMAT =
                        //tm.getDeviceId()
                        ANDROID_ID
                                + "_" + String.format("%04d", captureID)
                                + "_" + activityType
                                + "_%s" // sensor type
                                //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                + "_" + String.format("%06d", filePart)
                                + ".csv";

                String filename = String.format(FILENAME_FORMAT, "ACC");
                CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
                //csv.createSensorDataFile(accelerometerData);
                try {
                    csv.open();
                    //writeLine("sensor,timestamp,x,y,z");
                    for (SensorSample p : accelerometerData) {
                        if(csv.getSize() > 300 * 1024){
                            csv.close();
                            filePart++;
                            FILENAME_FORMAT =
                                    //tm.getDeviceId()
                                    ANDROID_ID
                                            + "_" + String.format("%04d", captureID)
                                            + "_" + activityType
                                            + "_%s" // sensor type
                                            //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                            + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                            + "_" + String.format("%06d", filePart)
                                            + ".csv";
                            filename = String.format(FILENAME_FORMAT, "ACC");
                            csv = new CSVFile(FILE_STORE_DIR, filename);
                            csv.open();
                        }else {
                            csv.writeLine(Long.valueOf(p.timestamp) + "," + p.x + "," + p.y + "," + p.z);
                        }
                    }
                    csv.close();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("BAYO CSV", e.toString());
                }

  /*              csv.open();
                //csv.writeLine("type,timestamp,x,y,z");
                try {
                    for (SensorSample p : accelerometerData)
                        csv.writeLine("1," + Long.valueOf(p.timestamp) + "," + p.x + "," + p.y + "," + p.z);
                }catch (Exception e){
                    Log.d("BAYO ACC", e.toString());
                }
                csv.close();*/

                // MAGNETICFIELD
                filePart = 0;
                FILENAME_FORMAT =
                        //tm.getDeviceId()
                        ANDROID_ID
                                + "_" + String.format("%04d", captureID)
                                + "_" + activityType
                                + "_%s" // sensor type
                                //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                + "_" + String.format("%06d", filePart)
                                + ".csv";

                filename = String.format(FILENAME_FORMAT, "MAG");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                //csv.createSensorDataFile(accelerometerData);
                try {
                    csv.open();
                    //writeLine("sensor,timestamp,x,y,z");
                    for (SensorSample p : magneticFieldData) {
                        if(csv.getSize() > 300 * 1024){
                            csv.close();
                            filePart++;
                            FILENAME_FORMAT =
                                    //tm.getDeviceId()
                                    ANDROID_ID
                                            + "_" + String.format("%04d", captureID)
                                            + "_" + activityType
                                            + "_%s" // sensor type
                                            //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                            + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                            + "_" + String.format("%06d", filePart)
                                            + ".csv";
                            filename = String.format(FILENAME_FORMAT, "MAG");
                            csv = new CSVFile(FILE_STORE_DIR, filename);
                            csv.open();
                        }else {
                            csv.writeLine(Long.valueOf(p.timestamp) + "," + p.x + "," + p.y + "," + p.z);
                        }
                    }
                    csv.close();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("BAYO CSV", e.toString());
                }

                /*
                filename = String.format(FILENAME_FORMAT, "MAG");
                csv = new CSVFile(FILE_STORE_DIR, filename);

                csv.createSensorDataFile(magneticFieldData);
*/

                // PRESSURE
                filePart = 0;
                FILENAME_FORMAT =
                        //tm.getDeviceId()
                        ANDROID_ID
                                + "_" + String.format("%04d", captureID)
                                + "_" + activityType
                                + "_%s" // sensor type
                                //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                + "_" + String.format("%06d", filePart)
                                + ".csv";

                filename = String.format(FILENAME_FORMAT, "PRESS");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                //csv.createSensorDataFile(accelerometerData);
                try {
                    csv.open();
                    //writeLine("sensor,timestamp,x,y,z");
                    for (SensorSampleSingleInput p : pressureData) {
                        if(csv.getSize() > 300 * 1024){
                            csv.close();
                            filePart++;
                            FILENAME_FORMAT =
                                    //tm.getDeviceId()
                                    ANDROID_ID
                                            + "_" + String.format("%04d", captureID)
                                            + "_" + activityType
                                            + "_%s" // sensor type
                                            //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                            + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                            + "_" + String.format("%06d", filePart)
                                            + ".csv";
                            filename = String.format(FILENAME_FORMAT, "PRESS");
                            csv = new CSVFile(FILE_STORE_DIR, filename);
                            csv.open();
                        }else {
                            csv.writeLine(Long.valueOf(p.timestamp) + "," + p.x);
                        }
                    }
                    csv.close();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("BAYO CSV", e.toString());
                }

                /*
                filename = String.format(FILENAME_FORMAT, "PRESS");
                csv = new CSVFile(FILE_STORE_DIR, filename);

                csv.createSensorDataFileSingleInput(pressureData);
                */

                // HEARTRATE
                filePart = 0;
                FILENAME_FORMAT =
                        //tm.getDeviceId()
                        ANDROID_ID
                                + "_" + String.format("%04d", captureID)
                                + "_" + activityType
                                + "_%s" // sensor type
                                //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                + "_" + String.format("%06d", filePart)
                                + ".csv";

                filename = String.format(FILENAME_FORMAT, "HR");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                //csv.createSensorDataFile(accelerometerData);
                try {
                    csv.open();
                    //writeLine("sensor,timestamp,x,y,z");
                    for (SensorSampleSingleInput p : heartRateData) {
                        if(csv.getSize() > 300 * 1024){
                            csv.close();
                            filePart++;
                            FILENAME_FORMAT =
                                    //tm.getDeviceId()
                                    ANDROID_ID
                                            + "_" + String.format("%04d", captureID)
                                            + "_" + activityType
                                            + "_%s" // sensor type
                                            //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                            + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                            + "_" + String.format("%06d", filePart)
                                            + ".csv";
                            filename = String.format(FILENAME_FORMAT, "HR");
                            csv = new CSVFile(FILE_STORE_DIR, filename);
                            csv.open();
                        }else {
                            csv.writeLine(Long.valueOf(p.timestamp) + "," + p.x);
                        }
                    }
                    csv.close();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("BAYO CSV", e.toString());
                }

                /*
                filename = String.format(FILENAME_FORMAT, "HR");
                csv = new CSVFile(FILE_STORE_DIR, filename);

                csv.createSensorDataFileSingleInput(heartRateData);
                */


                // STEPCOUNTER
                filePart = 0;
                FILENAME_FORMAT =
                        //tm.getDeviceId()
                        ANDROID_ID
                                + "_" + String.format("%04d", captureID)
                                + "_" + activityType
                                + "_%s" // sensor type
                                //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                + "_" + String.format("%06d", filePart)
                                + ".csv";

                filename = String.format(FILENAME_FORMAT, "STEP");
                csv = new CSVFile(FILE_STORE_DIR, filename);
                //csv.createSensorDataFile(accelerometerData);
                try {
                    csv.open();
                    //writeLine("sensor,timestamp,x,y,z");
                    for (SensorSampleSingleInput p : stepCounterData) {
                        if(csv.getSize() > 300 * 1024){
                            csv.close();
                            filePart++;
                            FILENAME_FORMAT =
                                    //tm.getDeviceId()
                                    ANDROID_ID
                                            + "_" + String.format("%04d", captureID)
                                            + "_" + activityType
                                            + "_%s" // sensor type
                                            //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                            + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                                            + "_" + String.format("%06d", filePart)
                                            + ".csv";
                            filename = String.format(FILENAME_FORMAT, "STEP");
                            csv = new CSVFile(FILE_STORE_DIR, filename);
                            csv.open();
                        }else {
                            csv.writeLine(Long.valueOf(p.timestamp) + "," + p.x);
                        }
                    }
                    csv.close();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("BAYO CSV", e.toString());
                }

/*
                filename = String.format(FILENAME_FORMAT, "STEP");
                csv = new CSVFile(FILE_STORE_DIR, filename);

                csv.createSensorDataFileSingleInput(stepCounterData);
*/
                isCaptureSaved = true;

                Log.d("SENSOR", "Data successfully writed to files");
            }
        }).start();
    }

    public void discardCapture() {

        if (isCaptureRunning()) {
            stopCapture();
        }

        Log.d("SENSOR", "Discard capture");

        this.isCaptureSaved = true;
    }
}

