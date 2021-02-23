package com.mobi.mobilitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.mobi.mobilitapp.Uploading.UploadService;

import java.io.File;
import java.io.IOException;

import static com.mobi.mobilitapp.Uploading.UploadService.FILEPATH;

/**
 * Created by Mahmoud Elbayoumy on 10/30/2017.
 */

/* Auto-upload files in case of Wifi is connected */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        uploadOnWifiConnected(context);
        /*
        ConnectivityManager conMan = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && isOnline()) {
            Log.d("BAYO WifiReceiver", "Have Wifi Connection");

            /* check if there are Files to upload *//*
            File dir = new File(FILEPATH);
            File[] files = dir.listFiles();
            Log.d("BAYO WifiReceiver", "Number of files: " + files.length);

            if(files.length > 2) {
                //Log.d("BAYO WifiReceiver", "Should upload");
                Intent ntnt = new Intent(context, UploadService.class);
                // Put some data for use by the IntentService
                ntnt.putExtra("UP", "Uploading...");
                context.startService(ntnt);
            }
        }
        //else
            //Log.d("BAYO WifiReceiver", "Don't have Wifi Connection");*/

    }

    // ICMP
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public void uploadOnWifiConnected(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && isOnline()) {
            Log.d("BAYO WifiReceiver", "Have Wifi Connection");

            /* check if there are Files to upload */
            File dir = new File(FILEPATH);
            File[] files = dir.listFiles();

            if (files != null){
                Log.d("BAYO WifiReceiver", "Number of files: " + files.length);
                if (files.length > 5) {
                    //Log.d("BAYO WifiReceiver", "Should upload");
                    Intent ntnt = new Intent(context, UploadService.class);
                    // Put some data for use by the IntentService
                    ntnt.putExtra("UP", "Uploading...");
                    context.startService(ntnt);
                }
            }
        }
    }
}
