package com.mobi.mobilitapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mobi.mobilitapp.ActivityRecognitionML.ActivityRecognitionMLService;
import com.mobi.mobilitapp.AutomaticRecognition.Vector;
import com.mobi.mobilitapp.Capture.ActivityRecognizedImplementation;
import com.mobi.mobilitapp.Capture.BTStatusChecker;
import com.mobi.mobilitapp.Capture.LocationFinder;
import com.mobi.mobilitapp.Capture.Sensor5seconds;
import com.mobi.mobilitapp.Capture.SensorLoger;
import com.mobi.mobilitapp.Capture.SensorSample;
import com.mobi.mobilitapp.Capture.Sensors;
import com.mobi.mobilitapp.Capture.WifiStatusChecker;
import com.mobi.mobilitapp.CrashDetection.DifferentTypes.PreferencesTypes;
import com.mobi.mobilitapp.CrashDetection.HandlersAndServices.AccelerometerServiceListener;
import com.mobi.mobilitapp.StepCounter.PreferencesTypesStepCounter;
import com.mobi.mobilitapp.StepCounter.StepCounterActivity;
import com.mobi.mobilitapp.Uploading.UploadService;
import com.mobi.mobilitapp.components.ActivityAlertDialog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

//useful to change locale manually
import java.util.Locale;
import android.content.res.Configuration;
import android.widget.Toast;
import android.preference.PreferenceManager;
import java.lang.String;
import android.preference.ListPreference;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;
import android.os.Handler;

//USEFUL FOR CHARTS (ANDREA AND JADY)
import java.util.stream.*;

/*interface ConnectionBooleanChangedListener {
	public void OnMyBooleanChanged();
}*/

public class MainActivity extends ActionBarActivity implements View.OnClickListener, CustomResultReceiver.AppReceiver {

//	static public String ANDROID_ID;

	static public SensorLoger sensorLoger; // 17.4.2017 - Rok SLamek
	static public Sensor5seconds mSensor; // 	29/09/17 - Fran Medina

    static public ActivityRecognizedImplementation activityRecognizedImplementation; // 26/10/2017 - Mahmoud Elbayoumy
    static public ActivityRecognitionMLService activityRecognitionMLService; // Rigo
    static public LocationFinder locationFinder; // 26/10/2017  - Mahmoud Elbayoumy
    static public WifiStatusChecker wifiStatusChecker; // 27/10/2017  - Mahmoud Elbayoumy
	static public BTStatusChecker btStatusChecker; // 28/10/2017  - Mahmoud Elbayoumy
	//static public CellularStatusChecker cellularStatusChecker; //29/10/2017 - Mahmoud Elbayoumy
    public static boolean capturing; // 30/10/2017  - Mahmoud Elbayoumy
    WifiReceiver wifiReceiver;

    public String className = "Nothing to say...";
	public static Sensors mSensors;
	public Vector vector;

	// Fran (start)
	private static final String TAG = "MyTAG";
	// Fran (end)
	Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private DrawerAdapter adapter;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private CustomResultReceiver resultReceiver;
	List<Item> drawvalues;
	SharedPreferences prefsgoogle, prefsface, prefs,prefs2;
	Handler h;
	boolean showlogin;
    private ShowcaseView showcaseView;
    private int contador=0;
    private Target t1,t2,t3;

    private GCMClientManager pushClientManager;
    String PROJECT_NUMBER = "443133651895",id="";
    String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

	ListView listview = null;

	Intent crashDetectionService;

	static Context c;

	int[] colors = { 0, 0xFFFF0000, 0 }; // red

    MainFragment frag;


    public static String datas_results;

	/* Check granted permissions - Mahmoud Elbayoumy 21/10/2017*/
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
				}
				else
				{
					Log.d("BAYO", "User rejected to give permissions");
					closeNow();
				}
				break;
		}

	}

