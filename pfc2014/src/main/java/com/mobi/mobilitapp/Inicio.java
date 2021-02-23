package com.mobi.mobilitapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class Inicio extends Activity {

	public static String ANDROID_ID;
	boolean firstRun;
	private boolean exit = false;
	SharedPreferences settings;
	SharedPreferences.Editor editor;

	String MyTAG = "Inicio";


	/* On response of permissions - Mahmoud Elbayoumy 21/10/2017*/
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		/* Mandatory permissions else the app will close - Mahmoud Elbayoumy 22/10/2017 */
		switch(requestCode)
		{
			case 1:  //Check for permissions grant
				if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					Log.d("BAYO", "Permissions Granted");
					try {
						Intent in = new Intent(this, MainActivity.class);
						startActivity(in);
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
						finish();
					}catch(Exception e){
						e.printStackTrace();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder
                                .setMessage("Unfortunately! The version of your Android does not support MobilitApp.")
                                // If user accepts: execute and carry on
                                .setPositiveButton("Dismiss", (DialogInterface dialog, int which) -> {
                                    Log.d("BAYO", "Android version not supported");
                                    //dialog.cancel();
                                    Inicio.super.finish();
                                });
                        AlertDialog ad = alertDialogBuilder.create();
                        ad.show();
					}
				}
				else
				{
					Log.d("BAYO", "User rejected to give permissions");
                    //dialog.cancel();
                    Inicio.super.finish();
                    //new MainActivity().closeNow();
				}
				break;
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//DEVICE_UNIQUE_ID = getUniquePsuedoID();
		//WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		//MAC_ADDRESS = wifi.getConnectionInfo().getMacAddress();
		if(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) != null)
			ANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
		else
			ANDROID_ID = "UNKNOWN";
		Log.d("BAYO Android_id", ANDROID_ID);
		setContentView(R.layout.inicio);
		settings = getSharedPreferences("prefs", 0);
		editor = settings.edit();

		Activity cntx = this;

		/* 17.03.2017 - ROK & FRAN: Disabling login on first run */
		/* -------------------------------------------------------------------------------------- */
