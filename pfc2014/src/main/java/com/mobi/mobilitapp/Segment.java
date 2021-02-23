package com.mobi.mobilitapp;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Segment {
	private Location _first_location;
	private Location _last_location;
	private double _total_distance;
	private long _total_duration;
	private double _total_average_speed;
	private String _activity, _line;
	private ArrayList<Location> _location_points = new ArrayList<Location>();
    private AudioManager mAudioManager ;
    private boolean mPhoneIsSilent = false;
    SharedPreferences prefes;
    private String _subido;
    Context mcontext;

    TelephonyManager TelephonManager;
    CellSignalStrength      CellSignalStrength;
    myPhoneStateListener pslistener;
    int SignalStrength = 0;


    //empty constructor
    public Segment() {
        super();


    }

	public Segment(Context context) {
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mPhoneIsSilent = false;
        prefes=context.getSharedPreferences("prefs", 0);
        mcontext = context;

      //  pslistener = new myPhoneStateListener();
       // TelephonManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);

       // Toast.makeText(mcontext, "Silence Mode ON"+CellSignalStrength.getDbm(), Toast.LENGTH_SHORT).show();
     //   TelephonManager.listen(pslistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


	}

	//getting First Location
	public Location getFirstLocation() {
		return this._first_location;
	}

	//setting subido
	public void setSubido(String subido) {
		this._subido = subido;
	}

    //getting Subido
    public String getSubido() {
        return this._subido;
    }

    //setting First Location
    public void setFirstLocation(Location first_location) {
        this._first_location = first_location;
    }


    //getting Last Location
	public Location getLastLocation() {
		return this._last_location;
	}

	//setting Last Location
	public void setLastLocation(Location last_location) {
		this._last_location = last_location;
	}

	//getting Total Distance
	public double getTotalDistance() {
		return this._total_distance;
	}

	//setting Total Distance
	public void setTotalDistance(double total_distance) {
		this._total_distance = total_distance;
	}

	//getting Total Duration
	public long getTotalDuration() {
		return this._total_duration;
	}

	//setting Total Duration
	public void setTotalDuration(long total_duration) {
		this._total_duration = total_duration;
	}

	//getting Total Average Speed
	public double getAverageSpeed() {
		return this._total_average_speed;
	}

	//setting Total Average Speed
	public void setTotalAverageSpeed(double total_average_speed) {
		this._total_average_speed = total_average_speed;
	}

	//getting Activity
	public String getActivity() {
		return this._activity;
	}

	//setting Activity
	public void setActivity(String activity) {

        int cellPower =1000;

        //wifipower
        WifiManager wifiManager = (WifiManager)mcontext.getSystemService(Context.WIFI_SERVICE);
        int wifiPower = wifiManager.getConnectionInfo().getRssi();
    //cellpower
        cellPower = prefes.getInt("cellPower",1000);

		this._activity = activity;
        SharedPreferences.Editor editor = prefes.edit();

        if ("vehicle".equals(activity) && prefes.getString("Silence","OFF").equalsIgnoreCase("ON")){
            if (prefes.getInt("estadoAnterior",5)==5) {

                switch (mAudioManager.getRingerMode()) {

                    case 0:
                        editor.putInt("estadoAnterior", 0);
                        editor.commit();
                        break;
                    case 1:
                        editor.putInt("estadoAnterior", 1);
                        editor.commit();
                        break;
                    case 2:
                        editor.putInt("estadoAnterior", 2);
                        editor.commit();
                        break;
                    default:
                        editor.putInt("estadoAnterior", mAudioManager.getRingerMode());
                        editor.commit();
                        break;
                }

            }

            SilentMode();

        }else{

            if (prefes.getInt("estadoAnterior",5)!=5) {

                mAudioManager.setRingerMode(prefes.getInt("estadoAnterior", mAudioManager.getRingerMode()));
                editor.putInt("estadoAnterior", 5);
                editor.commit();

            }else {

                editor.putInt("estadoAnterior", 5);
                editor.commit();

            }
            }

	}



    private void SilentMode() {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }




	//getting line
	public String getLine() {
		return this._line;
	}

	//setting line
	public void setLine(String line) {
		this._line = line;
	}

	//getting Location points
	public ArrayList<Location> getLocationPoints() {
		return this._location_points;
	}

	//updating Location points
	public void updateLocationPoints(ArrayList<Location> location_points) {
		this._location_points.addAll(location_points);
	}

	public void clearLocationPoints() {
		this._location_points.clear();
	}

	//update single Location point
	public void addSingleLocationPoint(Location location) {
		this._location_points.add(location);
	}

	public Location getSingleLocationPoint() {
		return this._location_points.get(0);
	}

    class myPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            SignalStrength = signalStrength.getGsmSignalStrength();
            SignalStrength = (2 * SignalStrength) - 113; // -> dBm
            //Log.v("aki", "cell power changed: "+SignalStrength);
            SharedPreferences.Editor editor = prefes.edit();
            editor.putInt("cellPower",SignalStrength);
            editor.commit();


        }

    }

}

