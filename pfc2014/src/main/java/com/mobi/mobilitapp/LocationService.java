package com.mobi.mobilitapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import org.joda.time.LocalTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;




public class LocationService extends Service {

	InputStream is=null;
	String result=null;
	int code;
    TelephonyManager TelephonManager;
    myPhoneStateListener pslistener;
    int SignalStrength = 0;

	private ArrayList<String> lastActivitiesArrayList = new ArrayList<String>();
	private ArrayList<Location> tempLocationArrayList = new ArrayList<Location>();
	private ArrayList<Location> arraymetro = new ArrayList<Location>();
	private List<Location> subarray = new ArrayList<Location>();
	private ArrayList<Segment> segmentArrayList = new ArrayList<Segment>();
	private ArrayList<LatLng> arrayrenfe;
	private ArrayList<String> arrayrenfeline;
	private ArrayList<String> twopositionarray;

	private final String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			.getAbsolutePath() + "/MobilitApp/";

	JSONArray arrayjson;
	ArrayList<String> al, al2, r;
	Multimap<String, Integer> lmm;
	String[] linea;
	HashMap<String, Integer> hmids;

	private final String API = "AIzaSyANN1DrfCliED-DrJr52pMMZqKwZXZpQuY";
	//private final String API = "AIzaSyANN1DrfCliED-DrJr52pMMZqKwZXZpQuY";//signed
    //private final String API = "AIzaSyBEfMGKyNLvPCEQuheqrc_scAvvOZ622XI";//not signed


	private String orig = null, dest = null;
	float distance_metro = 0;
	public Location location, loc1, loc2;
	public ArrayList<Location> locgood = new ArrayList<Location>();
	private Segment segment;

	JSONArray segmentsData = null;
	String json_file;
	public Location tmpLastLocation;
	public Location goodloc = null;
	private long segment_total_duration = 0, temp_duration = 0, prev_duration = 0;
	private double total_segment_delta_distance = 0, average_segment_speed = 0, segment_delta_distance = 0,
			temp_distance = 0, temp_speed = 0;
	String activityName = "";
	int size = 0;
	public int i = 0;
	int vehicle_count = 0, bicycle_count = 0, on_foot_count = 0, still_count = 0, unknown_count = 0;
	int badloc = 0;
	Location tempPlace, lastl = null;
	boolean afterbad = false;
	String line = null;
	boolean write = true;
	boolean avoid = false;
	boolean falsepositive = false;
	boolean reverse = false;
	boolean doonce = true;
	boolean depart = true;
        boolean upload_diffday = false;
	int transb = 0;
	SharedPreferences peso, calo, co2, prefsGoogle, prefsFace, prefs;
	WifiManager wifi;
	File f;
	Segment tempSegment;
	FileInputStream fis;
	long elapsedtime;
	String lastday = null;
	String lastupload = null;
	Notification noti;
	NotificationManager nm;
	int bus = 0;
	Location bus_orig;
	int tam = 0;
	boolean unavez = true;
	int pos2rev = -1;
	boolean block = false;
	boolean metrodetect = false;
	boolean gpsON = false;
	int reploc = 0;
	int cada2min = 0;
	LocationManager lm;
	boolean hasGPS;
	private double block_distance = 0.0;
	double dist = 0.0;
	Handler h;
	private String id;
	
	private AudioManager mAudioManager ;

	private boolean mPhoneIsSilent = false;

	private String transport;

//	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

	String MyTAG = "MyTAG";

	//public Data_Receiver mReceiver;

