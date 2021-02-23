package com.mobi.mobilitapp.CrashDetection.Acitivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.mobi.mobilitapp.CrashDetection.DifferentTypes.EmergencyTypes;
import com.mobi.mobilitapp.CrashDetection.DifferentTypes.PreferencesTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.ILastLocationSaved;
import com.mobi.mobilitapp.CrashDetection.HandlersAndServices.EmergencyManager;
import com.mobi.mobilitapp.CrashDetection.Entities.LastLocationSaved;
import com.mobi.mobilitapp.CrashDetection.HandlersAndServices.AccelerometerServiceListener;
import com.mobi.mobilitapp.CrashDetection.HandlersAndServices.EmergencyService;
import com.mobi.mobilitapp.R;

public class EmergencyMenuActivity extends Activity {

    private static final String MyTAG = "MyTAG";
    EmergencyManager emergencyManager;
    SharedPreferences prefs;
    Context context;

    ILastLocationSaved lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        emergencyManager = new EmergencyManager();
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        lastLocation = new LastLocationSaved();

        setContentView(R.layout.activity_emergency_menu);
    }

    public void onEmergencyClick(View view) {
        dialogEmergencyAsking();
    }

    public void onFalseAlarmClick(View view) {
        noEmergency();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            noEmergency();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshLastLocationData() {
        lastLocation.refreshData(
                prefs.getString("timeAtLastLocation", "0"),
                prefs.getString("latitudeAtLastLocation", "0"),
                prefs.getString("longitudeAtLastLocation", "0")
        );
    }

    public void dialogEmergencyAsking(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.r_u_sure_to_call))
                .setTitle(getString(R.string.emergency_call))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        startEmergencyActions();
                        Log.i("MyTAG", "Confirmacion Aceptada.");
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noEmergency();
                        Log.i("MyTAG", "Confirmacion Cancelada.");
                        dialog.cancel();
                    }
                }).show();
    }

    public void startEmergencyActions(){
        Log.v(MyTAG, "Enviando mensaje al servidor");
        stopService(new Intent(this, AccelerometerServiceListener.class));
        prefs.edit().putString(PreferencesTypes.switchCrashOnOff.toString(), PreferencesTypes.off.toString()).commit();

        prefs.edit().putString(EmergencyTypes.emergencyType.toString(), EmergencyTypes.EmergencyButton.toString()).commit();

        startService(new Intent(this, EmergencyService.class));


        Log.v(MyTAG, "Boton Emergencia Presionado");
    }

    public void noEmergency(){
        super.onBackPressed();
        stopService(new Intent(this, AccelerometerServiceListener.class));

        prefs.edit().putString(EmergencyTypes.emergencyType.toString(), EmergencyTypes.NoEmergency.toString()).commit();

        startService(new Intent(this, EmergencyService.class));

        if(prefs.getString(PreferencesTypes.switchCrashOnOff.toString(), PreferencesTypes.no_selection.toString())
                .equalsIgnoreCase(PreferencesTypes.on.toString())){
            startService(new Intent(this, AccelerometerServiceListener.class));
        }
    }
}