/*		//Looks if the firstRun preference value is true, if it doesn´t exists returns true
		if (settings.getBoolean("firstRun", true))
			//Puts true to the firstRun preference value
			// We need apply method to make changes
			editor.putBoolean("firstRun", true).apply();
			// We asign the value of firstRun preference to firstRun boolean variable
			firstRun = settings.getBoolean("firstRun", true);

		//Put the value "ON" on "Silence" string preference
       if(settings.getString("Silence","").equalsIgnoreCase("")) {
           editor.putString("Silence", "ON");
       }

		if (firstRun) {
			Intent i = new Intent(this, LoginActivity.class);
			//Starts the activity described by the Intent i and waits for the 18 request code
			startActivityForResult(i, 18);
		} else {
			Intent a = new Intent(this, MainActivity.class);
			//Starts the activity
			startActivity(a);
			finish();
		}*/
		/* -------------------------------------------------------------------------------------- */

		/* -------------------------------------------------------------------------------------- */
		/* 17.03.2017 - ROK & FRAN: Added privacy policy pop-up */
		boolean privacyPolicyAccepted = settings.getBoolean("privacyPolicyAccepted", false);

		// Privacy Policy Accepted
		if (privacyPolicyAccepted) {

            /* Ask for Permissions - Mahmoud Elbayoumy 21/10/2017 */

            // If device is running SDK >= 23

            if (Build.VERSION.SDK_INT >= 23) {

				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						//|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
						//!= PackageManager.PERMISSION_GRANTED
                        //|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        //!= PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE)
						!= PackageManager.PERMISSION_GRANTED
						|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_LOGS)
						!= PackageManager.PERMISSION_GRANTED) {

					// ask for permissions
					ActivityCompat.requestPermissions(this, new String[]{
							Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION,
							//Manifest.permission.READ_PHONE_STATE,
                            //Manifest.permission.READ_CONTACTS,
							Manifest.permission.WRITE_EXTERNAL_STORAGE,
							Manifest.permission.READ_EXTERNAL_STORAGE,
							Manifest.permission.ACCESS_NETWORK_STATE,
							Manifest.permission.ACCESS_WIFI_STATE,
							Manifest.permission.BLUETOOTH,
							Manifest.permission.BLUETOOTH_ADMIN,
							Manifest.permission.BODY_SENSORS,
							Manifest.permission.CHANGE_WIFI_STATE,
							Manifest.permission.CHANGE_NETWORK_STATE,
							Manifest.permission.READ_LOGS}, 1);
					Log.d("BAYO", " ASK permissions");
				}
				else {

                    try {
                        // Starts the MainActivity
                        Intent in = new Intent(cntx, MainActivity.class);
                        startActivity(in);
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }catch(Exception e){
                        e.printStackTrace();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder
                                .setMessage("Unfortunately! The version of your Android does not support MobilitApp.")
                                // If user accepts: execute and carry on
                                .setPositiveButton("Dismiss", (DialogInterface dialog, int which) -> {
                                    Log.d("BAYO", "Android version not supported");
                                    //dialog.cancel();
                                    Inicio.super.finish();
                                });
                        AlertDialog ad = alertDialogBuilder.create();
                        ad.show();
                    }
                }
			}
            else {

                // Starts the MainActivity
                Intent in = new Intent(cntx, MainActivity.class);
                startActivity(in);
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
		}
		// Privacy Policy Not Accepted
		else {
			try {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(getString(R.string.privacy_policy))
						.setMessage(R.string.privacy_policy_message)
						// If user accepts: execute and carry on
						.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) -> {
								// Mark as accepted
							editor.putBoolean("privacyPolicyAccepted", true).apply();
							editor.commit();


                    /* Ask for Permissions - Mahmoud Elbayoumy 21/10/2017 */

                            // If device is running SDK >= 23

                            if (Build.VERSION.SDK_INT >= 23) {

                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED
                          //              || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                            //            != PackageManager.PERMISSION_GRANTED
                                       // || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                                       // != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE)
                                        != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_LOGS)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    // ask for permissions
                                    ActivityCompat.requestPermissions(this, new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                              //              Manifest.permission.READ_PHONE_STATE,
                               //             Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.ACCESS_NETWORK_STATE,
                                            Manifest.permission.ACCESS_WIFI_STATE,
                                            Manifest.permission.BLUETOOTH,
                                            Manifest.permission.BLUETOOTH_ADMIN,
                                            Manifest.permission.BODY_SENSORS,
                                            Manifest.permission.CHANGE_WIFI_STATE,
                                            Manifest.permission.CHANGE_NETWORK_STATE,
                                            Manifest.permission.READ_LOGS}, 1);
                                    Log.d("BAYO", " ASK permissions");
                                }
                                else {

                                    // Starts the MainActivity
                                    Intent in = new Intent(cntx, MainActivity.class);
                                    startActivity(in);
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }
                            }
                            else {

                                // Start the MainActivity with Policy Accepted
                                Intent in = new Intent(cntx, MainActivity.class);
                                startActivity(in);
                                //   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
				})
								// If user deny: close the app
						.setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {
							dialog.cancel();
							Inicio.super.finish();
						});

				AlertDialog ad = alertDialogBuilder.create();
				ad.show();
					 // To make the TextView clickable
				((TextView)ad.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

			} catch (Exception e) {
				Log.v(MyTAG, "Error con el dialog");
			}
		}

    }
		/* -------------------------------------------------------------------------------------- */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 18) {
			//-1 equals to RESULT_OK
			if (resultCode == -1) {
				Intent a = new Intent(this, PrefAct.class);
				startActivityForResult(a, 66);
			}else{
				finish();
			}
		}

		else if (requestCode == 66) {
			//0 equals to RESULT_CANCELLED
			if (resultCode == 0) {
				Intent a = new Intent(this, MainActivity.class);
				startActivity(a);
				editor.putBoolean("firstRun", false).commit();
				finish();
			}
		}
	}

	public void onBackPressed() {
		if (exit) {
			finish();
		}
		else {
			Toast.makeText(this, R.string.exit_msg, Toast.LENGTH_LONG).show();
			exit = true;
			new Handler().postDelayed(() -> {
					exit = false;
			}, 5 * 1000);
		}
	}


	/**
	 * Return pseudo unique ID
	 * @return ID
	 */
	public static String getUniquePsuedoID() {
		// If all else fails, if the user does have lower than API 9 (lower
		// than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
		// returns 'null', then simply the ID returned will be solely based
		// off their Android device information. This is where the collisions
		// can happen.
		// Thanks http://www.pocketmagic.net/?p=1662!
		// Try not to use DISPLAY, HOST or ID - these items could change.
		// If there are collisions, there will be overlapping data
		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

		// Thanks to @Roman SL!
		// https://stackoverflow.com/a/4789483/950427
		// Only devices with API >= 9 have android.os.Build.SERIAL
		// http://developer.android.com/reference/android/os/Build.html#SERIAL
		// If a user upgrades software or roots their device, there will be a duplicate entry
		String serial = null;
		try {
			serial = android.os.Build.class.getField("SERIAL").get(null).toString();

			// Go ahead and return the serial for api => 9
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		} catch (Exception exception) {
			// String needs to be initialized
			serial = "serial"; // some value
		}

		// Thanks @Joe!
		// https://stackoverflow.com/a/2853253/950427
		// Finally, combine the values we have found by using the UUID class to create a unique identifier
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}

}