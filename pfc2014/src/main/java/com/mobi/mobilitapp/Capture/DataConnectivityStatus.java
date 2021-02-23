package com.mobi.mobilitapp.Capture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Mahmoud Elbayoumy on 10/28/2017.
 */

public class DataConnectivityStatus extends BroadcastReceiver {

    //Context context;
    //WifiManager wifi;
/*
    public DataConnectivityStatus(Context context) {
        this.context = context;
        wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }*/


    @Override
    public void onReceive(Context context, Intent intent) {

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

   //     if(info != null && info.isConnected()) {
            // Do your work.

            // e.g. To check the Network Name or other info:

            //String ssid = wifiInfo.getSSID();
            Log.d("BAYO WiFi", wifiInfo.toString());
     //   }else{
            Log.d("BAYO WiFi", "null");
       // }
    }
}