//    LocationFinder locationFinder = new LocationFinder();
public static final String FILEPATHPARENT = Environment
		.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
		.getAbsolutePath() + "/MobilitApp";

	public static final String FILEPATH = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			.getAbsolutePath() + "/MobilitApp/sensors";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//update language at starting up
		update_language(Boolean.TRUE);



		/* Ask for Permissions - Mahmoud Elbayoumy 21/10/2017 */

        // If device is running SDK >= 23

        //if (Build.VERSION.SDK_INT >= 23) {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                //|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
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
                  //  Manifest.permission.READ_PHONE_STATE,
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
        //}

        Log.v("aki", "aki_onCreateMainAct");

		setContentView(R.layout.activity_main);

   		/*	For Logging Errors in remote devices - Mahmoud Elbayoumy 31/10/2017	*/
        createDebugFiles(true); // if you put the input true log files will be created in Mobilitapp/sensors and uploaded with rest of files to the server, else put it false to prevent log files creation


        /* Delete JSON files - !! */
        File dirParent = new File(FILEPATHPARENT);
        if (!dirParent.exists()) dirParent.mkdirs();
        File[] filesParent = dirParent.listFiles();
        if(filesParent != null) {
            for (int i = 0; i < filesParent.length; i++) {
                if (filesParent[i].isFile()) {
                    filesParent[i].delete();
                }
            }
        }

        /* upload files */
        File dir = new File(FILEPATH);
        if (!dir.exists()) dir.mkdir();
        wifiReceiver = new WifiReceiver();
        wifiReceiver.uploadOnWifiConnected(this);

        if(!Python.isStarted()){
			Python.start(new AndroidPlatform(this)); // 11.4.2019 - Rigoberto Elias Ramirez
		}

        toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setBackgroundColor(Color.GRAY);


		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container_main, new MainFragment(), "main_tag")
					.commit();
		}

        BroadcastReceiver br = new MyBroadcastReceiver();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(br, filter);
		/*
		try{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(R.string.develop)
					.setMessage(getString(R.string.develop_msg))
					.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) -> {
						//SelectTransportFragment selectTransportFragment = new SelectTransportFragment();
						//selectTransportFragment.show(getSupportFragmentManager(),"select_transport");
						dialog.cancel();
                            })

					.setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) ->
							dialog.cancel())
					.show();
		}catch (Exception e){
			Log.v(TAG, "Error con el Dialog");
		}
		*/

		getSupportFragmentManager().executePendingTransactions();

		prefsgoogle = getSharedPreferences("google_login", Context.MODE_PRIVATE);
		prefsface = getSharedPreferences("face_login", Context.MODE_PRIVATE);
		prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        prefs2 = getSharedPreferences("com.mobi.mobilitapp", Context.MODE_PRIVATE);

		registerService();

		if (prefs.getString("login", "default").equalsIgnoreCase("never")) {
			showlogin = false;
		} else {
			showlogin = true;
		}

		/* diable login - Mahmoud Elbayoumy 11/4/2017 */
        showlogin = false;

        if (prefs.getString("id", "no_id").equalsIgnoreCase("no_id")) {
            id = hash(getSessionID());
            prefs.edit().putString("id", id).commit();


        } else {
            id = prefs.getString("id", "no_id");
        }

		//------------------------------------------------------------------------------------------------------------
		if (prefs.getString(PreferencesTypes.switchCrashOnOff.toString(), "no_selection")
				.equalsIgnoreCase("no_selection")) {
			prefs.edit().putString(PreferencesTypes.switchCrashOnOff.toString(), PreferencesTypes.off.toString()).commit();
		}

		if (prefs.getString(PreferencesTypes.sensitivityCrashMaximumThreshold.toString(), "no_selection")
				.equalsIgnoreCase("no_selection")) {
			prefs.edit().putString(PreferencesTypes.sensitivityCrashMaximumThreshold.toString(), "16").commit();
		}

		if(prefs.getString(PreferencesTypes.timeAtLastLocation.toString(), "no_time")
				.equalsIgnoreCase("no_time")){
			prefs.edit().putString(PreferencesTypes.timeAtLastLocation.toString(),"0").commit();
		}

		if(prefs.getString(PreferencesTypes.latitudeAtLastLocation.toString(), "no_latLoc")
				.equalsIgnoreCase("no_latLoc")){
			prefs.edit().putString(PreferencesTypes.latitudeAtLastLocation.toString(),"0").commit();
		}

		if(prefs.getString(PreferencesTypes.longitudeAtLastLocation.toString(), "no_lonLoc")
				.equalsIgnoreCase("no_lonLoc")){
			prefs.edit().putString(PreferencesTypes.longitudeAtLastLocation.toString(),"0").commit();
		}

		if(prefs.getString(PreferencesTypes.switchSmsOnOff.toString(), PreferencesTypes.no_selection.toString())
				.equalsIgnoreCase(PreferencesTypes.no_selection.toString())){
			prefs.edit().putString(PreferencesTypes.switchSmsOnOff.toString(),PreferencesTypes.off.toString()).commit();
		}

		if(prefs.getString(PreferencesTypes.on.toString(), PreferencesTypes.on.toString())
				.equalsIgnoreCase(PreferencesTypes.on.toString())){
			prefs.edit().putString(PreferencesTypes.on.toString(),PreferencesTypes.on.toString()).commit();
		}

		if(prefs.getString(PreferencesTypes.off.toString(), PreferencesTypes.on.toString())
				.equalsIgnoreCase(PreferencesTypes.off.toString())){
			prefs.edit().putString(PreferencesTypes.off.toString(),PreferencesTypes.off.toString()).commit();
		}

		if(prefs.getString(PreferencesTypes.emergencyPhoneNumberSms.toString(), "")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypes.emergencyPhoneNumberSms.toString(),"").commit();
		}

		if(prefs.getString(PreferencesTypes.emergencyPhoneNameSms.toString(), "")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypes.emergencyPhoneNumberSms.toString(),"").commit();
		}

		if(prefs.getString(PreferencesTypes.medical_info.toString(), "")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypes.medical_info.toString(),"").commit();
		}

		if(prefs.getString(PreferencesTypesStepCounter.stepCounter.toString(),PreferencesTypesStepCounter.off.toString())
				.equalsIgnoreCase(PreferencesTypesStepCounter.off.toString())){
			prefs.edit().putString(PreferencesTypesStepCounter.stepCounter.toString(),
					PreferencesTypesStepCounter.off.toString()).commit();
		}

		if(prefs.getString(PreferencesTypesStepCounter.totalSteps.toString(),"")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypesStepCounter.totalSteps.toString(),
					"0").commit();
		}

		if(prefs.getString(PreferencesTypesStepCounter.todaySteps.toString(),"")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypesStepCounter.todaySteps.toString(),
					"0").commit();
		}

		if(prefs.getString(PreferencesTypesStepCounter.todayDataTime.toString(),"")
				.equalsIgnoreCase("")){
			prefs.edit().putString(PreferencesTypesStepCounter.todayDataTime.toString(),
					PreferencesTypesStepCounter.noDataTime.toString()).commit();
		}

		if(prefs.getString(PreferencesTypesStepCounter.lastCounterStepsFromSensor.toString(),"")
				.equalsIgnoreCase("0")){
			prefs.edit().putString(PreferencesTypesStepCounter.lastCounterStepsFromSensor.toString(),
					"0").commit();
		}

		if(prefs.getString(PreferencesTypes.switchCrashOnOff.toString(), PreferencesTypes.no_selection.toString())
				.equalsIgnoreCase(PreferencesTypes.on.toString())){
			startService(new Intent(this, AccelerometerServiceListener.class));
		}



		//------------------------------------------------------------------------------------------------------------

		drawercontent();
		adapter = new DrawerAdapter(this, drawvalues);

		mTitle = mDrawerTitle = getTitle();

		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
		mDrawerList.setDividerHeight(1);

		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.abrir, R.string.cerrar) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle.syncState();

		c = this;
		h = new Handler();



    	pushClientManager = new GCMClientManager(this, PROJECT_NUMBER);
    	pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
		@Override
		public void onSuccess(String registrationId, boolean isNewRegistration) {


			//Toast.makeText(MainActivity.this, registrationId,
			//  Toast.LENGTH_SHORT).show();
			// SEND async device registration to your back-end server
			// linking user with device registration id
			// POST https://my-back-end.com/devices/register?user_id=123&device_id=abc


/*			try {
				String result = new SaveDatabase(ruta, registrationId, prefs.getString("id", "no_id"), MainActivity.this).execute().get();

				if (result.equalsIgnoreCase("OK")) {
					//Toast.makeText(MainActivity.this, "Saved RegId", Toast.LENGTH_LONG).show();

				} else {
					//Toast.makeText(MainActivity.this, "Failed RegId", Toast.LENGTH_LONG).show();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}*/


		}

		@Override
		public void onFailure(String ex) {
			super.onFailure(ex);
			// If there is an error registering, don't just keep trying to register.
			// Require the user to click a button again, or perform
			// exponential back-off when retrying.
		}




	});

        frag = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_tag");



		//getSupportFragmentManager().beginTransaction().add(R.layout.transportfragment,new SelectTransportFragment(),"selected_transport").commit();

		//Shows a Dialog List which allows the user to select between 4 means of transportation
		//SelectTransportFragment transportselector = new SelectTransportFragment();
		//transportselector.show(getSupportFragmentManager(), "");

