package com.mobi.mobilitapp;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.mobi.mobilitapp.SelectTransportFragment;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Gerard Marrugat on 28/02/2016.
 */
public class DataStorage extends AsyncTask<ArrayList<SensorData>, String, String> {

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
    private String transport_selected;

    private File csv_file;

    private SensorDataList sensorDataList;

    private static final String MyTAG = "MyTAG";

    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MobilitApp");

    private Context mContext;

    public DataStorage(Context context){
        mContext = context;
    }


    @Override
    protected String doInBackground(ArrayList<SensorData>... params) {
        //get the arraylist of accelerometer data and save it in a file

        //Gives a name to the file, date is included in the name´s file
        date = time(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss");

        //Look which kind of data it is;  Acceleration or Gyroscope

        sensorDataList = (SensorDataList) params[0];

        //at this point the selected transport at the beginning of the service is retrieved and used in the file´s name

       transport_selected = SelectTransportFragment.selection;


        if(sensorDataList.getSensorName().equals("AccelerationData")){

            filename.append("acceleration_"+transport_selected+"_"+date+".csv");

        }

        if(sensorDataList.getSensorName().equals("GyroscopeData")) {

            filename.append("gyroscope_"+transport_selected+"_"+date+".csv");

        }

        // Creates a directory for acceleration files if it´s not yet created

        if (!dir.exists()) {
            dir.mkdirs();
            //publishProgress("Directory created");
        } else {
            //publishProgress("Directory already exists");
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

            if(sensorDataList.getSensorName().equals("AccelerationData")) {
                CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

                Log.v(MyTAG, "Writing on accelerometer file");

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
                    //x,y and z will be stored as strings and not as doubles
                    writer.writeNext(new String[]{timestamp, String.format("%.3f", x), String.format("%.3f", y), String.format("%.3f", z)});

                }

                writer.close();
                Log.v(MyTAG, "AccelerometerData written");

            }

            if(sensorDataList.getSensorName().equals("GyroscopeData")){
                CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

                Log.v(MyTAG, "Writing on gyroscope file");

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

            /*else if(sensorDataList.getSensorName().equals("EmergencyData")){
                CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

                Log.v(MyTAG, "Writing on emergency file");

                writer.writeNext(HeaderEmergency);

                timestamp = params[0].toString();
                x = Double.parseDouble(params[1].toString());
                y = Double.parseDouble(params[2].toString());
                z=1;

                //timestamp = "emergency";
                //x = 1;
                //y=2;
                //z=3;

                data_gyro = new SensorData(timestamp, x, y, z);

                writer.writeNext(new String[]{timestamp, String.format("%.3f", x), String.format("%.3f", y), String.format("%.3f", z)});

                writer.close();
                Log.v(MyTAG, "EmergencyData written");
            }/*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(MyTAG, "DataStorage_OK");
        UploadFile filetoupload = new UploadFile(csv_file.toString());

        Thread UploadFile = new Thread(filetoupload);

        UploadFile.start();


        // Get the state of the UploadFile thread and when it´s finished then delete the sensor data from the mobile
        while(UploadFile.getState() != Thread.State.TERMINATED){

        }

        Log.v("TAG","Ahora pude borrar los ficheros");
        //Delete sensor files
        File delete_file = new File(csv_file.toString());
        Boolean delete = delete_file.delete();

        if (delete) {
            Log.v("TAG", csv_file.toString() + " has been deleted");

        }
        //uploadFile(csv_file.toString());

        return "Data Uploaded to Mobilitapp Server";

    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        //message_info.setText(values[0]);

    }


    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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


}




