package com.example.gerardmarrugat.accelerometer;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;


import android.util.Log;

import android.view.View;

import android.widget.TextView;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import com.opencsv.CSVWriter;

import java.lang.Math;


/**
 * Created by Gerard Marrugat on 20/10/2015.
 */


// DataScreen
// Listen, shows and stores the accelerometer´s data
public class DataScreen_BK extends Activity implements SensorEventListener {

    //Declare attributes of the class which should be visible from each part of the class
    //Intern attributes of each method aren´t visible from other parts of the code inside the class


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    double x, y, z,x_gyro,y_gyro,z_gyro;

    String timestamp;

    File csv_file;

    private long start_time, currentTime;

    private final float THERESLHOLD = (float) 0.4;

    //Specify the object type which will be stored in the ArrayList
    private ArrayList<SensorData> AccelerationData = new ArrayList<SensorData>();

    //Specify the object type which will be stored in the ArrayList
    private ArrayList<SensorData> GyroscopeData = new ArrayList<SensorData>();

    private TextView message_info;

    private static final String MyTAG = "MyTAG";

    private boolean first_sample = true;

    private int position = 0;
    private int position_gyro = 0;



    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //this method emulates a timer

            currentTime = System.currentTimeMillis() - start_time;

            TextView timerText = (TextView) findViewById(R.id.timer_text);
            timerText.setText(time(currentTime, "mm:ss:SSS"));