/*
		getSupportFragmentManager().beginTransaction().add(R.layout.transportfragment,new SelectTransportFragment() ,"transport tag");

		String transport = sharedPref.getString(getString(R.string.transport_selected),null);
*/
/*
		listview = new ListView(this);

		String [] mtransports = {"Car","Motorbike","Metro","Bus","Train","Other"};

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.transportfragment,R.id.txtitem,mtransports);

		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				ViewGroup vg = (ViewGroup) view;
				TextView txt = (TextView)vg.findViewById(R.id.txtitem);
				Toast.makeText(MainActivity.this,txt.getText().toString(),Toast.LENGTH_LONG).show();
			}
		});

*/

	//	if(transport != null){
	//		Log.v(TAG,"El transporte es: "+transport);
	//	}
	//	else{
	//		Log.v(TAG,"Basura pa tu boca");
	//	}

		//sensorLoger = new SensorLoger(this); // 17.4.2017 - Rok SLamek
		mSensor = new Sensor5seconds(this); // 29/09/2017 - Fran Medina
		mSensors = new Sensors(this);
		vector = new Vector();

        activityRecognizedImplementation = new ActivityRecognizedImplementation(this); //26/10/2017 - Mahmoud Elbayoumy
        activityRecognitionMLService = new ActivityRecognitionMLService();
        locationFinder = new LocationFinder(MainActivity.this); //26/10/2017 - Mahmoud Elbayoumy
        wifiStatusChecker = new WifiStatusChecker(this); // 27/10/2017  - Mahmoud Elbayoumy
		btStatusChecker = new BTStatusChecker(this);  // 28/10/2017  - Mahmoud Elbayoumy
		//cellularStatusChecker = new CellularStatusChecker(this); // 29/10/2017 - Mahmoud Elbayoumy
        capturing = false; // 30/10/2017 - Mahmoud Elbayoumy
    }

	public void showDialogListView(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setPositiveButton("OK",null);
		builder.setView(listview);
		AlertDialog dialog = builder.create();
		dialog.show();

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
                + Integer.toString(android.os.Process.myPid()) + key.toString();

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

    static public Menu menu;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
        this.menu = menu;

        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;

		if (itemId == R.id.prf) {

			Intent i = new Intent(this, PrefAct.class);
			startActivityForResult(i, 0);



			return true;

		}

		else if (itemId == R.id.stepCounter) {

			Intent i = new Intent(this, StepCounterActivity.class);
			startActivityForResult(i, 0);

			return true;
		}

		// Botton to send data to server // Fran
		else if (itemId == R.id.sendData) {

			Intent intent = new Intent(this, UploadService.class);
			// Put some data for use by the IntentService
			intent.putExtra("UP", "Uploading...");
			startService(intent);

			return true;
		}

		else if (itemId == R.id.exit) {

			frag.exitAll();
			getSupportFragmentManager().beginTransaction().remove(frag).commit();
			btStatusChecker.destroy();
            locationFinder.destroyLocationFinder();
			wifiStatusChecker.destroy();
            activityRecognizedImplementation.destroy();
			activityRecognitionMLService.onDestroy();
			sensorLoger.discardCapture();
			finish();
			return true;

		}

		// 29/09/17 - Fran Medina - Adding the button to gather 5s of sensor
        /*else if (itemId == R.id.add) {

			this.setFinishOnTouchOutside(true);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Wait for the result");
			builder.setMessage("5 seconds remaining");
			builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//do things
				}
			});
			builder.show();
			//className = mSensor.startCapturing();
			mSensors.newCapture();
			vector.vectorCSV();

			Toast toast = Toast.makeText(MainActivity.this, className, Toast.LENGTH_LONG);
			toast.show();

            return true;
        }*/
        /////////////////////////////////////////////////////////////////////

		// 3.4.2017 - Rok Slamek
		//
		else if (itemId == R.id.addActivity) {

            //Log.d("BAYO ID", Integer.toString(itemId))



			if (capturing){ //equivalent to sensorLoger.isCaptureRunning()
				Intent intentML = new Intent(c , ActivityRecognitionMLService.class);
				sensorLoger.stopCapture();
				item.setIcon(android.R.drawable.ic_media_play);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Captured activity");

				builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(
								getString(R.string.preference_file_key), Context.MODE_PRIVATE);
						int captureIDCurrent = sharedPref.getInt("captureIDCurrent", 1);

						/* Cell part */
						//cellularStatusChecker.destroy(); included in wifi part
						wifiStatusChecker.writeToFileCell(captureIDCurrent,SelectTransportFragment.selection);

						/* BT part */
						btStatusChecker.destroy();
						btStatusChecker.writeToFile(captureIDCurrent,SelectTransportFragment.selection);

						/* WiFi part */
						wifiStatusChecker.destroy();
						wifiStatusChecker.writeToFile(captureIDCurrent,SelectTransportFragment.selection);

						/* AR Part */
						activityRecognizedImplementation.destroy();
						activityRecognizedImplementation.writeToFile(MainActivity.this, captureIDCurrent, SelectTransportFragment.selection);

						/* AR ML */
						List<SensorSample> sensorSampleList = sensorLoger.getAccelerometerData();



						int number_intervals = (sensorSampleList.size()/400) + 1;
						Float[][] matrix = new Float[number_intervals][3];
						matrix = createMatrix(sensorSampleList, matrix);

						Bundle bundle = new Bundle();
						bundle.putSerializable("data_matrix", matrix);

						intentML.setAction(Intent.ACTION_NEW_OUTGOING_CALL);
						//intentML.setAction(Intent.ACTION_ATTACH_DATA);

						intentML.putExtra("receiver", resultReceiver);
						intentML.putExtras(bundle);
						//intentML.putParcelableArrayListExtra("accelerometerData", (ArrayList<? extends Parcelable>) sensorSampleList);

						datas_results = "";
						startService(intentML);
						//startActivityForResult(intentML, 100);

						/*//wait until datas_results is filled
						while(datas_results.equals("")){

						}*/





						Runnable r = new Runnable() {
							@Override
							public void run(){
								if (datas_results.equals("") == false){

									String RESULTSSSS = datas_results;

								}


								//added for charts by ANDREA AND JADY
								PieChart myPieChart;
								myPieChart = (PieChart) findViewById(R.id.idPieChart);
								Description myDescription = new Description();
								myDescription.setText("Probabilities for each transportation mean");
								myPieChart.setDescription(myDescription);
								myPieChart.setRotationEnabled(true);

								int [] yData = {10,2,1,54,5,1,3,4,2,3};
								String [] xData = {"A", "B", "C", "D","E", "F", "G", "H", "I", "J"} ;



								Button button = (Button) findViewById(R.id.back_to_main);

								myPieChart.setVisibility(View.VISIBLE);
								button.setVisibility(View.VISIBLE);


								button.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										//set the piechart NON visible
										myPieChart.setVisibility(View.GONE);
										button.setVisibility(View.GONE);
									}
								});

								if (datas_results.length() >0) {
									//split whole_datas to a list
									String[] whole_datas = datas_results.split(" ");

									int data_index = 0;
									int number_of_transportations = 10;



									//static datas for piechart tests
									/*int [] yData = {10,2,1,54,5,1,3,4,2,3};
									String [] xData = {"Bicycle", "Bus", "Car", "Metro","Motorbike", "Run", "Stationary", "Train", "Tram", "Walk"} ;
									*/

									//real datas
									xData = new String[number_of_transportations];
									yData = new int[number_of_transportations];

									//retrieves X (labels) and Y (values)
									for (int i = 0; i < whole_datas.length; i = i + 2) {
										xData[data_index] = whole_datas[i];
										yData[data_index] = Integer.valueOf(whole_datas[i + 1]);
										data_index++;
									}


								}

								addDataSet(yData, xData, myPieChart);



								/* For GPS part - Mahmoud Elbayoumy 24/10/2017 */
								locationFinder.destroyLocationFinder();
								locationFinder.saveLocationFinderData(captureIDCurrent, SelectTransportFragment.selection);

								/* sensors part */
								sensorLoger.saveCapture();
								sensorLoger.discardCapture();
								capturing = false;

							}
						};

						Handler h = new Handler();
						h.postDelayed(r, 5000); // <-- the "1000" is the delay time in miliseconds.









					}

			});

				builder.setNegativeButton("DISCARD", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						/* Cell part */
						//cellularStatusChecker.destroy(); included in wifi part

						/* BT part */
						btStatusChecker.destroy();

                        /* WiFi part */
                        wifiStatusChecker.destroy();

                        /* AR Part */
                        activityRecognizedImplementation.destroy();

                        /* GPS Part */
                        locationFinder.destroyLocationFinder();

                        /* Sensor part */
                        sensorLoger.discardCapture(); // we lose all the datas

                        dialog.cancel();
						capturing = false;
					}
				});

				builder.show();

			} else {

				SelectTransportFragment selectTransportFragment = new SelectTransportFragment(); //capturing becomes true if a mean of transportation is seleected
				selectTransportFragment.show(getSupportFragmentManager(), "select_transport");
				sensorLoger = new SensorLoger(this);
			}
			return true;
			//
			// 3.4.2017
			//
		}

		else {
			return super.onOptionsItemSelected(item);
		}
	}





	private void drawercontent() {

		String nombre;
		nombre = prefsgoogle.getString("nombre", getString(R.string.pulsa));
		drawvalues = new ArrayList<Item>();

		if (nombre.equalsIgnoreCase(getResources().getString(R.string.pulsa))) {
			nombre = prefsface.getString("nombre", getResources().getString(R.string.pulsa));
		} else if (nombre.equalsIgnoreCase(getResources().getString(R.string.pulsa))) {
			nombre = getResources().getString(R.string.pulsa);
		}

		if (!showlogin) {
			//nombre = "MobilitApp";
			nombre = "Menu";
		}
		drawvalues.add(new Header(this, nombre));
		drawvalues.add(new ItemList(this, R.string.show_location_history));
		drawvalues.add(new ItemList(this, R.string.graph));
		drawvalues.add(new ItemList(this, R.string.clean));

        /* Mahmoud Elbayoumy - 11/4/2017
		if (showlogin) {
				drawvalues.add(new ItemList(this, R.string.perfil));
		} else{
            Log.v("session:",R.string.sesion+"");
            drawvalues.add(new ItemList(this, R.string.sesion));
        }*/

		drawvalues.add(new ItemList(this, R.string.traffic));
        drawvalues.add(new ItemList(this, R.string.help));


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

			case 69:
				if (resultCode == Activity.RESULT_OK) {
					Log.v("aki", "aki_antesderead_main");
					frag.drawRoutes(data.getStringExtra("archivo"));
				}
				break;

			case 12:
				Log.v("aki", "aki_actualizardrawerBAD");
				if (resultCode == Activity.RESULT_OK) {
					Log.v("aki", "aki_actualizardrawerOK");
					drawercontent();
					adapter = new DrawerAdapter(this, drawvalues);
					mDrawerList.setAdapter(adapter);
				}
				break;

			case 70:
				if (resultCode == Activity.RESULT_OK) {
					Log.v("aki", "aki_Traffic");
					//frag.drawTraffic(data.getStringExtra("archivo"));
				}
				break;

		}
	}

    @Override
    public void onClick(View v) {
        Log.v("aki", "contador"+contador);

        switch (contador){
            case 0:
                showcaseView.setShowcase(t1,true);
                showcaseView.setContentTitle("Preferencias");
                showcaseView.setContentText("Ver Preferencias");
                break;
            case 1:
                showcaseView.setShowcase(t2,true);
                showcaseView.setContentTitle("Apagar");
                showcaseView.setContentText("Ver Apagar");
                break;
            case 2:
                showcaseView.setShowcase(t3,true);
                showcaseView.setContentTitle("Main Menu");
                showcaseView.setContentText("Ver Main Menu");
                break;
            case 3:
                showcaseView.hide();
                break;
        }
        contador++;

    }

    public class DrawerItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
			switch (position) {
/*
                case 0:
                    mDrawerLayout.closeDrawers();
                    Intent log = new Intent(MainActivity.c, LoginActivity.class);
                    startActivityForResult(log, 12);

                    break;
*/
                case 1:

                    Intent intent = new Intent(MainActivity.c, History.class);
                    startActivityForResult(intent, 69);
//				mDrawerLayout.closeDrawers();
                    closedrawer();
                    break;

                case 2:

                    mDrawerLayout.closeDrawers();
                    Intent in = new Intent(MainActivity.c, GraphActivity.class);
                    startActivity(in);

                    break;

                case 3:

                    mDrawerLayout.closeDrawers();
                    ((MainFragment) frag).cleanMap();
                    break;
/*
                case 4:
//				mDrawerLayout.closeDrawers();
                    Intent log2 = new Intent(MainActivity.c, LoginActivity.class);
                    startActivityForResult(log2, 12);
                    closedrawer();
                    break;
*/
                case 4:
                //case 5:

/*				Intent intentT = new Intent(MainActivity.c, Traffic.class);
				startActivityForResult(intentT, 70);
//				mDrawerLayout.closeDrawers();
				closedrawer();
				break;
				*/
                    mDrawerLayout.closeDrawers();
                    ((MainFragment) frag).drawTraffic();
                    break;

                //case 6:
                case 5:

                    //help
                    /*Intent help = new Intent(MainActivity.c, Help.class);
                    startActivity(help);
					*/
                    break;
                /*
                contador=0;
                showcaseView.show();
                showcaseView.setTarget(Target.NONE);
                showcaseView.setContentTitle("Help");
                showcaseView.setContentText("Ver Help");
                showcaseView.setButtonText("Next");

                */


            }

		}
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, AccelerometerServiceListener.class));
		sensorLoger.discardCapture();
		super.onDestroy();
		if(resultReceiver != null) {
			resultReceiver.setAppReceiver(null);
		}
		Log.v("aki", "aki_onDestroyMainAct");
	}

	public void resume() {
		stopService(new Intent(this, AccelerometerServiceListener.class));

		if(prefs.getString("switchCrashOnOff", "no_selection").equalsIgnoreCase("on")){
			startService(new Intent(this, AccelerometerServiceListener.class));
		}

		if (!prefsface.getString("nombre", "Pulsa para iniciar sesi\u00f3n").equalsIgnoreCase("Pulsa para iniciar sesi\u00f3n")) {
			Log.v("aki", "aki_ANTESDEDRAWER");
			drawercontent();
			adapter = new DrawerAdapter(this, drawvalues);
			mDrawerList.setAdapter(adapter);
		}
	}

	private void closedrawer() {
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				mDrawerLayout.closeDrawers();

			}
		}, 20);

	}

	/* Close Application - Mahmoud Elbayoumy 22/10/2017 */
	public void closeNow() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			finishAffinity();
		}

		else
		{
			finish();
		}
	}

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    public void createDebugFiles(boolean decide){
        if(decide) {
          //Log.d("BAYO", "logging 1");
            if (isExternalStorageWritable()) {
                //Log.d("BAYO", "logging 2");
               // if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                 //       != PackageManager.PERMISSION_GRANTED) {
                   // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                //} else {
                    //TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

                    File appDirectory = new File(FILEPATHPARENT);
                    File logDirectory = new File(FILEPATH);
                    //File logFile = new File(logDirectory, "logcat_" + tm.getDeviceId() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".log");
                    File logFile = new File(logDirectory, "logcat_" + ANDROID_ID + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".log");

                    // create app folder
                    if ( !appDirectory.exists() ) {
                        appDirectory.mkdir();
                    }

                    // create log folder
                    if (!logDirectory.exists()) {
                        logDirectory.mkdir();
                    }

                    // clear the previous logcat and then write the new one to the file
                    try {
                        Process process = Runtime.getRuntime().exec("logcat -c");
                        process = Runtime.getRuntime().exec("logcat -f " + logFile);
                        //process = Runtime.getRuntime().exec( "logcat -f " + logFile + " *:S MyActivity:D MyActivity2:D");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (isExternalStorageReadable()) {
                // only readable
            } else {
                // not accessible
            }
        //}
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
    void showDialog(String message) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = ActivityAlertDialog.newInstance(message);
        newFragment.show(ft, "dialog");
    }
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		/*
		 * Step 3: Handle the results from the intent service here!
		 * */
		Bundle b = resultData;
		String data = b.getString("activityML");
		if(data != null){
			showDialog(data);
		}

	}

	public void update_language(Boolean on_create_call){
		//chosen language
		String currValue=PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("language_key","(Auto)");
        String the_language_short_version;

		//manually change locale if it's not AUTO
		if (currValue.equals("(Auto)") == false ) {
			the_language_short_version = currValue.split("[\\(\\)]")[1];
			Toast.makeText(MainActivity.this, the_language_short_version, Toast.LENGTH_LONG).show();
		}else{
			//set BACK language to AUTO
            the_language_short_version = Locale.getDefault().getISO3Language() ;
		}

		Locale locale = new Locale(the_language_short_version);
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


        //we restart ONLY if we update language from preferences
		if (on_create_call==Boolean.FALSE){
			recreate();
		}

	}
	private void registerService() {
		resultReceiver = new CustomResultReceiver(new Handler(), this);

	}

	private Float[][] createMatrix(List<SensorSample> data, Float[][] matrix) {
		Float[][] matrixAux = matrix;
		int interval = 400;
		int number_intervals = (int) Math.ceil(data.size()/interval)+1 ;
		int index ;
		for (int j = 0 ; j < number_intervals; j++){
			Float[] x ;
			Float[] y ;
			Float[] z ;
			if ((j == 0 && data.size() < interval )) {
				x = new Float[data.size()];
				y = new Float[data.size()];
				z = new Float[data.size()];
				index = data.size();

			}else if ( ( j > 0 && (data.size() < (j+1)* interval))){
				x = new Float[data.size()- j*interval];
				y = new Float[data.size()- j*interval];
				z = new Float[data.size()- j*interval];
				index = data.size()- j*interval;

			}else {
				x = new Float[interval];
				y = new Float[interval];
				z = new Float[interval];
				index = interval;
			}
			for (int l= 0 ; l < index ; l++){
				x[l]= data.get(j * interval +  l).getX();
				y[l]= data.get(j * interval +  l).getY();
				z[l]= data.get(j * interval +  l).getZ();
			}
			matrix[j][0] = this.mean(x);
			matrix[j][1] = this.mean(y);
			matrix[j][2] = this.mean(z);
		}
		return matrixAux;
	}

	private static float mean(Float[] m){
		float result = 0;
		for (int i = 0 ; i < m.length ; i++){
			result += m[i];
		}
		return result / m.length;
	}

	//useful for charts by ANDREA AND JADY
	private void addDataSet(int[] yData, String[] xData, PieChart myPieChart){
    	ArrayList<PieEntry> yEntrys = new ArrayList<>();
		ArrayList<String> xEntrys = new ArrayList<>();

		//sum of values in yData
		int sum_of_y = 0;
		for (int i = 0 ; i < yData.length; i ++) {
			sum_of_y = sum_of_y + yData[i];
		}

		//initialize the new table of yData as percentages
		float[] yData_percentages = new float[yData.length];

		//computes percentages
		for (int i = 0 ; i < yData.length; i ++){
			yData_percentages[i] =  (float)    ((100 * yData[i]) / sum_of_y);
			//yEntrys.add(new PieEntry(yData[i],i));
			yEntrys.add(new PieEntry((float)yData_percentages[i],i));

		}

		for (int i = 1 ; i < xData.length; i ++){
			xEntrys.add(xData[i]);
		}

		//creates the piechart
		PieDataSet myPieDataSet = new PieDataSet(yEntrys,"Means of transportation");

		//no text in the center of the pie chart
		myPieChart.setCenterTextRadiusPercent(0);
		myPieChart.setHoleRadius(0);
		myPieChart.setDrawHoleEnabled(false);

		//setting colors of the piechart
		ArrayList<Integer> myColors = new ArrayList<>();
		myColors.add(Color.GRAY);
		myColors.add(Color.rgb(153,102,51)); //brown
		myColors.add(Color.RED);
		myColors.add(Color.rgb(204,255,153)); //green
		myColors.add(Color.CYAN);
		myColors.add(Color.YELLOW);
		myColors.add(Color.MAGENTA);
		myColors.add(Color.rgb(220,220,220)); //light grey
		myColors.add(Color.BLUE);
		myColors.add(Color.rgb(255,153,0)); //orange
		myPieDataSet.setColors(myColors);

		//adding legends to the piechart
		Legend myLegend = myPieChart.getLegend();
		myLegend.setEnabled(true);
		myLegend.setWordWrapEnabled(true);
		myLegend.setTextSize(30f);

		//legends = name of transportation mean + a color
		List<LegendEntry> entries = new ArrayList<>();

		for (int i = 0; i < xData.length; i++) {
			LegendEntry entry = new LegendEntry();
			entry.formColor = myColors.get(i);
			entry.label = xData[i];
			entries.add(entry);
		}
		myLegend.setCustom(entries);


		//myLegend.setForm(Legend.LegendForm.CIRCLE);

		//create pie data object
		PieData myPieData = new PieData(myPieDataSet);
		myPieData.setValueTextSize(10f);
		myPieChart.setData(myPieData);
		myPieChart.invalidate();
	}


	public interface VariableChangeListener {
		public void onVariableChanged(Object... variableThatHasChanged);
	}

}
