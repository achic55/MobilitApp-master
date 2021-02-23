package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.mobi.mobilitapp.CrashDetection.Entities.IEmergencyData;
import com.mobi.mobilitapp.SelectTransportFragment;
import com.mobi.mobilitapp.UploadFile;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by toni_ on 04/06/2016.
 */

public class EmergencyDataStorage  extends AsyncTask<IEmergencyData, String, String> {

    private String directory_name;
    public StringBuilder filename = new StringBuilder();

    private final String HeaderEmergency[] = {"time", "latitude", "longitude", "type"};

    private String time;
    private double latitude;
    private double longitude;
    private String type;

    private String date;
    private String transport_selected;

    private File csv_file;

    private static final String MyTAG = "MyTAG";

    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MobilitApp");

    private Context mContext;

    int contadorIntentosDeConexion;

    public EmergencyDataStorage(Context context) {
        mContext = context;
        contadorIntentosDeConexion = 0;
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

    protected String doInBackground(IEmergencyData[] params) {

        date = time(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss");

        transport_selected = SelectTransportFragment.selection;

        filename.append("ecall_" + transport_selected + "_" + date + ".csv");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        directory_name = dir.toString();
        csv_file = new File(directory_name, filename.toString());

        try {
            csv_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv_file));

            Log.v(MyTAG, "Writing on emergency file");

            writer.writeNext(HeaderEmergency);

            time = params[0].getTime();
            latitude = params[0].getLatitude();
            longitude = params[0].getLongitude();
            type = params[0].getType();

            writer.writeNext(new String[]{time, String.valueOf(latitude), String.valueOf(longitude), type});

            writer.close();
            Log.v(MyTAG, "EmergencyData written");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v(MyTAG, "DataStorage_OK");
        UploadFile filetoupload = new UploadFile(csv_file.toString());

        Thread UploadFile = new Thread(filetoupload);

        UploadFile.start();

        // Get the state of the UploadFile thread and when it´s finished then delete the sensor data from the mobile
        while (UploadFile.getState() != Thread.State.TERMINATED) {}

    Log.v("TAG", "Ahora pude borrar los ficheros");
        File delete_file = new File(csv_file.toString());
        Boolean delete = delete_file.delete();

        if (delete) {
            Log.v("TAG", csv_file.toString() + " has been deleted");
        }

        return "Data Uploaded to Mobilitapp Server";

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

}