            //Very Important! this Handler method calls the runnable again
            timerHandler.post(this);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.datascreen);

        message_info = (TextView) findViewById(R.id.message_info);

        //Set timerText to zero
        TextView timerText = (TextView) findViewById(R.id.timer_text);
        timerText.setText("00:00:000");

        // Creates an arraylist where the accelerometer data will be stored
        AccelerationData = new ArrayList<SensorData>();

        // Creates an arraylist where the accelerometer data will be stored
        GyroscopeData = new ArrayList<SensorData>();

        Log.v(MyTAG, "Activity starts");

    }


    //Stops listening the accelerometer
    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);

    }


    //When the accelerometer detects a variation this will be registered and processed
    @Override
    public void onSensorChanged(SensorEvent event) {


        //Match the resources in the app screen with the code variables
        TextView time = (TextView) findViewById(R.id.time);

        TextView x_axis = (TextView) findViewById(R.id.x_axis);

        TextView y_axis = (TextView) findViewById(R.id.y_axis);

        TextView z_axis = (TextView) findViewById(R.id.z_axis);

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

                if (first_sample == true) {


                    timestamp = time(currentTime, "mm:ss:SSS");
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    first_sample = false;
                    position++;

                } else {

                    //We use a low pass filter to remove short-terme fluctuations(Jitter) on our samples
                    timestamp = time(currentTime, "mm:ss:SSS");
                    x = alpha * event.values[0] + (1 - alpha) * AccelerationData.get(position - 1).getX();
                    y = alpha * event.values[1] + (1 - alpha) * AccelerationData.get(position - 1).getY();
                    z = alpha * event.values[2] + (1 - alpha) * AccelerationData.get(position - 1).getZ();

                    position++;
                }


                //When the accelerometer receives new values a SensorData object is created with these values, after that this object will be
                // stored in an array

                SensorData data_accelerometer = new SensorData(timestamp, x, y, z);

                AccelerationData.add(data_accelerometer);

                Log.v(MyTAG, "Collecting Accelerometer´s Data");

                //Only 3 first decimals are stored
                time.append(timestamp + System.getProperty("line.separator"));
                x_axis.append(String.format("%.3f", x) + System.getProperty("line.separator"));
                y_axis.append(String.format("%.3f", y) + System.getProperty("line.separator"));
                z_axis.append(String.format("%.3f", z) + System.getProperty("line.separator"));

            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){

            timestamp = time(currentTime, "mm:ss:SSS");
            x_gyro = event.values[0];
            y_gyro = event.values[1];
            z_gyro = event.values[2];

            //When the accelerometer receives new values a SensorData object is created with these values, after that this object will be
            // stored in an array

            SensorData data_gyro = new SensorData(timestamp, x_gyro, y_gyro, z_gyro);

            GyroscopeData.add(data_gyro);

            Log.v(MyTAG, "Collecting Gyroscope´s Data");

        }
    }


    //Allow using the accelerometer manually, first we did it onCreate() and onResum()
    public void onStartClick(View view) {

        // Instance the accelerometer and gyroscope
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        // registerListener(EventListener, Sensor, rate)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        first_sample = true;
        //Clear text if button start is clicked again
        TextView time = (TextView) findViewById(R.id.time);

        TextView x_axis = (TextView) findViewById(R.id.x_axis);

        TextView y_axis = (TextView) findViewById(R.id.y_axis);

        TextView z_axis = (TextView) findViewById(R.id.z_axis);

        time.setText("");
        x_axis.setText("");
        y_axis.setText("");
        z_axis.setText("");


        // Moment in which the accelerometer is going to store data
        start_time = System.currentTimeMillis();

        //call the run method of timerRunnable
        timerHandler.post(timerRunnable);

    }


    //Stops listening the accelerometer
    public void onStopClick(View view) {

        //Unregister the Listener for all the sensors
        mSensorManager.unregisterListener(this);
        //Stops timerRunnable
        timerHandler.removeCallbacks(timerRunnable);

        new DataStorage().execute(AccelerationData);

        Log.v(MyTAG, "AccelerationData_OK");

        new DataStorage().execute(GyroscopeData);

        Log.v(MyTAG, "GyroscopeData_OK");

    }


    //Exit Button
    //Stops listening the accelerometer in case the user hasn´t clicked stopButton
    public void onExitClick(View view) {

        //Unregister the Listener for all the sensors
        mSensorManager.unregisterListener(this);
        //Stops timerRunnable in case the user hasn´t clicked stopButton
        timerHandler.removeCallbacks(timerRunnable);

        //When we stop the Main activity we send a boolean message with the Intent
        Intent i = new Intent(this, Start.class);
        // MainMessage to know if the activity has started or not
        i.putExtra("MainMessage", false);
        // The activity starts
        startActivity(i);
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



    private class DataStorage extends AsyncTask<ArrayList<SensorData>, String, String> {

        private String directory_name;
        public StringBuilder filename = new StringBuilder();
        private final String Header[] = {"timestamp", "x_acceleration", "y_acceleration", "z_acceleration"};
        private final String Header_gyro[] = {"timestamp", "x_rate", "y_rate", "z_rate"};
        private String timestamp = null;
        private double x = 0.0;
        private double y = 0.0;
        private double z = 0.0;
        private SensorData data,data_gyro;
        private String date;



        @Override
        protected String doInBackground(ArrayList<SensorData>... params) {
            //get the arraylist of accelerometer data and save it in a file

            //Gives a name to the file, date is included in the name´s file
            date = time(System.currentTimeMillis(), "dd-MM-yyyy");
            //Look which kind of data it is;  Acceleration or Gyroscope
            if(params[0].equals(AccelerationData)) {

                filename.append("acceleration_");
                filename.append(date);
                filename.append(".csv");

            }

            if(params[0].equals(GyroscopeData)) {

                filename.append("gyroscope_");
                filename.append(date);
                filename.append(".csv");

            }

            // Creates a directory for acceleration files if it´s not yet created
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Sensor Data/");
            if (!dir.exists()) {
                dir.mkdirs();
                publishProgress("Directory created");
            } else {
                publishProgress("Directory already exists");
            }


            //Creates the file inside the directory folder
            directory_name = dir.toString();
            csv_file = new File(directory_name, filename.toString());

            try {
                csv_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Creates the .csv file and store all the acceleration data or gyroscope data
            try {

                if(params[0].equals(AccelerationData)) {
                    CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

                    writer.writeNext(Header);

                    data = new SensorData(timestamp, x, y, z);

                    int length = params[0].size();

                    for (int i = 0; i < length; i++) {

                        data = params[0].get(i);

                        timestamp = data.getTimestamp();
                        x = data.getX();
                        y = data.getY();
                        z = data.getZ();

                        //Only 3 first decimals are stored
                        writer.writeNext(new String[]{timestamp, String.format("%.3f", x), String.format("%.3f", y), String.format("%.3f", z)});

                    }

                    writer.close();
                    Log.v(MyTAG, "AccelerometerData written");

                }

                if(params[0].equals(GyroscopeData)){
                    CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

                    writer.writeNext(Header_gyro);

                    data_gyro = new SensorData(timestamp, x, y, z);

                    int length = params[0].size();

                    for (int i = 0; i < length; i++) {

                        data_gyro = params[0].get(i);

                        timestamp = data_gyro.getTimestamp();
                        x = data_gyro.getX();
                        y = data_gyro.getY();
                        z = data_gyro.getZ();

                        //Only 3 first decimals are stored
                        writer.writeNext(new String[]{timestamp, String.format("%.3f", x), String.format("%.3f", y), String.format("%.3f", z)});

                    }

                    writer.close();
                    Log.v(MyTAG, "GyroscopeData written");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v(MyTAG, "DataStorage_OK");
            UploadFile filetoupload = new UploadFile(csv_file.toString());

            Thread UploadFile = new Thread(filetoupload);
            UploadFile.start();

            //uploadFile(csv_file.toString());

            return "Data Uploaded to Mobilitapp Server";

        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            message_info.setText(values[0]);

        }


        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            message_info.setText(s);

            TextView date_view = (TextView) findViewById(R.id.date);
            date_view.setText(filename);
        }

    }

}