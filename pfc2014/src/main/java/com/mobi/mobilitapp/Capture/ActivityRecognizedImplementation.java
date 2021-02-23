package com.mobi.mobilitapp.Capture;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;

/**
 * Created by Mahmoud Elbayoumy on 10/26/2017.
 */

public class ActivityRecognizedImplementation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //public GoogleApiClient mApiClient;
    public GoogleApiClient mApiClient;
    PendingIntent pendingIntent;
    Intent intent;

    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";

    Context context;

    //private static int SAMPLING_PERIOD = 15000;
    private static int SAMPLING_PERIOD = 0;

    public ActivityRecognizedImplementation(Context context){
        this.context = context;
        mApiClient = null;
        pendingIntent = null;
        intent = null;
    }
/*
    public GoogleApiClient getClient(){
        if(mApiClient == null) {
            mApiClient = new GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        return mApiClient;
    }
*/
    public void create(){
        if(mApiClient == null) {

            mApiClient = new GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }

        mApiClient.connect();

        Log.d("BAYO AR", "Client Connected: " + mApiClient.toString());
    }

    public void destroy(){
        if(mApiClient == null)
            Log.d("BAYO AR Before", "null");
        else
            Log.d("BAYO AR Before", mApiClient.toString());

        if(mApiClient != null)
            mApiClient.disconnect();
        if(pendingIntent != null)
            pendingIntent.cancel();

        if(mApiClient == null)
        Log.d("BAYO AR After", "null");
        else
        Log.d("BAYO AR After", mApiClient.toString());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        intent = new Intent( context, ActivityRecognizedService.class );
        pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, SAMPLING_PERIOD, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void writeToFile(Context context, int captureID, String activityType){

        //final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


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

        String filename = String.format(FILENAME_FORMAT, "AR");
        CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
        //csv.createSensorDataFile(accelerometerData);
        try {
            csv.open();
            //writeLine("sensor,timestamp,x,y,z");
            for (ARSample p : ActivityRecognizedService.arData) {
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
                    filename = String.format(FILENAME_FORMAT, "AR");
                    csv = new CSVFile(FILE_STORE_DIR, filename);
                    csv.open();
                }else {
                    csv.writeLine(Long.valueOf(p.timestamp)
                            + "," + p.vehicle + "," + p.bicycle + "," + p.foot + "," + p.running
                            + "," + p.still + "," + p.tilting + "," + p.walking + "," + p.unknown);
                }
            }
            csv.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", e.toString());
        }

        /*

        // String FILENAME_FORMAT = tm.getDeviceId()
        String FILENAME_FORMAT = ANDROID_ID
                + "_" + String.format("%04d", captureID)
                + "_" + activityType
                + "_%s" // sensor type
                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                + ".csv";

        String filename = String.format(FILENAME_FORMAT, "AR");
        CSVFile csvFile = new CSVFile(FILE_STORE_DIR, filename);

        csvFile.createARDataFile(ActivityRecognizedService.arData);
        */
    }
}