	Runnable runn = new Runnable() {

		@Override
		public void run() {
			//uploadJSON(ruta + lastupload + "location_segment.json");
			uploadJSONDB(ruta + lastupload + "location_segment.json");

            Log.v("aki_"," lastup_onCall: "+lastupload);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {


        pslistener = new myPhoneStateListener();
        TelephonManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonManager.listen(pslistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		
		prefsGoogle = getSharedPreferences("google_login", Context.MODE_PRIVATE);
		prefsFace = getSharedPreferences("face_login", Context.MODE_PRIVATE);
		prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
		peso = getSharedPreferences("com.mobi.mobilitapp_preferences", Context.MODE_PRIVATE);
		calo = getSharedPreferences("data_calories", Context.MODE_PRIVATE);
		co2 = getSharedPreferences("data_co2", Context.MODE_PRIVATE);
		
		
		
		lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		h = new Handler();

		f = new File(ruta + AppUtils.getDay(System.currentTimeMillis()) + "location_segment.json");

		
		if (f.exists() && segmentArrayList.isEmpty()) {



			recoverArray(f, AppUtils.getDay2(System.currentTimeMillis()));

		}

		wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		hasGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

		lastday = prefs.getString("lastday", "0");
		lastupload = prefs.getString("lastupload", "0");

        Log.v("aki_"," lastup_on create: "+lastupload);

		if (lastday.equalsIgnoreCase("0")) {
			lastday = null;
		} else if (!lastday.equalsIgnoreCase(AppUtils.getDay(System.currentTimeMillis()))) { // estamos en un dia diferente al lastday
			upload_diffday = true;
		} else {
			upload_diffday = false;
		}

		if (lastupload.equalsIgnoreCase("0")) {

            lastupload = lastday;
		}

		if (prefs.getString("id", "no_id").equalsIgnoreCase("no_id")) {
			id = hash(getSessionID());
			prefs.edit().putString("id", id).commit();
			
			
		} else {
			id = prefs.getString("id", "no_id");
		}

		Log.v("aki_", " lastup_finaloncreate: " + lastupload);


        registerReceiver(activityReceiver, new IntentFilter("activityRecognition"));


	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

		try {
		locationChanged(location);
		}catch (Exception e){
			e.printStackTrace();
		}

		return START_NOT_STICKY;
	}

	public void writeSegment(String _filename, ArrayList<Segment> _segmentList) {
		try {

          /*  TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
            int cellPower = cellSignalStrengthGsm.getDbm();
    */

			int cellPower = prefs.getInt("cellPower",0);
			WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
			int wifiPower = wifiManager.getConnectionInfo().getRssi();

			Segment segment = new Segment(this);
            
			// create the segments array
			JSONArray segments = new JSONArray();

			for (int k = 0; k < _segmentList.size(); k++) {
				// create the objects that will be in each spot of the segment
				// array
				JSONObject single_segment = new JSONObject();
               // single_segment.put("cellPower", cellPower);
                //single_segment.put("wifiPower", wifiPower);
				single_segment.put("activity", _segmentList.get(k).getActivity());
				single_segment.put("distance (m)", _segmentList.get(k).getTotalDistance());
				single_segment.put("duration (s)", _segmentList.get(k).getTotalDuration());

				if (_segmentList.get(k).getActivity().equalsIgnoreCase("metro"))
					single_segment.put("line", _segmentList.get(k).getLine());

				single_segment.put("speed (Km/h)", _segmentList.get(k).getAverageSpeed());
				single_segment.put("first time", AppUtils.getTime(_segmentList.get(k).getFirstLocation().getTime()));
				single_segment.put("last time", AppUtils.getTime(_segmentList.get(k).getLastLocation().getTime()));

				// create an array inside the object for each spot of the
				// segment array
				JSONArray location_points = new JSONArray();


				for (int j = 0; j < _segmentList.get(k).getLocationPoints().size(); j++) {
                    location_points.put(_segmentList.get(k).getLocationPoints().get(j).getLatitude());
                    location_points.put(_segmentList.get(k).getLocationPoints().get(j).getLongitude());
                    location_points.put(AppUtils.getTime(_segmentList.get(k).getLocationPoints().get(j).getTime()));


                    //   if ((j == _segmentList.get(k).getLocationPoints().size()-1) && (k == _segmentList.size() -1)){
                    //     location_points.put(Integer.toString(cellPower)+"/"+ Integer.toString(wifiPower));
                    //   Log.v("aki","ultima location de ultimo segmento");
                    //}else{
                    location_points.put(_segmentList.get(k).getLocationPoints().get(j).getProvider());
                    //    Log.v("aki","otra location, no modifica power");
                //}


				}
				single_segment.put("location", location_points);

				segments.put(k, single_segment);
			}
			JSONObject json = new JSONObject();
			json.put("segments", segments);
			try {

				File directory = new File(ruta);
				if (!directory.exists()) {
					directory.mkdir();
				}
				FileWriter fw = new FileWriter(_filename, false);
				fw.append(json.toString(1));
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			activityProcessing(intent.getStringExtra("activity"));
		}
	};

	private void activityProcessing(String _activity) {

		switch (_activity) {

		case "vehicle":
			vehicle_count++;
			break;
		case "bicycle":
			bicycle_count++;
			break;
		case "on_foot":
			on_foot_count++;
			break;
		case "still":
			still_count++;
			break;
		case "unknown":
			unknown_count++;
			break;
		}
	}

	private String activityEstimation(int _isOnfoot, int _isBicycle, int _isStill, int _isVehicle, int _isUnknown) {

		ArrayList<Integer> max = new ArrayList<Integer>(5);

		max = findMax(_isOnfoot, _isBicycle, _isStill, _isVehicle, _isUnknown);

		switch (max.size()) {

		case 1:
			if (max.get(0) == 3) // si el máximo es vehicle devolvemos tal cuál
				return (toActivity(max.get(0)));

			else {
			if ((max.get(0) == 1)&&(_isBicycle - _isVehicle) > 3) { // si el máximo es distinto de vehicle y la diferencia
															// entre ellos es mayor que tres devolvemos el valor máximo devuelto
				return toActivity(1);
				}
			else if ((max.get(0) == 0)&&(_isOnfoot - _isVehicle) > 3) {
				return toActivity(0);
			}
			else if ((max.get(0) == 2)&&(_isStill - _isVehicle) > 3) {
				return toActivity(2);
			}
			else {	// en caso que la diferencia entre el maximo devuelto y vehicle sea menos que tres devolvemos vehicle
						// priorizamos vehicle
				return toActivity(3);
				}
			}

		case 2:
			if (max.get(0) == 3 || max.get(1) == 3) { // si entre los máximos está vehicle nos quedamos con el.
				return toActivity(3);
			} else if ((max.get(0) == 1 || max.get(1) == 1)&&(_isBicycle - _isVehicle > 3)) { //lo mismo pero para bicycle siempre que
																							// la diferencia con vehicle sea mayor que tres
				return toActivity(1);
			} else if ((max.get(0) == 0 || max.get(1) == 0)&&(_isOnfoot - _isVehicle > 3)) { //lo mismo pero para on_foot
																							// la diferencia con vehicle sea mayor que tres
				return toActivity(0);
			} else if (_isStill - _isVehicle > 3){
				return toActivity(2); // si no es ninguno de los anteriores y la diferencia con vehicle es mayor que tres
										// devuelve still
			} else {
				return toActivity(3); // sino devuelve vehicle
			}
		case 3:
			if (max.get(0) == 3 || max.get(1) == 3 || max.get(2) == 3) {
				return toActivity(3);
			} else if ((max.get(0) == 1 || max.get(1) == 1 || max.get(2) == 1)&&(_isBicycle - _isVehicle > 3)) {
				return toActivity(1);
			} else if (_isOnfoot - _isVehicle > 3){
				return toActivity(0); // si no es ninguno de los anteriores y la diferencia con vehicle es mayor que tres
										// devuelve still
			} else {
				return toActivity(3); // sino devuelve vehicle

			}
		case 4:
			if (max.get(0) == 3 || max.get(1) == 3 || max.get(2) == 3 || max.get(3) == 3) {
				return toActivity(3);
			} else if(_isBicycle - _isVehicle > 3){
				return toActivity(1); // si no es ninguno de los anteriores y la diferencia con vehicle es mayor que tres
				// devuelve still
			} else {
				return toActivity(3); // sino devuelve vehicle
			}
		case 5:
			return toActivity(2); // si son todas iguales(a 0) es cuando no nos movemos porque no envía nada -> still
		}
		return "still";

	}

	private String toActivity(int index) {

		switch (index) {
		case 0:
			return "on_foot";
		case 1:
			return "bicycle";
		case 2:
			return "still";
		case 3:
			return "vehicle";
		case 4:
			return "unknown";
		}
		return "";
	}

	public void locationChanged(Location location) {

        int cellPower = prefs.getInt("cellPower",0);
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        int wifiPower = wifiManager.getConnectionInfo().getRssi();


        location.setProvider(Integer.toString(cellPower)+"/"+Integer.toString(wifiPower));

		String timeAtLastLocation = AppUtils.getTime(location.getTime());
		String latitudeAtLastLocation = Double.toString(location.getLatitude());
		String longitudeAtLastLocation = Double.toString(location.getLongitude());

		prefs.edit().putString("timeAtLastLocation", timeAtLastLocation).commit();
		prefs.edit().putString("latitudeAtLastLocation", latitudeAtLastLocation).commit();
		prefs.edit().putString("longitudeAtLastLocation", longitudeAtLastLocation).commit();

		Log.v("aki",
				"aki_ " + " " + AppUtils.getTime(location.getTime()) + " " + Float.toString(location.getAccuracy()));


		Log.v("aki",
				"aki_ " + " " + Double.toString(location.getLatitude()) + " "
						+ Double.toString(location.getLongitude()));
		
		
		//SilentMode();
		//NormalMode();
		
		
		

       // runn.run();

		
		
	
		//try {
			//resultdb = new SaveDatabase(location.getLatitude(),location.getLongitude(),location.getLatitude(),location.getLongitude(),"prueba").execute().get();
			
			//runn.run(); //Subida al cloud provisional, 
			//Log.v("BBDD","subida al cloud OK");
			
			
		//} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			
		//	Log.v("BBDD_result",resultdb);
			
			
		//} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		//}
		
		
		writeSegment(ruta + AppUtils.getDay(System.currentTimeMillis()) + "location_segment.json",
				segmentArrayList);

	
		
//		postproc();
//////////////////////////////////////////////////////////////////////////
		//First Part: Enabling GPS
		if (gpsON && location.getAccuracy() > 1000) { // underground
			needGPS(false);
			cada2min = 0;
			Log.v("aki", "aki_GPSOFF(metro)");
			gpsON = false;
		}

		if (location.getAccuracy() < 200) {

			tempLocationArrayList.add(location);

			// Checks whether the loc is the same as the previous --> Activate GPS 

			if (!gpsON)
				if (tempLocationArrayList.size() != 1)
					if (tempLocationArrayList.get(tempLocationArrayList.size() - 1).getLatitude() == tempLocationArrayList
							.get(tempLocationArrayList.size() - 2).getLatitude()
							&& tempLocationArrayList.get(tempLocationArrayList.size() - 1).getLongitude() == tempLocationArrayList
									.get(tempLocationArrayList.size() - 2).getLongitude()) {
						reploc++;
						if (reploc > 2) {
							if (vehicle_count > 1 || on_foot_count > 2) {
								needGPS(true);
								cada2min = 0;
								Log.v("aki", "aki_GPSON");
								gpsON = true;
							}
						}
					} else {
						reploc = 0;
					}

			if (!block)
				goodloc = location;

			if (falsepositive) {
				Log.v("aki", "aki_falsepositive");
				badloc = 0;
				avoid = false;
				falsepositive = false;
				write = true;
			}
			if (afterbad) {
				locgood.add(goodloc);
				if (locgood.size() > 1) // checks if after bad locs good ones are received but repeated, if this happens we continue as before

					if (locgood.get(locgood.size() - 1).getLatitude() == locgood.get(locgood.size() - 2).getLatitude()
							&& locgood.get(locgood.size() - 1).getLongitude() == locgood.get(locgood.size() - 2)
									.getLongitude()) {
						locgood.remove(locgood.size() - 1);
					}
				if (locgood.size() > 2) {
					avoid = true;
					loc2 = locgood.get(0);
					badloc = 0;
					afterbad = false;
					tempLocationArrayList.clear();
					for (int index = 0; index < locgood.size(); index++)
						tempLocationArrayList.add(locgood.get(index)); // borro las localizaciones y añado las de despues de metro
					locgood.clear();
					Log.v("aki", "aki_goodloc");
					try {
						new GetPlaces("subway_station").execute(
								new Double[] { loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(),
										loc2.getLongitude() }).get();// 41.431559,2.144991});

					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		} else { // loc > 200

			if (location.getAccuracy() < 1000 && wifi.isWifiEnabled()) //GPS activated when 200<loc<1000
				if (!gpsON) {
					needGPS(true);
					cada2min = 0;
					Log.v("aki", "aki_GPSON");
					gpsON = true;
				}

			if (wifi.isWifiEnabled() && location.getAccuracy() > 1000) // location accuracy > 1000 --> Underground
				badloc++;
			if (badloc > 4 && !afterbad)
				block = true; //asi se evita que la localizacion buena que hace que pasen los 2 min no cuente como goodloc
		}
////////////////////////////////////////////////////////////////////////////////////////////
		size = tempLocationArrayList.size();
		if (size > 1)
			if (((tempLocationArrayList.get(size - 1).getTime() - tempLocationArrayList.get(0).getTime())
					/ AppUtils.MILLISECONDS_PER_SECOND > AppUtils.UPDATE_INTERVAL_SEGMENT_LOCATION_IN_SECONDS)
					|| avoid) {

				cada2min++;

				Log.v("aki", "aki_2min");

				if (gpsON && cada2min == 2) { // desactivamos el gps la segunda
												// vez que pasemos por aquí
					needGPS(false);
					gpsON = false;
					Log.v("aki", "aki_GPSOFF");
					reploc = 0;
				}

				Log.v("aki", "aki_vehicle = " + Integer.toString(vehicle_count));
				Log.v("aki", "aki_bicycle = " + Integer.toString(bicycle_count));
				Log.v("aki", "aki_on_foot = " + Integer.toString(on_foot_count));
				Log.v("aki", "aki_still = " + Integer.toString(still_count));
				Log.v("aki", "aki_unknown = " + Integer.toString(unknown_count));

				////////////////////////////////////////////////////////////////
				// At this point depending on the API results we will call a subapp that collects accelerometer/gyroscope data
				// or won´t

				activityName = activityEstimation(on_foot_count, bicycle_count, still_count, vehicle_count,
						unknown_count);


				Log.v(MyTAG,"At this point we have to call to Sensor_Listener to read data from the Accelerometer&Gyroscope");


				//get which transport has been selected

				String transport_selected = SelectTransportFragment.selection;

				Log.v(MyTAG,"The transport selected has been: " + transport_selected);

				if(activityName != "still" ||  activityName != "on_foot") {
					Intent Sensor_Listener = new Intent(this, Sensor_Listener.class);

					startService(Sensor_Listener);
				}
				////////////////////////////////////////////////////////////////

				Log.v("aki", "aki_Actividad: " + activityName);

				if (badloc > 4 && goodloc != null && !afterbad) {
					write = false;
					loc1 = goodloc;
					Log.v("aki", "aki_badloc " + Integer.toString(badloc));
					afterbad = true;
					block = false;

				} else {
					badloc = 0;
				}

				// new size in case last location was added successfully to the
				// number of samples obtained within the windows time

				size = tempLocationArrayList.size();

				total_segment_delta_distance = 0.0;

				// Total Duration
				segment_total_duration = (tempLocationArrayList.get(size - 1).getTime() - tempLocationArrayList.get(0)
						.getTime()) / AppUtils.MILLISECONDS_PER_SECOND;

				// Block distance (not counting intermediate points) to compare with block size)
				block_distance = AppUtils.distanceCalculation(tempLocationArrayList.get(0).getLatitude(),
						tempLocationArrayList.get(0).getLongitude(), tempLocationArrayList.get(size - 1).getLatitude(),
						tempLocationArrayList.get(size - 1).getLongitude());

				//Total Distance
				for (int j = 0; j < size - 1; j++) {
					// Distance between consecutive locations, in meters
					segment_delta_distance = AppUtils.distanceCalculation(tempLocationArrayList.get(j).getLatitude(),
							tempLocationArrayList.get(j).getLongitude(),
							tempLocationArrayList.get(j + 1).getLatitude(), tempLocationArrayList.get(j + 1)
									.getLongitude());

					// Sum of all distance_deltas of the segment
					total_segment_delta_distance += segment_delta_distance;
				}

				size = 0;

				// If it is the first fix
				if (lastActivitiesArrayList.isEmpty() && !tempLocationArrayList.isEmpty()) {

					Log.v("aki", "aki_first fix");

					// update the activity array
					lastActivitiesArrayList.add(activityName);

					// store temp values
					temp_duration = segment_total_duration;
					if (segment_total_duration != 0.0) {
						temp_speed = (total_segment_delta_distance * 3.6) / segment_total_duration;
					} else {
						temp_speed = 0.0;
					}
					int segment_size = tempLocationArrayList.size();

					// create new segment to store data
					segment = new Segment(this);

					if (activityName.equals("still")) {
						if (segment_size > 1) {
							tempPlace = tempLocationArrayList.get(0);
							for (int j = 0; j < segment_size; j++) {
								if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
									tempPlace = tempLocationArrayList.get(j);
							}
						}

						// temp_distance = segment_total_distance;
						temp_distance = total_segment_delta_distance;

						// add parameters to created segment
						segment.setFirstLocation(tempLocationArrayList.get(0));
						segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
						segment.setTotalDistance(0);
						segment.setTotalDuration(temp_duration);
						segment.setTotalAverageSpeed(0);
						segment.setActivity("still");
						segment.addSingleLocationPoint(tempPlace);

						// store still place as Last Location (with the modified
						// time)

						// store segment in the last position of the segment
						// array (in this case is the first one)
						segmentArrayList.add(segment);

						tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

						tempLocationArrayList.clear();
					}

					else {

						// store temp distance
						temp_distance = total_segment_delta_distance;

						segment.setFirstLocation(tempLocationArrayList.get(0));
						segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
						segment.setTotalDistance(temp_distance);
						segment.setTotalDuration(temp_duration);
						segment.setTotalAverageSpeed(temp_speed);
						segment.setActivity(activityName);
						segment.updateLocationPoints(tempLocationArrayList);

						// store segment in the first position of the segment
						// array
						segmentArrayList.add(segment);

						tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

						tempLocationArrayList.clear();
					}
				}

				// Not the first fix
				else {

					dist = AppUtils.distanceCalculation(tempLocationArrayList.get(0).getLatitude(),
							tempLocationArrayList.get(0).getLongitude(), tmpLastLocation.getLatitude(),
							tmpLastLocation.getLongitude());

					prev_duration = segment_total_duration;

					segment_total_duration += ((tempLocationArrayList.get(0).getTime() - tmpLastLocation.getTime()) / AppUtils.MILLISECONDS_PER_SECOND);

					total_segment_delta_distance += dist;
					block_distance += dist;

					if (segment_total_duration != 0.0) {
						if (dist != 0) {
							average_segment_speed = (total_segment_delta_distance * 3.6) / segment_total_duration;
						} else {
							average_segment_speed = (total_segment_delta_distance * 3.6) / prev_duration;
						}
					} else {
						average_segment_speed = 0.0;
					}

					Log.v("aki", "aki_duration= " + Long.toString(segment_total_duration));
					Log.v("aki", "aki_distance= " + Double.toString(total_segment_delta_distance));
					Log.v("aki", "aki_block= " + Double.toString(block_distance));
					Log.v("aki", "aki_speed= " + Double.toString(average_segment_speed));

					if (write && !tempLocationArrayList.isEmpty())

						switch (activityName) {

						case "on_foot":

							// 2 straight samples on_foot
							if (lastActivitiesArrayList.get(0).equals("on_foot")) {

								if (average_segment_speed < AppUtils.MAX_ON_FOOT_SPEED) {

									if (block_distance > AppUtils.BLOCK_RADIUS) {
										// add & update activity of the list
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("on_foot");

										temp_duration += segment_total_duration;
										temp_distance += total_segment_delta_distance;
										temp_speed = (temp_speed + average_segment_speed) / 2;

										// update segment on_foot
										int segment_size = tempLocationArrayList.size();

										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(temp_distance);
										segment.setTotalDuration(temp_duration);
										segment.setTotalAverageSpeed(temp_speed);
										segment.setActivity("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);

										// update current segment
										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										int segment_size = tempLocationArrayList.size();

										segment = new Segment(this);

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(0);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(0);
										segment.setActivity("still");
										segment.addSingleLocationPoint(tempPlace);

										// add current segment in the next spot
										segmentArrayList.add(segment);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}

								} else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("unknown");

									int segment_size = tempLocationArrayList.size();

									segment = new Segment(this);

									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(total_segment_delta_distance);
									segment.setTotalDuration(segment_total_duration);
									segment.setTotalAverageSpeed(average_segment_speed);
									segment.setActivity("unknown");
									segment.updateLocationPoints(tempLocationArrayList);

									// add current segment in the next spot
									segmentArrayList.add(segment);

									// new values to temp_variables
									temp_duration = segment_total_duration;
									temp_distance = total_segment_delta_distance;
									temp_speed = average_segment_speed;

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
								}

							}

							else if (lastActivitiesArrayList.get(0).equals("metro")) {

								// update activity array
								lastActivitiesArrayList.clear();

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;

								segment = new Segment(this);
								// update segment data

								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);

								if (average_segment_speed < AppUtils.MAX_ON_FOOT_SPEED) {
									if (block_distance > AppUtils.BLOCK_RADIUS) {
										segment.setActivity("on_foot");
										lastActivitiesArrayList.add("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);
										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										segment = new Segment(this);

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(0);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(0);
										segment.setActivity("still");
										segment.addSingleLocationPoint(tempPlace);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}
								} else {
									segment.setActivity("unknown");
									lastActivitiesArrayList.add("unknown");
									segment.updateLocationPoints(tempLocationArrayList);
									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
								}

								// update current segment
								segmentArrayList.add(segment);

							}

							// segment vehicle followed by on_foot
							else if (lastActivitiesArrayList.get(0).equals("vehicle")) {

								// speed higher than on_foot maximum, it's
								// vehicle
								if (average_segment_speed > AppUtils.MAX_ON_FOOT_SPEED) {

									// update activity array
									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("vehicle");
									
									

									int segment_size = tempLocationArrayList.size();

									// update variables of current segment
									// vehicle
									temp_duration += segment_total_duration;
									temp_distance += total_segment_delta_distance;
									temp_speed = (temp_speed + average_segment_speed) / 2;

									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setActivity("vehicle");
									segment.updateLocationPoints(tempLocationArrayList);

									// update current segment
									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
								}

								// speed lower than on_foot maximum, it's
								// on_foot
								else {

									lastActivitiesArrayList.clear();

									int segment_size = tempLocationArrayList.size();

									temp_duration = segment_total_duration;
									temp_distance = total_segment_delta_distance;
									temp_speed = average_segment_speed;

									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);

									if (block_distance > AppUtils.BLOCK_RADIUS) {
										segment.setActivity("on_foot");
										lastActivitiesArrayList.add("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);
										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										segment.setActivity("still");
										lastActivitiesArrayList.add("still");
										segment.setTotalDistance(0);
										segment.setTotalAverageSpeed(0);

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.addSingleLocationPoint(tempPlace);
										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

									}
									segmentArrayList.add(segment);
								}

							}
							// segment bicycle followed by on_foot
							else if (lastActivitiesArrayList.get(0).equals("bicycle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("on_foot");

								int segment_size = tempLocationArrayList.size();

								// create new segment to store the new
								// values
								segment = new Segment(this);
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDistance(total_segment_delta_distance);
								segment.setTotalDuration(segment_total_duration);
								segment.setTotalAverageSpeed(average_segment_speed);
								segment.setActivity("on_foot");
								segment.updateLocationPoints(tempLocationArrayList);

								// add current segment
								segmentArrayList.add(segment);

								// new values to temp_variables
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							// segment still followed by on_foot
							else if (lastActivitiesArrayList.get(0).equals("still")) {

								if (average_segment_speed > AppUtils.MAX_STILL_SPEED) {
									if (block_distance > AppUtils.BLOCK_RADIUS) {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("on_foot");

										int segment_size = tempLocationArrayList.size();

										// create new segment to store the new
										// values
										segment = new Segment(this);
										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(total_segment_delta_distance);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(average_segment_speed);
										segment.setActivity("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);

										segmentArrayList.add(segment);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = total_segment_delta_distance;
										temp_speed = average_segment_speed;

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										int segment_size = tempLocationArrayList.size();

										temp_duration += segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										if (segment_size > 1) {
											tempPlace = tmpLastLocation;
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));

										segment.setTotalDuration(temp_duration);

										segment.setActivity("still");
										segment.clearLocationPoints();
										segment.addSingleLocationPoint(tempPlace);

										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}
								}

								// speed lower than still maximum, it's
								// still
								else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("still");

									int segment_size = tempLocationArrayList.size();

									// store temp values
									temp_duration += segment_total_duration;
									temp_distance = 0;
									temp_speed = 0;

									// update still segment
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDuration(temp_duration);
									segment.setActivity("still");

									if (segment_size > 1) {
										tempPlace = tmpLastLocation;
										for (int j = 0; j < segment_size; j++) {
											if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
												tempPlace = tempLocationArrayList.get(j);
										}
									}

									segment.clearLocationPoints();
									segment.addSingleLocationPoint(tempPlace);

									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}

							}
							// segment unknown followed by on_foot
							else if (lastActivitiesArrayList.get(0).equals("unknown")) {

								// speed lower than on_foot maximum & higher
								// than
								// still_maximum, it's on_foot
								if (average_segment_speed < AppUtils.MAX_ON_FOOT_SPEED) {
									if (block_distance > AppUtils.BLOCK_RADIUS) {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("on_foot");

										int segment_size = tempLocationArrayList.size();

										// update variables of current segment
										// on_foot
										temp_duration += segment_total_duration;
										temp_distance += total_segment_delta_distance;
										temp_speed = (temp_speed + average_segment_speed) / 2;

										// update segment data
										segment.setTotalDistance(temp_distance);
										segment.setTotalDuration(temp_duration);
										segment.setTotalAverageSpeed(temp_speed);
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setActivity("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);

										// update current segment
										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										int segment_size = tempLocationArrayList.size();

										// new values to temp_variables
										temp_duration += segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(0);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(0);
										segment.setActivity("still");
										segment.clearLocationPoints();
										segment.addSingleLocationPoint(tempPlace);

										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}
								}

								else {
									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("unknown");

									int segment_size = tempLocationArrayList.size();

									// update variables of current segment
									// on_foot
									temp_duration += segment_total_duration;
									temp_distance += total_segment_delta_distance;
									temp_speed = (temp_speed + average_segment_speed) / 2;

									// update segment data
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setActivity("unknown");
									segment.updateLocationPoints(tempLocationArrayList);

									// update current segment
									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}
							}

							break;

						case "vehicle":

							// segment vehicle followed by on_foot, it's on_foot + vehicle

							if (lastActivitiesArrayList.get(0).equals("on_foot")) {
								if (average_segment_speed > AppUtils.MAX_ON_FOOT_SPEED) {
									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("vehicle");

									int segment_size = tempLocationArrayList.size();

									// create new segment to store the new values
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(total_segment_delta_distance);
									segment.setTotalDuration(segment_total_duration);
									segment.setTotalAverageSpeed(average_segment_speed);
									segment.setActivity("vehicle");
									segment.updateLocationPoints(tempLocationArrayList);

									// add current segment
									segmentArrayList.add(segment);

									// new values to temp_variables
									temp_duration = segment_total_duration;
									temp_distance = total_segment_delta_distance;
									temp_speed = average_segment_speed;

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
								} else {

									if (block_distance > AppUtils.BLOCK_RADIUS) {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("on_foot");

										int segment_size = tempLocationArrayList.size();

										// update variables of current segment on_foot
										temp_duration += segment_total_duration;
										temp_distance += total_segment_delta_distance;
										temp_speed = (temp_speed + average_segment_speed) / 2;

										// update segment data
										segment.setTotalDistance(temp_distance);
										segment.setTotalDuration(temp_duration);
										segment.setTotalAverageSpeed(temp_speed);
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setActivity("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);

										// update current segment
										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");
										int segment_size = tempLocationArrayList.size();

										segment = new Segment(this);

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(0);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(0);
										segment.setActivity("still");
										segment.addSingleLocationPoint(tempPlace);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										segmentArrayList.add(segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}
								}
							} else if (lastActivitiesArrayList.get(0).equals("metro")) {

								// update activity array
								lastActivitiesArrayList.clear();

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;

								segment = new Segment(this);
								// update segment data
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));

								if (average_segment_speed > AppUtils.MAX_ON_FOOT_SPEED) {
									segment.setActivity("vehicle");
									lastActivitiesArrayList.add("vehicle");
									segment.updateLocationPoints(tempLocationArrayList);
									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
								} else {
									if (block_distance > AppUtils.BLOCK_RADIUS) {
										segment.setActivity("on_foot");
										lastActivitiesArrayList.add("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);
										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										segment = new Segment(this);

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(0);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(0);
										segment.setActivity("still");
										segment.addSingleLocationPoint(tempPlace);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}
								}

								// update current segment
								segmentArrayList.add(segment);

							}
							// segment vehicle followed by another vehicle: 2 vehicle

							else if (lastActivitiesArrayList.get(0).equals("vehicle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("vehicle");

								int segment_size = tempLocationArrayList.size();

								temp_duration += segment_total_duration;
								temp_distance += total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data vehicle
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("vehicle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}
							// segment still followed by vehicle & speed
							// higher
							// than
							// still maximum, it's still + vehicle
							else if (lastActivitiesArrayList.get(0).equals("still")) {

								if (average_segment_speed > AppUtils.MAX_ON_FOOT_SPEED) {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("vehicle");

									int segment_size = tempLocationArrayList.size();

									// create new segment to store the new
									// values
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(total_segment_delta_distance);
									segment.setTotalDuration(segment_total_duration);
									segment.setTotalAverageSpeed(average_segment_speed);
									segment.setActivity("vehicle");
									segment.updateLocationPoints(tempLocationArrayList);

									// add current segment
									segmentArrayList.add(segment);

									// new values to temp_variables
									temp_duration = segment_total_duration;
									temp_distance = total_segment_delta_distance;
									temp_speed = average_segment_speed;

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								} else {

									if (block_distance > AppUtils.BLOCK_RADIUS) {

										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("on_foot");

										int segment_size = tempLocationArrayList.size();

										// create new segment to store the new
										// values
										segment = new Segment(this);
										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDistance(total_segment_delta_distance);
										segment.setTotalDuration(segment_total_duration);
										segment.setTotalAverageSpeed(average_segment_speed);
										segment.setActivity("on_foot");
										segment.updateLocationPoints(tempLocationArrayList);

										// add current segment
										segmentArrayList.add(segment);

										// new values to temp_variables
										temp_duration = segment_total_duration;
										temp_distance = total_segment_delta_distance;
										temp_speed = average_segment_speed;

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

									} else {

										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										int segment_size = tempLocationArrayList.size();

										// store temp values
										temp_duration += segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										// update still segment
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDuration(temp_duration);
										segment.setActivity("still");

										// store segment in the first position
										// of
										// the
										// segment array
										segmentArrayList.set(segmentArrayList.size() - 1, segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

									}
								}
							}
							// segment vehicle followed by unknown
							else if (lastActivitiesArrayList.get(0).equals("unknown")) {

								// speed higher than still maximum, it's
								// vehicle
								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("vehicle");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration += segment_total_duration;
								temp_distance += total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("vehicle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							break;

						case "bicycle": // equal as vehicle

							// segment bicycle followed by on_foot, it's on_foot + bicycle

							if (lastActivitiesArrayList.get(0).equals("on_foot")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								// create new segment to store the new
								// values
								segment = new Segment(this);
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDistance(total_segment_delta_distance);
								segment.setTotalDuration(segment_total_duration);
								segment.setTotalAverageSpeed(average_segment_speed);
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// add current segment
								segmentArrayList.add(segment);

								// new values to temp_variables
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							} else if (lastActivitiesArrayList.get(0).equals("metro")) {

								// update activity array
								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;
								segment = new Segment(this);
								// update segment data
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.add(segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							} else if (lastActivitiesArrayList.get(0).equals("vehicle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								// create new segment to store the new
								// values
								segment = new Segment(this);
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDistance(total_segment_delta_distance);
								segment.setTotalDuration(segment_total_duration);
								segment.setTotalAverageSpeed(average_segment_speed);
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// add current segment
								segmentArrayList.add(segment);

								// new values to temp_variables
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							// segment bicycle followed by another bicycle:
							// 2
							// bicycle
							else if (lastActivitiesArrayList.get(0).equals("bicycle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								temp_duration += segment_total_duration;
								temp_distance += total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data bicycle
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}
							// segment still followed by bicycle & speed
							// higher
							// than
							// still maximum, it's still + bicycle
							else if (lastActivitiesArrayList.get(0).equals("still")) {

								if (average_segment_speed > AppUtils.MAX_STILL_SPEED
										&& block_distance > AppUtils.BLOCK_RADIUS) {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("bicycle");

									int segment_size = tempLocationArrayList.size();

									// create new segment to store the new
									// values
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(total_segment_delta_distance);
									segment.setTotalDuration(segment_total_duration);
									segment.setTotalAverageSpeed(average_segment_speed);
									segment.setActivity("bicycle");
									segment.updateLocationPoints(tempLocationArrayList);

									// add current segment
									segmentArrayList.add(segment);

									// new values to temp_variables
									temp_duration = segment_total_duration;
									temp_distance = total_segment_delta_distance;
									temp_speed = average_segment_speed;

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}

								else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("still");

									int segment_size = tempLocationArrayList.size();

									// store temp values
									temp_duration += segment_total_duration;
									temp_distance = 0;
									temp_speed = 0;

									// update still segment
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDuration(temp_duration);
									segment.setActivity("still");

									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}
							}
							// segment bicycle followed by unknown
							else if (lastActivitiesArrayList.get(0).equals("unknown")) {

								// speed higher than bicycle minimum, it's
								// bicycle
								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// bicycle
								temp_duration += segment_total_duration;
								temp_distance += total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							break;

						case "still":
							// segment on_foot followed by still
							if (lastActivitiesArrayList.get(0).equals("on_foot")) {

								if (average_segment_speed > AppUtils.MAX_STILL_SPEED) {
									if (block_distance > AppUtils.BLOCK_RADIUS) {
										if (average_segment_speed < AppUtils.MAX_ON_FOOT_SPEED) {
											lastActivitiesArrayList.clear();
											lastActivitiesArrayList.add("on_foot");

											int segment_size = tempLocationArrayList.size();

											temp_duration += segment_total_duration;
											temp_distance += total_segment_delta_distance;
											temp_speed = (temp_speed + average_segment_speed) / 2;

											segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
											segment.setTotalDistance(temp_distance);
											segment.setTotalDuration(temp_duration);
											segment.setTotalAverageSpeed(temp_speed);
											segment.setActivity("on_foot");
											segment.updateLocationPoints(tempLocationArrayList);

											// add current segment
											segmentArrayList.set(segmentArrayList.size() - 1, segment);

											tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
										} else {

											if (vehicle_count > 0) {

												// update activity array
												lastActivitiesArrayList.clear();
												lastActivitiesArrayList.add("vehicle");

												int segment_size = tempLocationArrayList.size();

												// update variables of current segment
												// vehicle
												temp_duration = segment_total_duration;
												temp_distance = total_segment_delta_distance;
												temp_speed = average_segment_speed;
												segment = new Segment(this);
												// update segment data
												segment.setFirstLocation(tempLocationArrayList.get(0));
												segment.setTotalDistance(temp_distance);
												segment.setTotalDuration(temp_duration);
												segment.setTotalAverageSpeed(temp_speed);
												segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
												segment.setActivity("vehicle");
												segment.updateLocationPoints(tempLocationArrayList);

												// update current segment
												segmentArrayList.add(segment);

												tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

											}

										}
									} else {
										lastActivitiesArrayList.clear();
										lastActivitiesArrayList.add("still");

										int segment_size = tempLocationArrayList.size();

										if (segment_size > 1) {
											tempPlace = tempLocationArrayList.get(0);
											for (int j = 0; j < segment_size; j++) {
												if (tempPlace.getAccuracy() > tempLocationArrayList.get(j)
														.getAccuracy())
													tempPlace = tempLocationArrayList.get(j);
											}
										}

										// clear temp_variables
										temp_duration = segment_total_duration;
										temp_distance = 0;
										temp_speed = 0;

										// create new segment
										segment = new Segment(this);
										segment.setFirstLocation(tempLocationArrayList.get(0));
										segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
										segment.setTotalDuration(temp_duration);
										segment.setTotalDistance(temp_distance);
										segment.setTotalAverageSpeed(temp_speed);
										segment.setActivity("still");
										segment.addSingleLocationPoint(tempPlace);

										segmentArrayList.add(segment);

										tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
									}

									// speed lower than still maximum, it's
									// still
								} else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("still");

									int segment_size = tempLocationArrayList.size();

									if (segment_size > 1) {
										tempPlace = tempLocationArrayList.get(0);
										for (int j = 0; j < segment_size; j++) {
											if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
												tempPlace = tempLocationArrayList.get(j);
										}
									}

									// clear temp_variables
									temp_duration = segment_total_duration;
									temp_distance = 0;
									temp_speed = 0;

									// create new segment
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDuration(temp_duration);
									segment.setTotalDistance(temp_distance);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setActivity("still");
									segment.addSingleLocationPoint(tempPlace);

									segmentArrayList.add(segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}

							} else if (lastActivitiesArrayList.get(0).equals("metro")) {

								// update activity array
								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("still");

								int segment_size = tempLocationArrayList.size();

								if (segment_size > 1) {
									tempPlace = tempLocationArrayList.get(0);
									for (int j = 0; j < segment_size; j++) {
										if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
											tempPlace = tempLocationArrayList.get(j);
									}
								}
								// update variables of current segment
								// vehicle
								temp_duration = segment_total_duration;
								temp_distance = 0;
								temp_speed = 0;

								segment = new Segment(this);
								// update segment data
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("still");
								segment.addSingleLocationPoint(tempPlace);

								// update current segment
								segmentArrayList.add(segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}
							// segment vehicle followed by still
							else if (lastActivitiesArrayList.get(0).equals("vehicle")) {

								if (average_segment_speed > AppUtils.MAX_ON_FOOT_SPEED) {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("vehicle");

									int segment_size = tempLocationArrayList.size();

									// update variables of current segment
									// vehicle
									temp_duration = temp_duration + segment_total_duration;
									temp_distance = temp_distance + total_segment_delta_distance;
									temp_speed = (temp_speed + average_segment_speed) / 2;

									// update segment data
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setActivity("vehicle");
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.updateLocationPoints(tempLocationArrayList);

									// update current segment
									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}

								// speed lower than still maximum, it's
								// still
								else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("still");

									int segment_size = tempLocationArrayList.size();

									if (segment_size > 1) {
										tempPlace = tempLocationArrayList.get(0);
										for (int j = 0; j < segment_size; j++) {
											if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
												tempPlace = tempLocationArrayList.get(j);
										}
									}

									// clear temp_variables
									temp_duration = segment_total_duration;
									temp_distance = 0;
									temp_speed = 0;

									// create new segment
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setActivity("still");
									segment.addSingleLocationPoint(tempPlace);

									segmentArrayList.add(segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}
							}

							// segment vehicle followed by still
							else if (lastActivitiesArrayList.get(0).equals("bicycle")) {

								if (average_segment_speed > AppUtils.MAX_STILL_SPEED
										&& block_distance > AppUtils.BLOCK_RADIUS) {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("bicycle");

									int segment_size = tempLocationArrayList.size();

									// update variables of current segment
									// vehicle
									temp_duration += segment_total_duration;
									temp_distance += total_segment_delta_distance;
									temp_speed = (temp_speed + average_segment_speed) / 2;

									// update segment data
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setActivity("bicycle");
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.updateLocationPoints(tempLocationArrayList);

									// update current segment
									segmentArrayList.set(segmentArrayList.size() - 1, segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}

								// speed lower than still maximum, it's
								// still
								else {

									lastActivitiesArrayList.clear();
									lastActivitiesArrayList.add("still");

									int segment_size = tempLocationArrayList.size();

									if (segment_size > 1) {
										tempPlace = tempLocationArrayList.get(0);
										for (int j = 0; j < segment_size; j++) {
											if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
												tempPlace = tempLocationArrayList.get(j);
										}
									}

									// clear temp_variables
									temp_duration = segment_total_duration;
									temp_distance = 0;
									temp_speed = 0;

									// create new segment
									segment = new Segment(this);
									segment.setFirstLocation(tempLocationArrayList.get(0));
									segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
									segment.setTotalDistance(temp_distance);
									segment.setTotalDuration(temp_duration);
									segment.setTotalAverageSpeed(temp_speed);
									segment.setActivity("still");
									segment.addSingleLocationPoint(tempPlace);

									segmentArrayList.add(segment);

									tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

								}
							}
							// segment still followed by still
							else if (lastActivitiesArrayList.get(0).equals("still")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("still");

								int segment_size = tempLocationArrayList.size();

								// update temp_variables
								temp_duration = temp_duration + segment_total_duration;
								temp_distance = 0;
								temp_speed = 0;

								// update still segment
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDuration(temp_duration);
								segment.setActivity("still");
								segment.clearLocationPoints();

								if (segment_size > 1) {
									tempPlace = tempLocationArrayList.get(0);
									for (int j = 0; j < segment_size; j++) {
										if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
											tempPlace = tempLocationArrayList.get(j);
									}
								}
								segment.addSingleLocationPoint(tempPlace);

								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}
							// segment unknown followed by still sample
							else if (lastActivitiesArrayList.get(0).equals("unknown")) { // igual que antes

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("still");

								int segment_size = tempLocationArrayList.size();

								// update temp_variables
								temp_duration = temp_duration + segment_total_duration;
								temp_distance = 0;
								temp_speed = 0;

								// update still segment
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDuration(temp_duration);
								segment.setActivity("still");

								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							break;

						case "unknown":

							// segment on_foot followed by unknown sample
							if (lastActivitiesArrayList.get(0).equals("on_foot")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("on_foot");

								int segment_size = tempLocationArrayList.size();
								// update variables of current segment
								// vehicle

								temp_duration = temp_duration + segment_total_duration;
								temp_distance = temp_distance + total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("on_foot");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							} else if (lastActivitiesArrayList.get(0).equals("metro")) {

								// update activity array
								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("unknown");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = segment_total_duration;
								temp_distance = total_segment_delta_distance;
								temp_speed = average_segment_speed;
								segment = new Segment(this);
								// update segment data
								segment.setFirstLocation(tempLocationArrayList.get(0));
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("unknown");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.add(segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							// segment vehicle followed by unknown sample
							else if (lastActivitiesArrayList.get(0).equals("vehicle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("vehicle");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = temp_duration + segment_total_duration;
								temp_distance = temp_distance + total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("vehicle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}
							// segment bicycle followed by unknown sample
							// (speed
							// threshold higher than still)
							else if (lastActivitiesArrayList.get(0).equals("bicycle")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("bicycle");

								int segment_size = tempLocationArrayList.size();

								// update variables of current segment
								// vehicle
								temp_duration = temp_duration + segment_total_duration;
								temp_distance = temp_distance + total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("bicycle");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							// segment still followed by unknown sample
							// (speed
							// threshold according to still features)
							else if (lastActivitiesArrayList.get(0).equals("still")) { //como antes

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("still");

								int segment_size = tempLocationArrayList.size();

								// store temp values
								temp_duration += segment_total_duration;
								temp_distance = 0;
								temp_speed = 0;

								// update still segment
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setTotalDuration(temp_duration);
								segment.setActivity("still");
								segment.clearLocationPoints();

								if (segment_size > 1) {
									tempPlace = tempLocationArrayList.get(0);
									for (int j = 0; j < segment_size; j++) {
										if (tempPlace.getAccuracy() > tempLocationArrayList.get(j).getAccuracy())
											tempPlace = tempLocationArrayList.get(j);
									}
								}

								segment.addSingleLocationPoint(tempPlace);

								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);
							}

							// 2 straight unknown samples
							else if (lastActivitiesArrayList.get(0).equals("unknown")) {

								lastActivitiesArrayList.clear();
								lastActivitiesArrayList.add("unknown");

								// update variables of current segment
								// vehicle
								temp_duration = temp_duration + segment_total_duration;
								temp_distance = temp_distance + total_segment_delta_distance;
								temp_speed = (temp_speed + average_segment_speed) / 2;

								int segment_size = tempLocationArrayList.size();

								// update segment data
								segment.setTotalDistance(temp_distance);
								segment.setTotalDuration(temp_duration);
								segment.setTotalAverageSpeed(temp_speed);
								segment.setLastLocation(tempLocationArrayList.get(segment_size - 1));
								segment.setActivity("unknown");
								segment.updateLocationPoints(tempLocationArrayList);

								// update current segment
								segmentArrayList.set(segmentArrayList.size() - 1, segment);

								tmpLastLocation = tempLocationArrayList.get(segment_size - 1);

							}

							break;
						}
				}
				vehicle_count = 0;
				bicycle_count = 0;
				on_foot_count = 0;
				still_count = 0;
				unknown_count = 0;

				// clear all values from temporal Location array
				if (!tempLocationArrayList.isEmpty())
					tempLocationArrayList.clear();
 /*
				if (!segmentArrayList.isEmpty())
					if (!(AppUtils
							.getDay(segmentArrayList.get(segmentArrayList.size() - 1).getLastLocation().getTime())
							.equalsIgnoreCase(lastday))
							&& lastday != null) {
						if (!upload_diffday) {// solo tiene que entrar cuando no se detiene la app al cambiar de dia
							Log.v("aki", "aki_cambiodedia(madrugada)");
							postproc();
							calperday(AppUtils.getDay(System.currentTimeMillis() - 3600000));
							co2perday(AppUtils.getDay(System.currentTimeMillis() - 3600000));
							writeSegment(ruta + lastday + "location_segment.json", segmentArrayList); //segmento procesado
                            segmentArrayList.clear();
							lastActivitiesArrayList.clear();
							runn.run();

							prefs.edit().remove("lastday").commit(); //borramos la entrada cuando lo hemos subido
							lastday = AppUtils.getDay(System.currentTimeMillis());
                            borrarJsonsAntiguos();


						}else { //cuando se ha abierto al dia siguiente cerrandola antes del cambio de dia
							Log.v("aki", "aki_cambiodedia(dia siguiente)");

							tempSegment = segmentArrayList.get(segmentArrayList.size() - 1); //guardamos el único segmento para luego añadirlo

							recoverArray(new File(ruta + lastday + "location_segment.json"),
									AppUtils.convertToEngl(lastday)); //ahora ya tenenmos el segment array para procesar y subirlo

							lastActivitiesArrayList.clear(); //elimino la last activity del dia anterior

							postproc();
							calperday(lastday);
							co2perday(lastday);
							writeSegment(ruta + lastday + "location_segment.json", segmentArrayList);
							segmentArrayList.clear();
							segmentArrayList.add(tempSegment);

//							uploadJSON(ruta + lastday + "location_segment.json");
							runn.run();

							prefs.edit().remove("lastday").commit();
							upload_diffday = false;
							lastActivitiesArrayList.add(tempSegment.getActivity());
							tmpLastLocation = tempSegment.getLastLocation();
                            borrarJsonsAntiguos();
						}
					}*/

				if (!segmentArrayList.isEmpty() && write) {

					Log.v("aki", "aki_writejson");
                    borrarJsonsAntiguos();
					Log.v("aki", "aki_last_act en array: "
							+ segmentArrayList.get(segmentArrayList.size() - 1).getActivity());

					writeSegment(ruta + AppUtils.getDay(System.currentTimeMillis()) + "location_segment.json",
							segmentArrayList);
					lastday = AppUtils.getDay(System.currentTimeMillis());

                    Log.v("aki", "hora: "+AppUtils.getTime(segmentArrayList.get(segmentArrayList.size() - 1).getLastLocation().getTime()));
                    //AppUtils.getTime(segmentArrayList.get(segmentArrayList.size() - 1).getLastLocation().getTime()).equalsIgnoreCase(lastday);
                    Log.v("aki", "last_day: "+lastday);

                    lastupload = prefs.getString("lastupload","0");

                    if (lastupload == null || lastupload =="0") {
                        lastupload = lastday;
                        prefs.edit().putString("lasupload",lastupload);
                    }

                    LocalTime hora = new LocalTime(AppUtils.getTime(segmentArrayList.get(segmentArrayList.size() - 1).getLastLocation().getTime()));

                    LocalTime horasubidaIN = new LocalTime(00,00,00);
                    LocalTime horasubidaOUT = new LocalTime(00,06,00);
                    if (hora.isBefore(horasubidaOUT) && hora.isAfter(horasubidaIN)){

                        postproc();
                        writeSegment(ruta + lastday + "location_segment.json", segmentArrayList); //segmento procesado
                        segmentArrayList.clear();
                        lastActivitiesArrayList.clear();
                        runn.run();
                        borrarJsonsAntiguos();
                    }

                    LocalTime horasubidaIN2 = new LocalTime(8,00,00);
                    LocalTime horasubidaOUT2 = new LocalTime(8,06,00);
                    if (hora.isBefore(horasubidaOUT2) && hora.isAfter(horasubidaIN2)){
                        postproc();
                        writeSegment(ruta + lastday + "location_segment.json", segmentArrayList); //segmento procesado
                        runn.run();
                    }


                    LocalTime horasubidaIN3 = new LocalTime(16,00,00);
                    LocalTime horasubidaOUT3 = new LocalTime(16,06,00);
                    if (hora.isBefore(horasubidaOUT3) && hora.isAfter(horasubidaIN3)){
                        postproc();
                        writeSegment(ruta + lastday + "location_segment.json", segmentArrayList); //segmento procesado
                        runn.run();
                    }
				}
			}
	}

    public void borrarJsonsAntiguos(){

        File dir = new File(ruta);
        if (dir!=null){
            File[] files = dir.listFiles();
			try {
				Log.v("aki", "Numero de files en el directorio: " + files.length);
			}catch (Exception e){
				e.printStackTrace();
			}

            if  (files.length>=32){
               for (File tmpf : files){
                   tmpf.delete();
               }
            }
        }

    }

	private void needGPS(boolean value) {
		if (hasGPS) {
			Log.v("aki", "aki_needGPS");

			Requester req = new Requester(this);

			req.requestUpdates("LOC", value);

		}
	}

	@Override
	public void onDestroy() {
		Log.v("aki", "aki_destroy_loca");
		unregisterReceiver(activityReceiver);

		prefs.edit().putString("lastday", AppUtils.getDay(System.currentTimeMillis())).commit();
	}

	//Data Sensor is received

	public void StoreDataSensor(SensorDataList sensorDataList) {

		Log.v(MyTAG, "Acceleration and Gyroscope data received");

		//the execute function can only be called by the main service/action
		//FRAN: comentamos esta linea porque usamos otros métodos
		//new DataStorage(this).execute(sensorDataList);

		Log.v(MyTAG, "SensorData_OK");

	}

	private class GetPlaces extends AsyncTask<Double, Void, Void> {

		private String places;

		public GetPlaces(String places) {
			this.places = places;
		}

		@Override
		protected Void doInBackground(Double... loc) {

			PlacesService service = new PlacesService(API);
			ArrayList<String> result = service.findPlaces(loc[0], loc[1], loc[2], loc[3], places);
			if (result != null) {
				if (result.size() == 2) {
					orig = result.get(0);
					dest = result.get(1);
					Log.v("aki", "aki_origen: " + orig);
					Log.v("aki", "aki_destino: " + dest);
					if (!orig.equalsIgnoreCase(dest)) {
						getDistance(orig, dest);
					} else {
						falsepositive = true;
					}
				} else {
					falsepositive = true;
					if (result.isEmpty())
						Log.v("aki", "aki_empty");
					else if (!result.get(0).isEmpty())
						Log.v("aki", "aki_result[0]: " + result.get(0));
				}
			} else {
				Log.v("aki", "aki_null");

				falsepositive = true;
			}

			return null;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

	}

	public String loadJSONFromAsset(String asset) {

		String json = null;

		try {
			InputStream is = getAssets().open(asset);
			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return json;

	}

	public void getIDjson() {

		hmids = new HashMap<String, Integer>();
		lmm = ArrayListMultimap.create();

		try {
			JSONObject obj = new JSONObject(loadJSONFromAsset("metrobcn copia.json"));
			arrayjson = obj.getJSONArray("metro");
			for (int i = 0; i < arrayjson.length(); i++) {

				lmm.put(arrayjson.getJSONObject(i).getString("name"), i);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getDistance(String orig, String dest) {

		getIDjson();
		double lator, longor, latdest, longdest;
		long total_time = 0;
		int number;
		int id, id2;
		id = id2 = 0;
		int aux;
		float[] results = new float[1];
		String destino_orig = dest;
		int size_orig = lmm.get(orig).size();
		int size_dest = lmm.get(dest).size();
		if (sameLineStation(orig, dest)) {
			Log.v("aki", "aki_sameline");
			transb = 1;
			if (size_orig == 1 && size_dest == 1) {
				try {
					line = arrayjson.getJSONObject((int) lmm.get(orig).toArray()[0]).getString("line");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (size_orig > size_dest && size_dest == 1) {
				try {
					line = arrayjson.getJSONObject((int) lmm.get(dest).toArray()[0]).getString("line");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (size_orig < size_dest && size_orig == 1) {
				try {
					line = arrayjson.getJSONObject((int) lmm.get(orig).toArray()[0]).getString("line");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			Log.v("aki", "aki_differents");
			r = new ArrayList<String>();
			r = differentlines(orig, dest);
			line = r.get(0);
			dest = r.get(2);
			transb = 2;

		}

		for (int a = 0; a < transb; a++) {
			if (a == 1) {
				line = r.get(1);
				orig = r.get(2);
				dest = destino_orig;
				unavez = true;
			}
			switch (line) {

			case "L1":

				linea = getResources().getStringArray(R.array.L1);
				al = new ArrayList<String>(Arrays.asList(linea));
				break;

			case "L2":

				linea = getResources().getStringArray(R.array.L2);
				al = new ArrayList<String>(Arrays.asList(linea));
				break;

			case "L3":

				linea = getResources().getStringArray(R.array.L3);
				al = new ArrayList<String>(Arrays.asList(linea));
				break;

			case "L4":

				linea = getResources().getStringArray(R.array.L4);
				al = new ArrayList<String>(Arrays.asList(linea));
				break;

			case "L5":

				linea = getResources().getStringArray(R.array.L5);
				al = new ArrayList<String>(Arrays.asList(linea));
				break;

			}

			int id_orig = al.indexOf(orig);
			int id_dest = al.indexOf(dest);
			number = id_dest - id_orig;

			if (number < 0) {
				aux = id_orig;
				id_orig = id_dest;
				id_dest = aux;
				number = -number;
				reverse = true;
			}

			for (int i = 0; i < number; i++) {

				if (lmm.get(al.get(id_orig + i)).size() > 1) {

					for (Iterator<Integer> it = lmm.get(al.get(id_orig + i)).iterator(); it.hasNext();) {
						id = it.next();
						try {
							if (arrayjson.getJSONObject(id).getString("line").equalsIgnoreCase(line)) {
								break;

							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} else {
					id = (int) lmm.get(al.get(id_orig + i)).toArray()[0];
				}

				if (lmm.get(al.get(id_orig + i + 1)).size() > 1) {

					for (Iterator<Integer> it2 = lmm.get(al.get(id_orig + i + 1)).iterator(); it2.hasNext();) {
						id2 = it2.next();
						try {
							if (arrayjson.getJSONObject(id2).getString("line").equalsIgnoreCase(line)) {
								break;

							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} else {
					id2 = (int) lmm.get(al.get(id_orig + i + 1)).toArray()[0];
				}

				try {

					if (reverse) {
						reverse = false;
						pos2rev = arraymetro.size();
						Log.v("aki", "aki_ " + Integer.toString(pos2rev));
					}

					lator = arrayjson.getJSONObject(id).getDouble("lat");
					longor = arrayjson.getJSONObject(id).getDouble("lon");
					latdest = arrayjson.getJSONObject(id2).getDouble("lat");
					longdest = arrayjson.getJSONObject(id2).getDouble("lon");

					Location.distanceBetween(lator, longor, latdest, longdest, results);

					if (unavez) {
						Location lorig = new Location("Metro_orig");
						lorig.setLatitude(lator);
						lorig.setLongitude(longor);
						lorig.setTime(loc1.getTime());
						arraymetro.add(lorig);
						unavez = false;
					}
					Location ldest = new Location("Metro_dest");
					ldest.setLatitude(latdest);
					ldest.setLongitude(longdest);
					ldest.setTime(loc1.getTime() + (total_time / number) * (i + 1)); //como son localizaciones falsas, no se el tiempo. Asi que hago incrementos iguales entre estaciones

					arraymetro.add(ldest);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				distance_metro += results[0];

			}
			writeMetro();
			distance_metro = 0;
		}
		avoid = false;
		write = true;
	}

	public boolean sameLineStation(String o, String d) {
		// mirar si estamos en la misma linea de metro es decir NO HACEMOS TRANSBORDO
		getIDjson();

		for (int i = 0; i < lmm.get(o).size(); i++) {
			for (int j = 0; j < lmm.get(d).size(); j++) {
				try {
					if (arrayjson.getJSONObject((int) lmm.get(o).toArray()[i]).getString("line")
							.equalsIgnoreCase(arrayjson.getJSONObject((int) lmm.get(d).toArray()[j]).getString("line"))) {
						return true;

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return false;

	}

	public ArrayList<String> differentlines(String o, String d) {
		getIDjson();
		Integer estaciones = 100, auxi = 0;
		int id1, id = 0;
		ArrayList<String> pareja = new ArrayList<String>();
		ArrayList<String> save = new ArrayList<String>();
		for (int i = 0; i < lmm.get(o).size(); i++) {
			for (int j = 0; j < lmm.get(d).size(); j++) {
				try {
					String lineorig = arrayjson.getJSONObject((int) lmm.get(o).toArray()[i]).getString("line");
					String linedest = arrayjson.getJSONObject((int) lmm.get(d).toArray()[j]).getString("line");

					switch (lineorig) {

					case "L1":

						linea = getResources().getStringArray(R.array.L1);
						al = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L2":

						linea = getResources().getStringArray(R.array.L2);
						al = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L3":

						linea = getResources().getStringArray(R.array.L3);
						al = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L4":

						linea = getResources().getStringArray(R.array.L4);
						al = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L5":

						linea = getResources().getStringArray(R.array.L5);
						al = new ArrayList<String>(Arrays.asList(linea));

						break;
					}

					switch (linedest) {

					case "L1":

						linea = getResources().getStringArray(R.array.L1);
						al2 = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L2":

						linea = getResources().getStringArray(R.array.L2);
						al2 = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L3":

						linea = getResources().getStringArray(R.array.L3);
						al2 = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L4":

						linea = getResources().getStringArray(R.array.L4);
						al2 = new ArrayList<String>(Arrays.asList(linea));

						break;

					case "L5":

						linea = getResources().getStringArray(R.array.L5);
						al2 = new ArrayList<String>(Arrays.asList(linea));

						break;
					}
					if (lineorig.equalsIgnoreCase(linedest)) {
						estaciones = al.indexOf(o) - al.indexOf(d);
						if (estaciones < 0)
							estaciones = -estaciones;
						pareja.add(lineorig);
						pareja.add(linedest);
						pareja.add("PRO"); // OJOOOOO
						pareja.add(estaciones.toString());
					} else {
						for (int k = 0; k < al.size(); k++) { // recorre linea origen entera

							if (k == al.indexOf(o)) {
								continue;
							}
							for (int l = 0; l < lmm.get(al.get(k)).size(); l++) { // recorre arraylist mismo nombre

								id1 = (int) lmm.get(al.get(k)).toArray()[l]; // id para jsonObject

								if (arrayjson.getJSONObject(id1).getString("line").toString()
										.equalsIgnoreCase(lineorig)) { // si el id corresponde a mi linea me lo quedo sino lo descarto

									id = id1;

									if (arrayjson.getJSONObject(id).getString("connections").toString()
											.contains(linedest)) { // miramos que se pueda hacer el transbordo

										int res = al.indexOf(o) - al.indexOf(al.get(k));
										int res1 = al2.indexOf(al.get(k)) - al2.indexOf(d);
										if (res < 0) {
											res = -res;
										}
										if (res1 < 0) {
											res1 = -res1;
										}
										auxi = res + res1;
										if (auxi < estaciones) {
											if (!pareja.isEmpty()) {

												for (int z = 0; z < pareja.size(); z++) {
													save.add(pareja.get(z));
												}
												pareja.removeAll(save);
											}
											pareja.add(lineorig);
											pareja.add(linedest);
											pareja.add(al.get(k));
											pareja.add(auxi.toString());
											estaciones = auxi;

										}

									}
								}
							}

						}

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return pareja;
	}

	private void recoverArray(File file, String day) {
		Log.v("aki", "aki_recover");
		Segment s;
		Location locfirst, loclast, loca;
		Timestamp ts1, ts2, ts3;
		ArrayList<Location> arraloc = new ArrayList<Location>();
		try {

			fis = new FileInputStream(file);
			String jsonStr = null;

			try {
				FileChannel fc = fis.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				jsonStr = Charset.defaultCharset().decode(bb).toString();
			} finally {
				fis.close();
			}

			JSONObject jsonObj = new JSONObject(jsonStr);

			// Getting JSON Array node
			segmentsData = jsonObj.getJSONArray("segments");

			for (int i = 0; i < segmentsData.length(); i++) {
				s = new Segment(this);
				locfirst = new Location("inventada");
				loclast = new Location("inventada2");
				JSONObject segment = segmentsData.getJSONObject(i);

				ts1 = Timestamp.valueOf(day + " " + segment.getString("first time") + ".0");
				ts2 = Timestamp.valueOf(day + " " + segment.getString("last time") + ".0");
				locfirst.setTime(ts1.getTime());
				loclast.setTime(ts2.getTime());
				s.setActivity(segment.getString("activity"));
				s.setTotalDistance(segment.getDouble("distance (m)"));
				if (segment.getString("activity").equalsIgnoreCase("metro"))
					s.setLine(segment.getString("line"));
				s.setTotalDuration(segment.getLong("duration (s)"));
				s.setTotalAverageSpeed(segment.getDouble("speed (Km/h)"));

				JSONArray location_points = segment.getJSONArray("location");

				for (int j = 0; j < location_points.length();) {
					loca = new Location("update");
					ts3 = Timestamp.valueOf(day + " " + location_points.get(j + 2).toString() + ".0");

					loca.setLatitude(location_points.optDouble(j));
					loca.setLongitude(location_points.optDouble(j + 1));
					loca.setTime(ts3.getTime());
                    loca.setProvider(location_points.getString(j + 3));


					arraloc.add(loca);
					j = j + 4;
				}
				s.updateLocationPoints(arraloc);

				locfirst.setLatitude(arraloc.get(0).getLatitude());
				locfirst.setLongitude(arraloc.get(0).getLongitude());

				loclast.setLatitude(arraloc.get(arraloc.size() - 1).getLatitude());
				loclast.setLongitude(arraloc.get(arraloc.size() - 1).getLongitude());

				s.setFirstLocation(locfirst);
				s.setLastLocation(loclast);

				arraloc.clear();
				segmentArrayList.add(s);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		this.segment = new Segment(this);
//		this.segment = segmentArrayList.get(segmentArrayList.size() - 1);
//
//		lastActivitiesArrayList.add(this.segment.getActivity());
//		temp_duration = this.segment.getTotalDuration();
//		temp_distance = this.segment.getTotalDistance();
//		temp_speed = this.segment.getAverageSpeed();
//		tmpLastLocation = this.segment.getLastLocation();
	}

	private class GetBusorTram extends AsyncTask<Double, Void, String> {

		long departure_time, arrival_time;

		public GetBusorTram(long departure_time, long arrival_time) {
			this.departure_time = departure_time;
			this.arrival_time = arrival_time;
		}

		@Override
		protected String doInBackground(Double... params) {

			DirectionsService ds = new DirectionsService(params[0], params[1], params[2], params[3], departure_time,
					arrival_time);

			return ds.findBusorTram();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equalsIgnoreCase("BUS")) {
				Log.v("aki", "aki_BUStrue");
			} else if (result.equalsIgnoreCase("TRAM")) {
				Log.v("aki", "aki_TRAMtrue");

			} else {
				Log.v("aki", "aki_noBUS/TRAM");
			}
		}

	}

	private String uploadJSON(String filename) {

		String result;

		try {
			result = new StorageJSON(getResources().openRawResource(R.raw.private_key), id, filename).execute().get();

			if (result.equalsIgnoreCase("KO")) {
				h.postDelayed(runn, 60 * 60 * 1000); // try every 60 mins upload
				Log.v("aki", "aki_upload_KO");
			} else {
				Log.v("aki", "aki_upload_OK");
				lastupload = AppUtils.getDay(System.currentTimeMillis());
				prefs.edit().putString("lastupload", lastupload).commit();
			}

			return result;
		} catch (NotFoundException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String uploadJSONDB(String filename) {

		String result="enviado";
		String nombreUsuario=prefsGoogle.getString("nombre", "");
		String apellidoUsuario=prefsGoogle.getString("apellidos", "");
		String pesoUsuario=peso.getString("peso", "0");
		String nacimientoUsuario=prefsGoogle.getString("birthday", "");
        String mailUsuario=prefsGoogle.getString("correo","");
        String generoUsuario=peso.getString("genero","");
		
		if (nombreUsuario.equals("")){
			nombreUsuario=prefsFace.getString("nombre", "not_set");
		}
		if (apellidoUsuario.equals("")){
			apellidoUsuario=prefsFace.getString("apellidos", "not_set");
		}
		if (nacimientoUsuario.equals("")){
			nacimientoUsuario=prefsFace.getString("birthday", "0000-00-00");
            if (nacimientoUsuario.equals("0000-00-00")){
               nacimientoUsuario = peso.getString("born","0000");
               nacimientoUsuario = nacimientoUsuario+"-01-01";

            }
		}
		if (mailUsuario.equals("")){
            mailUsuario=prefsFace.getString("email", "");
            if (mailUsuario.equals("")){
                mailUsuario = "notset@notset.com";
            }
        }
		if (generoUsuario.equals("")){
            generoUsuario = "not_set";

        }

        String dateFile=lastupload;
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd" );
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date;
        try {
        date = originalFormat.parse(lastupload);
        dateFile=targetFormat.format(date);
            Log.v("bbdd","fecha: "+dateFile);

        } catch (ParseException ex) {

        }


        id = prefs.getString("id","no_id");

        //(new SaveDatabase(ruta, id, nombreUsuario, apellidoUsuario, pesoUsuario, nacimientoUsuario, mailUsuario, generoUsuario, true)).execute();
        //(new SaveDatabase(id, filename,ruta,nombreUsuario,apellidoUsuario,pesoUsuario,nacimientoUsuario,mailUsuario,generoUsuario,dateFile,this)).execute();




	/*try {
			result = new SaveDatabase(id, filename,ruta,nombreUsuario,apellidoUsuario,pesoUsuario,nacimientoUsuario,mailUsuario,generoUsuario).execute().get();

			if (result.equalsIgnoreCase("KO")) {
				h.postDelayed(runn, 60 * 60 * 1000); // que intente cada hora el upload
				Log.v("aki", "aki_upload_KO");
			} else {
				Log.v("aki", "aki_upload_OK");
				lastupload = AppUtils.getDay(System.currentTimeMillis());
				prefs.edit().putString("lastupload", lastupload).commit();
			}

			return result;
		} catch (NotFoundException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return result;

	}

	private void writeMetro() {

		unavez = true;

		if (distance_metro != 0) {
			Log.v("aki", "aki_dentrodemetro");
			lastActivitiesArrayList.clear();
			lastActivitiesArrayList.add("metro");

			temp_distance = distance_metro;
			temp_duration = (loc2.getTime() - loc1.getTime()) / AppUtils.MILLISECONDS_PER_SECOND;
//			temp_duration = 410;

			temp_speed = (temp_distance * 3.6) / temp_duration;
		} else {
			temp_distance = temp_duration = (long) (temp_speed = 0);
		}

		if (pos2rev != -1) {
			int arraysize = arraymetro.size();
			int index = arraysize - pos2rev;
			subarray = new ArrayList<Location>(arraymetro.subList(pos2rev, arraymetro.size()));

			Collections.reverse(subarray);
			int subarraysize = subarray.size();
			for (int q = 0; q < index; q++)
				arraymetro.remove(arraysize - 1 - q);

			for (int t = 0; t < subarraysize; t++)
				arraymetro.add(subarray.get(t));
			pos2rev = -1;

		}

		segment = new Segment(this);
		segment.setFirstLocation(loc1);
		segment.setLastLocation(loc2);
		segment.setTotalDistance(temp_distance);
		segment.setLine(line);
		segment.setTotalDuration(temp_duration);
		segment.setTotalAverageSpeed(temp_speed);
		segment.setActivity("metro");
		segment.updateLocationPoints(arraymetro);

		segmentArrayList.add(segment);

		tmpLastLocation = loc2;
		// metro_detect = false;
		if (!arraymetro.isEmpty())
			arraymetro.clear();
	}

	private void postproc() {

		Log.v("aki", "aki_postproc");

		int jsonsize, siz;
		Location o, d;

		if (!segmentArrayList.isEmpty()) {
			jsonsize = segmentArrayList.size();

			// corregir vehicle-on_foot-vehicle -> HEMOS ENCONTRADO TRAFICO

			for (int i = 0; i < jsonsize;) {
				if (i + 2 < jsonsize)
					if (segmentArrayList.get(i).getActivity().equalsIgnoreCase("vehicle"))
						if (segmentArrayList.get(i + 1).getActivity().equalsIgnoreCase("on_foot")
								|| segmentArrayList.get(i + 1).getActivity().equalsIgnoreCase("still")
								|| segmentArrayList.get(i + 1).getActivity().equalsIgnoreCase("unknown"))
							if (segmentArrayList.get(i + 2).getActivity().equalsIgnoreCase("vehicle")) {

								// 3 segmentos se van a convertir en un solo
								// segmento vehicle con sus valores actualizados

								temp_duration = segmentArrayList.get(i).getTotalDuration()
										+ segmentArrayList.get(i + 1).getTotalDuration()
										+ segmentArrayList.get(i + 2).getTotalDuration();

								temp_distance = segmentArrayList.get(i).getTotalDistance()
										+ segmentArrayList.get(i + 1).getTotalDistance()
										+ segmentArrayList.get(i + 2).getTotalDistance();

								temp_speed = (segmentArrayList.get(i).getAverageSpeed()
										+ segmentArrayList.get(i + 1).getAverageSpeed() + segmentArrayList.get(i + 2)
										.getAverageSpeed()) / 3;

								segmentArrayList.get(i).setLastLocation(segmentArrayList.get(i + 2).getLastLocation());
								segmentArrayList.get(i).setTotalDuration(temp_duration);
								segmentArrayList.get(i).setTotalDistance(temp_distance);
								segmentArrayList.get(i).setTotalAverageSpeed(temp_speed);

								segmentArrayList.get(i).updateLocationPoints(
										segmentArrayList.get(i + 1).getLocationPoints());
								segmentArrayList.get(i).updateLocationPoints(
										segmentArrayList.get(i + 2).getLocationPoints());

								segmentArrayList.remove(i + 1);
								segmentArrayList.remove(i + 1); // volvemos a borrar la misma posición xk la i+2 se ha desplazado a i+1

								jsonsize = segmentArrayList.size(); // nuevo size ya que hemos borrado 2

								Log.v("aki", "aki_veh+foot/unk/stil+veh");

								continue; // continuamos sin modificar la "i"
							}
				i += 2; // /OJO
			}
            //juntar segmentos


		}

		// Mirar si el segmento vehicle se corresponde con el del BUS
		siz = segmentArrayList.size();

		for (int i = 0; i < siz; i++) { //DETECCION DE BUS
			String t;
			if (segmentArrayList.get(i).getActivity().equalsIgnoreCase("vehicle")) {
				if (segmentArrayList.get(i - 1).getActivity().equalsIgnoreCase("still")) { //si justo la anterior es still cogemos la primera del vehicle
					o = segmentArrayList.get(i).getFirstLocation();
				} else {
					o = segmentArrayList.get(i - 1).getFirstLocation(); //sino cogemos ésta como origen ya que al inicio puede detectar on_foot estando en el bus
				}
				d = segmentArrayList.get(i).getLastLocation();

				try {
					t = new GetBusorTram((o.getTime()) / 1000, (d.getTime()) / 1000).execute(
							new Double[] { o.getLatitude(), o.getLongitude(), d.getLatitude(), d.getLongitude() })
							.get();

					if (t.equalsIgnoreCase("BUS")) {
						segmentArrayList.get(i).setActivity("bus");
						continue;
					} else if (t.equalsIgnoreCase("TRAM")) {
						segmentArrayList.get(i).setActivity("tram");
						continue;
					}

				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (findRenfe(o.getLatitude(), o.getLongitude(), d.getLatitude(), d.getLongitude()))
					segmentArrayList.get(i).setActivity("renfe");
			}

		}
		writeSegment(ruta + AppUtils.getDay(System.currentTimeMillis()) + "location_segment.json", segmentArrayList);
	}

	private void calperday(String day) {

		Log.v("aki", "aki_calorias");

		int jsonsize;
		float totcal = 0;

		if (!segmentArrayList.isEmpty()) {
			jsonsize = segmentArrayList.size();

			for (int i = 0; i < jsonsize; i++)
				if (segmentArrayList.get(i).getActivity().equalsIgnoreCase("on_foot"))

					totcal += calories(segmentArrayList.get(i).getTotalDuration(), segmentArrayList.get(i)
							.getAverageSpeed());

			calo.edit().putFloat(day, totcal).commit();
		}
	}

	private void co2perday(String day) {

		Log.v("aki", "aki_co2");

		int jsonsize;
		double totco2 = 0;

		if (!segmentArrayList.isEmpty()) {
			jsonsize = segmentArrayList.size();

			for (int i = 0; i < jsonsize; i++)
				if (segmentArrayList.get(i).getActivity().equalsIgnoreCase("metro")
						|| segmentArrayList.get(i).getActivity().equalsIgnoreCase("bus")
						|| segmentArrayList.get(i).getActivity().equalsIgnoreCase("tram")
						|| segmentArrayList.get(i).getActivity().equalsIgnoreCase("renfe"))

					totco2 += co2calc(segmentArrayList.get(i).getTotalDistance());

			co2.edit().putFloat(day, (float) totco2).commit();

		}
	}

	private double calories(long duration, double speed) {

		double hours = duration / 3600.0;
		double parameter;
		if (speed < 2.7) {
			parameter = 2.3;
		} else {
			parameter = 2.9;
		}
		return parameter * Integer.parseInt(peso.getString("peso", "0")) * hours;
	}

	private double co2calc(double distance) {

		double dist = distance / 1000.0;
		// media de consumo de co2 de un coche= 140 gr / km
		return 140 * dist;
	}

	private boolean findRenfe(double lat_orig, double longi_orig, double lat_dest, double longi_dest) {

		if (nearstation(lat_orig, longi_orig) && nearstation(lat_dest, longi_dest)) {
			if (twopositionarray.get(0).contains(twopositionarray.get(1))) { // si tanto origen y destino pertenecen a la misma linea devolvemos true
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean nearstation(double lat, double longi) {

		double min = 100000;
		double dist;
		int min_index = 0;
		twopositionarray = new ArrayList<>(2); //en la posición 0 linea origen y en la 1 el destino

		getRenfeLocs();

		for (int i = 0; i < arrayrenfe.size(); i++) {

			dist = AppUtils.distanceCalculation(arrayrenfe.get(i).latitude, arrayrenfe.get(i).longitude, lat, longi);
			if (dist < min) {
				min = dist;
				min_index = i;
			}

		}
		Log.v("aki", "aki_min " + Integer.toString(min_index) + " " + Double.toString(min));

		if (min < 100) { // si estamos a menos de 100 metros(block level)
			twopositionarray.add(arrayrenfeline.get(min_index));
			return true;
		} else {
			return false;
		}
	}

	private void getRenfeLocs() {

		JSONArray jsonrenfe;
		arrayrenfe = new ArrayList<LatLng>();
		arrayrenfeline = new ArrayList<String>();

		try {
			JSONObject obj = new JSONObject(loadJSONFromAsset("renfe.json"));
			jsonrenfe = obj.getJSONArray("renfe");
			for (int i = 0; i < jsonrenfe.length(); i++) {

				arrayrenfe.add(new LatLng(jsonrenfe.getJSONObject(i).getDouble("lat"), jsonrenfe.getJSONObject(i)
						.getDouble("lon")));
				arrayrenfeline.add(jsonrenfe.getJSONObject(i).getString("line"));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getSessionID() {

		String s;
		KeyGenerator keyGenerator = null;
		SecretKey key;

		// Generate a 256-bit key
		final int outputKeyLength = 256;
		SecureRandom secureRandom = new SecureRandom();
		// Do *not* seed secureRandom! Automatically seeded from system   entropy.

		try {
			keyGenerator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyGenerator.init(outputKeyLength, secureRandom);
		key = keyGenerator.generateKey();

		s = Long.toString(System.currentTimeMillis()) + Double.toString(Math.random())
				+ Integer.toString(Process.myPid()) + key.toString();

		return s;
	}

	private String hash(String s) {

		try {
			// Create SHA-256 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private ArrayList<Integer> findMax(int _isOnfoot, int _isBicycle, int _isStill, int _isVehicle, int _isUnknown) {

		int maxValue;
		int[] values = { _isOnfoot, _isBicycle, _isStill, _isVehicle, _isUnknown };
		ArrayList<Integer> max = new ArrayList<Integer>(5);

		maxValue = Ints.max(values);

		for (int i = 0; i < 5; i++) {
			if (values[i] == maxValue) {
				max.add(i);
			}
		}
		return max;
	}

    class myPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            SignalStrength = signalStrength.getGsmSignalStrength();
            SignalStrength = (2 * SignalStrength) - 113; // -> dBm
            //Log.v("aki", "cell power changed: "+SignalStrength);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("cellPower",SignalStrength);
            editor.commit();
        }

    }

}


