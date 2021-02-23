package com.mobi.mobilitapp.Capture;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;

/**
 * Created by Mahmoud Elbayoumy on 10/28/2017.
 */

public class BTStatusChecker {

    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";
    Context context;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothProfile.ServiceListener mProfileListener;
    IntentFilter filter;
    List<String> btData;
    boolean receiverReg = false;
   // SharedPreferences btSharedPreference;

    public BTStatusChecker(Context context){
        this.context = context;
        //btSharedPreference = context.getSharedPreferences("", Context.MODE_PRIVATE);
    }

    public void create(){

        btData = new ArrayList<String>();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
       // Log.d("BAYO before del", loadArrayFlat());
        deleteArray(); //to start brand new preferences
       // Log.d("BAYO after del", loadArrayFlat());
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Log.d("BAYO BLU Status", Integer.toString(mBluetoothAdapter.getState()));

        //Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //if(pairedDevices.size() > 0)
          //  for(BluetoothDevice bt : pairedDevices) {
            //    Log.d("BAYO BLU Bonded", bt.toString());
            //}
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        //filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(mReceiver, filter);
        receiverReg=true;
    }

    public void destroy(){
        if(receiverReg) {
            context.unregisterReceiver(mReceiver);
            receiverReg = false;
        }
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
           //Device is now connected
                updateArrayElement("CON", device.getAddress().toString());
                btData.add(loadArrayFlat());
  //              Log.d("BAYO BLU CONNECTED", device.toString());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
           //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
           //Device is about to disconnect
                //Log.d("BAYO BLU DISCONNECT REQ", device.toString());
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
           //Device has disconnected
                updateArrayElement("DIS", device.getAddress().toString());
                btData.add(loadArrayFlat());
//                Log.d("BAYO BLU DISCONNECTED", device.toString());
            }
        }
    };


    public void writeToFile(int captureID, String activityType){
//header: timestamp,device,status....
        //final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //String FILENAME_FORMAT = tm.getDeviceId()
        String FILENAME_FORMAT = ANDROID_ID
                + "_" + String.format("%04d", captureID)
                + "_" + activityType
                + "_%s" // sensor type
                + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                + ".csv";

        String filename = String.format(FILENAME_FORMAT, "BT");
        CSVFile csvFile = new CSVFile(FILE_STORE_DIR, filename);
        try {
           csvFile.open();
            for (String p : btData)
                csvFile.writeLine(p);
            csvFile.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", "BT : " + e.toString());
        }
    }


    public boolean saveArray(List<String> sKey, List<String> dKey)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
    /* sKey is an array */
        mEdit1.putInt("array_size", sKey.size());

        for(int i=0;i<sKey.size();i++)
        {
            mEdit1.remove("Device_" + i);
            mEdit1.putString("Device_" + i, dKey.get(i));
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, sKey.get(i));
        }

        return mEdit1.commit();
    }

    public List<String> loadSArray()
    {
        List<String> sKey = new ArrayList<String>();

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(context);
        sKey.clear();

        int size = mSharedPreference1.getInt("array_size", 0);

        for(int i=0;i<size;i++)
        {
            sKey.add(mSharedPreference1.getString("Status_" + i, null));
        }
        return sKey;

    }

    public List<String> loadDArray()
    {
        List<String> dKey = new ArrayList<String>();

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(context);
        dKey.clear();

        int size = mSharedPreference1.getInt("array_size", 0);

        for(int i=0;i<size;i++)
        {
            dKey.add(mSharedPreference1.getString("Device_" + i, null));
        }
        return dKey;

    }


    public boolean deleteArray()
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = mSharedPreference1.edit();
        int size = mSharedPreference1.getInt("array_size", 0);


        for(int i=0;i<size;i++)
        {
            mEdit1.remove("Device_" + i);
            mEdit1.remove("Status_" + i);
        }

        mEdit1.putInt("array_size", 0);
        return mEdit1.commit();
    }


    public String loadArrayFlat()
    {
        long timestamp = new Date().getTime();
        String flatArray = Long.toString(timestamp);

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(context);

        int size = mSharedPreference1.getInt("array_size", 0);

        int secureCount = 10; //maximum devices to load 10 if not connected

        for(int i=0;i<size;i++) {
            if(mSharedPreference1.getString("Status_" + i, null) != null) {
                if (mSharedPreference1.getString("Status_" + i, null).equals("CON")) {
                    flatArray += "," + mSharedPreference1.getString("Device_" + i, null) + "," + mSharedPreference1.getString("Status_" + i, null);
                    secureCount -= 1;
                }
            }
        }

        if(secureCount > 0){
            for(int i=0;i<size;i++) {
                if(mSharedPreference1.getString("Status_" + i, null) != null) {
                    if (mSharedPreference1.getString("Status_" + i, null).equals("DIS")) {
                        flatArray += "," + mSharedPreference1.getString("Device_" + i, null) + "," + mSharedPreference1.getString("Status_" + i, null);
                        secureCount -= 1;
                    }
                }
            }
        }

        return flatArray;

    }

    public boolean updateArrayElement(String status, String device)
    {
        boolean updated = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int size = sp.getInt("array_size", 0);
        SharedPreferences.Editor mEdit1 = sp.edit();

     //   Log.d("BAYO check Device", device + "," + status);
        for(int i=0;i<size;i++)
        {
         //   Log.d("BAYO check", sp.getString("Device_" + i, null) + "," + sp.getString("Status_" + i, null));
            if(device.equals(sp.getString("Device_" + i, null))) {
                mEdit1.remove("Device_" + i);
                mEdit1.putString("Device_" + i, device);
                mEdit1.remove("Status_" + i);
                mEdit1.putString("Status_" + i, status);
                updated = true;
            }
        }

        if(!updated) {
            mEdit1.putString("Device_" + size, device);
            mEdit1.putString("Status_" + size, status);
            mEdit1.putInt("array_size", size+1);
        }

        return mEdit1.commit();
    }
}
