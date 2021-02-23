package com.mobi.mobilitapp.Capture;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;


/**
 * Created by Mahmoud Elbayoumy on 10/27/2017.
 */

public class WifiStatusChecker {

    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    WifiManager wifi;
    long timestamp;
    //ConnectivityManager cm;
    //NetworkInfo activeNetwork;
    Context context;


    public static List<WifiSample> wifiData;
    public static String statusToCompare;

    /* Telephony part */
    TelephonyManager telephonyManager;
    public static List<CellSample> cellData;
    public static String statusToCompareCell;

    public WifiStatusChecker(Context context){
        this.context = context;
        wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        /* telephony part */
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void create() {
        wifiData = new ArrayList<WifiSample>();
        statusToCompare = "";

        /* Telephony part */
        cellData = new ArrayList<CellSample>();
        statusToCompareCell = "";

        mHandler = new Handler();
        mStatusChecker.run();
    }

    public void destroy() {
        if(mHandler != null)
            mHandler.removeCallbacks(mStatusChecker);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //updateStatus(); //this function can change value of mInterval.
                //Check WiFi status

      //          activeNetwork = cm.getActiveNetworkInfo();
        //        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                timestamp = new Date().getTime();
                WifiInfo status = wifi.getConnectionInfo();

                if(statusToCompare.equals(status.toString())){
  //                    Log.d("BAYO Wifi", "NOT ADDED " + status.toString());
                }else {
                    wifiData.add(new WifiSample(timestamp,status.getSSID(), status.getBSSID(), status.getRssi(), status.getNetworkId()));
//                    Log.d("BAYO Wifi", "ADDED " + status.toString());
                }
                statusToCompare = status.toString();

                /* Telephony part */
                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                    GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
                    if (location != null) {
                        String mccmnc = "";
                        if(telephonyManager.getNetworkOperator() != null) mccmnc = telephonyManager.getNetworkOperator();

                        if(statusToCompareCell.equals(location.toString())){
                          //  Log.d("BAYO Cell", "NOT ADDED LAC: " + location.getLac() + " CID: " + location.getCid() + " MCCMC: " + mccmnc);
                        }else {
                            cellData.add(new CellSample(timestamp,telephonyManager.getNetworkOperator(),location.getLac(),location.getCid()));
                      //      Log.d("BAYO Cell", "ADDED LAC: " + location.getLac() + " CID: " + location.getCid() + " MCCMC: " + mccmnc);
                        }
                        statusToCompareCell = location.toString();
                    }else{
                        //Log.d("BAYO Cell", "ADDED LAC: Not retrieved");

                    }
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };


    public void writeToFile(int captureID, String activityType){
//header: timestamp,ssid,bssid,rssi,networkId
        //final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //String FILENAME_FORMAT = tm.getDeviceId()
        String FILENAME_FORMAT = ANDROID_ID
                + "_" + String.format("%04d", captureID)
                + "_" + activityType
                + "_%s" // sensor type
                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                + ".csv";

        String filename = String.format(FILENAME_FORMAT, "WIFI");
        CSVFile csvFile = new CSVFile(FILE_STORE_DIR, filename);
        csvFile.createWifiDataFile(wifiData);
    }

    public void writeToFileCell(int captureID, String activityType){
//header: timestamp,ssid,bssid,rssi,networkId
        //final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //String FILENAME_FORMAT = tm.getDeviceId()
        String FILENAME_FORMAT = ANDROID_ID
                + "_" + String.format("%04d", captureID)
                + "_" + activityType
                + "_%s" // sensor type
                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                + ".csv";

        String filename = String.format(FILENAME_FORMAT, "CELL");
        CSVFile csvFile = new CSVFile(FILE_STORE_DIR, filename);
        csvFile.createCellDataFile(cellData);
    }

}
