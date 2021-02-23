package com.mobi.mobilitapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;

/**
 * Created by Gerard Marrugat on 19/03/2016.
 */
public class SelectTransportFragment extends DialogFragment {
    String[] means_of_transportation;
    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";

    public static int ADD_ACTIVITY_ID = 3; //change it if the order of the AddActivity MenuItem changed in the menu
    String [] mtransports = { "Bicycle", "Bus", "Car", "Metro", "Motorbike", "Run", "Stationary", "Train", "Tram", "Walk"};

    //String [] mtransports = { "Walk","Run","Bicycle","Motorbike","Car","Bus","Metro","Train","Tram","Stationary"};

    String MyTAG = "MyTAG";
    public static String selection;
    Activity activity;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = this.getActivity();
        selection = null;
        means_of_transportation = getResources().getStringArray(R.array.transports);

        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int captureID = sharedPref.getInt("captureID", 1);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("captureIDCurrent", captureID);
        editor.commit();

        // Creates a directory for save the transport mean
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/TransportSelected");
        if (!dir.exists()) {
            dir.mkdirs();
            //publishProgress("Directory created");
        } else {
            //publishProgress("Directory already exists");
        }

//        final TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        //builder.setTitle("Device ID: "+tm.getDeviceId() + "\nSelect activity:")
        builder.setTitle("Device ID: "+ ANDROID_ID + "\n" + getString(R.string.select_activity) + ":")
                .setSingleChoiceItems(means_of_transportation, -1, new DialogInterface.OnClickListener() {
                    @Override
                    //Save the tranport selected in a static variable because when the file will be created we will need to access to this value
                    public void onClick(DialogInterface dialog, int which) {
                        selection = mtransports[which];
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        /* Cell part */
                        //MainActivity.cellularStatusChecker.destroy(); included in wifi part

                        /* BT part */
                        MainActivity.btStatusChecker.destroy();

                        /* WiFi part */
                        MainActivity.wifiStatusChecker.destroy();

                        /* AR part */
                       // ActivityRecognizedService activityRecognizedService = new ActivityRecognizedService();
                        MainActivity.activityRecognizedImplementation.destroy();

                        /* GPS part */
                        //Log.d("BAYO STF", "set Before:" + locationFinderEnabled);
                        MainActivity.locationFinder.destroyLocationFinder();
                        return;
                    }
                })
                .setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(selection != null){
                            int captureID = sharedPref.getInt("captureID", 1);

                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putInt("captureIDCurrent", captureID);

                            MainActivity.sensorLoger.startNewCapture(captureID, selection);
                            Toast.makeText(activity, selection, Toast.LENGTH_SHORT).show();

                            captureID++;
                            //SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("captureID", captureID);
                            //        editor.putInt("captureID", captureID);
                            editor.commit();

                            /* Cell part */
                            //MainActivity.wifiStatusChecker.create(); //included in wifi part

                            /* BT part */
                            MainActivity.btStatusChecker.create();

                            /* WiFi part */
                            MainActivity.wifiStatusChecker.create();

                            /* AR part */
                            MainActivity.activityRecognizedImplementation.create();

                            /* GPS part */
                            if (MainActivity.locationFinder.locationManager == null && MainActivity.locationFinder.locationListener == null) {

                                //LocationFinder locationFinder = new LocationFinder(getActivity());
                                MainActivity.locationFinder.createLocationFinder();
                                Log.d("BAYO STF", "I created new GPS");
                            }

                            try {
                                MainActivity.menu.getItem(ADD_ACTIVITY_ID).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_stop_capture));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            MainActivity.capturing = true;

                        }
                        else{

                            //Show dialog with error Toast for user to choose an activity
                            builder.show();
                            Toast.makeText(activity,"No activity was selected! Please select the activity you are doing",Toast.LENGTH_LONG).show();
                           // SelectTransportFragment.show(getSupportFragmentManager(), "select_transport");
                        }
                    }
                });

        return builder.create();
    }
}
