package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.mobi.mobilitapp.CrashDetection.DifferentTypes.EmergencyTypes;
import com.mobi.mobilitapp.CrashDetection.DifferentTypes.PreferencesTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.ILastLocationSaved;
import com.mobi.mobilitapp.CrashDetection.Entities.LastLocationSaved;
import com.mobi.mobilitapp.R;

/**
 * Created by toni_ on 05/06/2016.
 */
public class EmergencyService extends Service {

    private static final String MyTAG = "MyTAG";

    ILastLocationSaved lastLocation;
    SharedPreferences prefs;
    String typeEmergency;
    String switchSmsEmergency;
    String on = PreferencesTypes.on.toString();

    @Override
    public void onCreate() {
        super.onCreate();
        lastLocation = new LastLocationSaved();

        loadSharedPreferences();
        refreshLastLocationData();
        sendEmergencySMS();

        if(hasActiveInternetConnection() ==true){

            sendEmergencyMessageToTheServer();
            Toast.makeText(this,"Message sent to the server", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Connection failure", Toast.LENGTH_SHORT).show();
        }

        stopSelf();
    }

    private void loadSharedPreferences() {
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        typeEmergency = prefs.getString(EmergencyTypes.emergencyType.toString(), EmergencyTypes.no_type.toString());
        refreshUserPreferences();
    }

    private void refreshUserPreferences() {
        switchSmsEmergency = prefs.getString(PreferencesTypes.switchSmsOnOff.toString(),PreferencesTypes.no_selection.toString());
    }

    private void refreshLastLocationData() {
        lastLocation.refreshData(
                prefs.getString(PreferencesTypes.timeAtLastLocation.toString(), "0"),
                prefs.getString(PreferencesTypes.latitudeAtLastLocation.toString(), "0"),
                prefs.getString(PreferencesTypes.longitudeAtLastLocation.toString(), "0")
        );
    }

    private void sendEmergencyMessageToTheServer(){
        SendEmergencyMessage.sendMessageToTheServer(
                lastLocation.getTimeAtLastLocation(),
                lastLocation.getLatitudeAtLastLocation(),
                lastLocation.getLongitudeAtLastLocation(),
                typeEmergency,
                this
        );
        Log.v(MyTAG, "Message sent to the server");
    }

    private void sendEmergencySMS(){
//        refreshUserPreferences();
//        if(switchSmsEmergency.equalsIgnoreCase(on)){
//            SendEmergencyMessage.sendSMS(
//                    lastLocation.getLatitudeAtLastLocation(),
//                    lastLocation.getLongitudeAtLastLocation(),
//                    typeEmergency,
//                    this,
//                    prefs.getString(PreferencesTypes.emergencyPhoneNumberSms.toString(), PreferencesTypes.no_phone.toString()),
//                    prefs.getString(PreferencesTypes.medical_info.toString(), "")
//            );
//        }
    }

    public boolean hasActiveInternetConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

