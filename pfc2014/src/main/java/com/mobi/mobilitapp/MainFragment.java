package com.mobi.mobilitapp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.koushikdutta.ion.Ion;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class MainFragment extends Fragment implements OnMapReadyCallback {

	private LinearLayout ll,ll2;
	public GoogleMap googleMap;
	SharedPreferences prefsgoogle, prefsface, prefspeso, prefs;
	Context c;
	float f;
	WifiManager wifi;
	Locale spanish;
	boolean conn = false;
	String[] valors = { "", "", "" };
    String[] valors3 = { "", "", "","","","","" };
	String[] oho,leyenda;

	String[] valors2 = { "" };
	String[] oho2 = { " " };
	boolean isGPSEnabled, isNETWORKEnabled;
	LocationManager locationManager;
	private Segment marker;
	private TextView tV;
	private ImageView iV,iV2;
	private GridView hlv, hlv2,hlv3;
	private Requester mrequesterLOC, mrequesterAR;
	private Remover mremoverLOC, mremoverAR;
    private AudioManager mAudioManager ;

	//LatLngBounds that includes BARCELONA
	//private LatLngBounds BARCELONA = new LatLngBounds(new LatLng(41.452361, 2.288246), new LatLng(41.341102, 2.065086));


	public enum REQUEST_TYPE {
		START, STOP
	}

	public ArrayList<Segment> segments = new ArrayList<Segment>();
	public ArrayList<Segment> markers = new ArrayList<Segment>();
	public HashMap<Marker, Segment> hmap = new HashMap<Marker, Segment>();

	boolean first_location_history = true;
	JSONArray segmentsData = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.mainfragment, container, false);

		return rootView;
	}

    @Override
    public void onPause(){
        getActivity().unregisterReceiver(onComplete);
        super.onPause();

    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//inicio de activities que aparecen cuando se da clic en el boton de actividad.
		ll = (LinearLayout) getActivity().findViewById(R.id.troll);
		tV = (TextView) getActivity().findViewById(R.id.name);
		iV = (ImageView) getActivity().findViewById(R.id.image);
        iV2 = (ImageView) getActivity().findViewById(R.id.image2);
		hlv = (GridView) getActivity().findViewById(R.id.gridView1);
		hlv.setVisibility(View.GONE);
        ll.setVisibility(View.GONE);

//inicio de activities que aparecen cuando se muestra el tráfico
        ll2 = (LinearLayout) getActivity().findViewById(R.id.troll2);
      //  hlv3 = (GridView) getActivity().findViewById(R.id.gridViewTraffic);
        //hlv3.setVisibility(View.INVISIBLE);

		MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mrequesterLOC = new Requester(getActivity());
		mrequesterAR = new Requester(getActivity());
		mremoverLOC = new Remover(getActivity());
		mremoverAR = new Remover(getActivity());

		hlv2 = (GridView) getActivity().findViewById(R.id.gridView2);
		hlv2.setVisibility(View.INVISIBLE);

		prefsgoogle = getActivity().getSharedPreferences("google_login", Context.MODE_PRIVATE);
		prefspeso = getActivity().getSharedPreferences("com.mobi.mobilitapp_preferences", Context.MODE_PRIVATE);
		prefsface = getActivity().getSharedPreferences("face_login", Context.MODE_PRIVATE);
		prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

		c = getActivity();
		f = 0;

		oho = getActivity().getResources().getStringArray(R.array.window);
        leyenda = getActivity().getResources().getStringArray(R.array.window2);

		spanish = new Locale("es", "ES");
		wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		// FRAN: The memory of the device will leak
		//wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);


//audio
        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("estadoAnterior", mAudioManager.getRingerMode());
        editor.putInt("run", 0);
        editor.commit();


        if (prefs.getString("id", "no_id").equalsIgnoreCase("no_id")) {
           String id = hash(getSessionID());
            prefs.edit().putString("id", id).commit();
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


    @Override
	public void onResume() {
		super.onResume();
		Log.v("aki", "aki_onResumeMainFrag");
        getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		checkGooglePlayServices();
		checkWIFI();

        //audio
        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("estadoAnterior", mAudioManager.getRingerMode());
        editor.putInt("run", 0);
        editor.commit();

		if (!conn)
			isProviderEnabled();

	}

	private int getActivityID(String _activityType) {
		switch (_activityType) {
		case "on_foot":
			return (R.drawable.foot);
		case "vehicle":
			return (R.drawable.vehicle);
		case "bicycle":
			return (R.drawable.bicycle);
		case "still":
			return (R.drawable.still);
		case "bus":
			return (R.drawable.bus);
		case "metro":
			return (R.drawable.metro);
		case "tram":
			return (R.drawable.metro);
		case "renfe":
			return (R.drawable.metro);
		case "unknown":
		}
		return 0;
	}

	private int getActivityName(String _activityType) {
		switch (_activityType) {
		case "on_foot":
			return (R.string.on_foot);
		case "vehicle":
			return (R.string.vehicle);
		case "bicycle":
			return (R.string.bicycle);
		case "still":
			return (R.string.still);
		case "transport":
			return (R.string.transport);
		case "bus":
			return (R.string.bus);
		case "metro":
			return (R.string.metro);
		case "tram":
			return (R.string.tram);
		case "renfe":
			return (R.string.renfe);
		case "unknown":
			return (R.string.unknown);
		}
		return 0;
	}

	private void checkWIFI() {
		// TODO Auto-generated method stub
		if (!wifi.isWifiEnabled()) {

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

			// Setting Dialog Title
			alertDialog.setTitle(R.string.desc);

			// Setting Dialog Message
			alertDialog.setMessage(R.string.wifi);

			// On pressing Settings button
			alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
				}
			});

			// on pressing cancel button
			alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			// Showing Alert Message
			alertDialog.show();

		}
	}

	private boolean checkGooglePlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

		if (ConnectionResult.SUCCESS == resultCode) {
			// Toast.makeText(this, "Google Play Services Connected",
			// Toast.LENGTH_LONG).show();
			return true;
		} else {
			Toast.makeText(c, "Google Play Services Error", Toast.LENGTH_LONG).show();
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
					AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getActivity().getFragmentManager(), AppUtils.ERROR_MESSAGE);
			}
			return false;
		}
	}

	private void isProviderEnabled() {

		locationManager = (LocationManager) getActivity().getSystemService(LocationService.LOCATION_SERVICE);
		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// getting network status
		isNETWORKEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isNETWORKEnabled) {
			showLocationSettingsAlert();
		} else {

			mrequesterLOC.requestUpdates("LOC", false);
			mrequesterAR.requestUpdates("AR", false);
		}

	}

	public void showLocationSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		// Setting Dialog Title
		alertDialog.setTitle(R.string.req);

		// Setting Dialog Message
		alertDialog.setMessage(R.string.locat);

		// On pressing Settings button
		alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	private static int getColor(String _activity) {
		switch (_activity) {
		case "on_foot":
			return Color.rgb(255, 102, 51);
		case "vehicle":
			return Color.rgb(44, 62, 83);
		case "bicycle":
			return Color.rgb(205, 153, 102);
		case "bus":
			return Color.rgb(22, 189, 195);
		case "tram":
			return Color.rgb(0, 102, 51);
		case "renfe":
			return Color.rgb(238, 110, 110);
		case "unknown":
			return Color.BLACK;
		case "still":
			return Color.LTGRAY;
		}
		return Color.TRANSPARENT;
	}

	private int getMetroColor(String _line) {
		switch (_line) {

		case "L1":

			return Color.RED;

		case "L2":

			return Color.MAGENTA;

		case "L3":

			return Color.GREEN;

		case "L4":

			return Color.YELLOW;

		case "L5":

			return Color.BLUE;

		}
		return Color.BLACK;

	}

	public void drawLocation(ArrayList<LatLng> _latlng, String _first_time, String _last_time, String _activity,
			String _line, double _distance, long _duration, double _speed, int index) throws IOException {
		Marker m;
		int size = _latlng.size();
		if (!_latlng.isEmpty()) {
			if (first_location_history) {
				first_location_history = false;

				m = googleMap.addMarker(new MarkerOptions().position(_latlng.get(0)).title(_activity)
						.snippet(_first_time + " - " + _last_time)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_still)));

				marker = new Segment(getActivity());
				marker.setActivity(_activity);
				marker.setTotalDistance(_distance);
				if (_line != null) {
					marker.setLine(_line);
				}
				marker.setTotalDuration(_duration);
				marker.setTotalAverageSpeed(_speed);
				markers.add(index, marker);
				hmap.put(m, marker);

			} else {
				if (_activity.equalsIgnoreCase("metro")) {
					googleMap.addPolyline(new PolylineOptions().addAll(_latlng).width(10).color(getMetroColor(_line)));
				} else {
					googleMap.addPolyline(new PolylineOptions().addAll(_latlng).width(10).color(getColor(_activity)));
				}

				if (_activity.equals("still")) {
					m = googleMap.addMarker(new MarkerOptions().position(_latlng.get(size - 1)).title(_activity)
							.snippet(_first_time + " - " + _last_time)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_still)));

					marker = new Segment(getActivity());
					marker.setActivity(_activity);
					marker.setTotalDistance(_distance);
					marker.setTotalDuration(_duration);
					marker.setTotalAverageSpeed(_speed);
					markers.add(index, marker);
					hmap.put(m, marker);
				} else {
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(_latlng.get(0), 16);
					googleMap.animateCamera(cameraUpdate);
					m = googleMap.addMarker(new MarkerOptions().position(_latlng.get((size - 1) / 2)).title(_activity)
							.snippet(_first_time + " - " + _last_time).icon(getActivityIcon(_activity)));

					marker = new Segment(getActivity());
					marker.setActivity(_activity);
					marker.setTotalDistance(_distance);
					marker.setTotalDuration(_duration);
					if (_line != null) {
						marker.setLine(_line);
					}
					marker.setTotalAverageSpeed(_speed);
					markers.add(index, marker);
					hmap.put(m, marker);
				}
			}
		}
	}

	private BitmapDescriptor getActivityIcon(String _activityType) {
		switch (_activityType) {
		case "on_foot":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_foot);
		case "vehicle":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_road);
		case "bicycle":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_bic);
		case "still":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_still);
		case "bus":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_bus2);
		case "metro":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_metro);
		case "tram":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_metro);
		case "renfe":
			return BitmapDescriptorFactory.fromResource(R.drawable.ic_metro);
		case "unknown":
		}
		return null;
	}


	public void readLocation(String filename) {

		String _first_time, _last_time, _activity, _line = null;
		double _distance, _speed;
		long _duration;
		LatLng _latlng = null;
		ArrayList<LatLng> _latlngList;

		if (!markers.isEmpty()) {
			markers.clear();
		}

		if (!hmap.isEmpty()) {
			hmap.clear();
		}

		try {

			File readFile = new File(filename);
			FileInputStream stream = new FileInputStream(readFile);
			String jsonStr = null;

			try {
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

				jsonStr = Charset.defaultCharset().decode(bb).toString();
			} finally {
				stream.close();
			}

			JSONObject jsonObj = new JSONObject(jsonStr);
			// Getting JSON Array node
			segmentsData = jsonObj.getJSONArray("segments");
			for (int i = 0; i < segmentsData.length(); i++) {
				JSONObject segment = segmentsData.getJSONObject(i);

				_first_time = segment.getString("first time");
				_last_time = segment.getString("last time");
				_activity = segment.getString("activity");
				_distance = segment.getDouble("distance (m)");
				if (_activity.equalsIgnoreCase("metro")) {
					_line = segment.getString("line");
				}
				_duration = segment.getLong("duration (s)");
				_speed = segment.getDouble("speed (Km/h)");

				_latlngList = new ArrayList<LatLng>();
				if (_latlng != null) {
					_latlngList.add(_latlng);
				}
				JSONArray location_points = segment.getJSONArray("location");

				for (int j = 0; j < location_points.length();) {
					_latlng = new LatLng(location_points.optDouble(j), location_points.optDouble(j + 1));
					_latlngList.add(_latlng);
					j = j + 4;
				}

				drawLocation(_latlngList, _first_time, _last_time, _activity, _line, _distance, _duration, _speed, i);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exitAll() {
		Log.v("aki", "aki_exitAll");

		mremoverAR.removeUpdates(mrequesterAR.getRequestPendingIntent(), "AR");
		mremoverLOC.removeUpdates(mrequesterLOC.getRequestPendingIntent(), "LOC");

		getActivity().stopService(new Intent(getActivity(), LocationService.class));
		getActivity().stopService(new Intent(getActivity(), ActivityRecognitionService.class));
		getActivity().finish();
	}

	public void drawRoutes(String path) {

		googleMap.clear();
		readLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/MobilitApp/" + path
				+ "location_segment.json");
	}

	public void drawTraffic() {
		googleMap.clear();
        googleMap.setTrafficEnabled(true);

       // List<Tramo> Tramos = readTraffic();
        Log.v("traffic","Leer tráfico");
        String rutaTrafico = Environment.DIRECTORY_DOWNLOADS + "/MobilitApp/traffic";
        String urlInfoTrams = "http://www.bcn.cat/transit/dades/dadestrams.dat";
		/* FRAN: cambiamos el URL ya que el anterior cambió de dirección */
		String urlTrams = "http://opendata-ajuntament.barcelona.cat/resources/opendata/TRANSIT_RELACIO_TRAMS.xls";

        doDownload(urlInfoTrams, "infotrams.csv");
        downloaFilefromUrl(rutaTrafico, urlTrams, "trams.xls");
        Log.v("xsl","bajando trams");

	}

    protected void doDownload(String urlLink, String fileName) {
        Thread dx = new Thread() {

            public void run() {

                File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MobilitApp/traffic");

                Log.v("xsl","dir:"+dir+"");
                if(dir.exists()==false) {
                    dir.mkdirs();

                }
                //Save the path as a string value

                try {
                    URL url = new URL(urlLink);
                    Log.i("FILE_NAME", "File name is "+fileName);
                    Log.i("FILE_URLLINK", "File URL is "+url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(dir+"/"+fileName);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;

                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("ERROR ", "ERROR IS" +e);
                }
            }
        };
        dx.start();
    }


public int getTrafficColor(Tramo tramo, List<infoTramo> infoTramos){

    /*1#20080516103551#2#2. The first value, 1#, is the road section identifier.
    The following part is the date plus the hour, minutes and seconds. That is followed by a #2, which stands for
     current status (0 = no data, 1 = very fluid, 2 = fluid, 3 = dense, 4 = very dense, 5 = congested, 6 = closed).
     Finally, another value is shown which indicates the expected status in 15 minutes' time. */


    for(infoTramo infotramos : infoTramos) {

        if (tramo.tram.equals(infotramos.id)){


            switch (infotramos.status) {
                case "0":
                    //return Color.argb(100,192,192,192);
                    return Color.GRAY;
                case "1":
                    return Color.argb(255,0,255,0);
                case "2":
                    return Color.argb(255,128,255,0);
                case "3":
                    return Color.argb(255,255,255,0);
                case "4":
                    return Color.argb(255,255,128,0);
                case "5":
                    return Color.argb(255,255,0,0);
                case "6":
                    return Color.argb(255,0,0,0);

            }
        }

    }




    return 0;
}
    public class readXlsCsv {
        private List<Tramo> first;
        private List<infoTramo> second;

        public readXlsCsv(List<Tramo> first, List<infoTramo> second) {
            this.first = first;
            this.second = second;
        }

        public List<Tramo> getXLS() {
            return first;
        }

        public List<infoTramo> getCSV() {
            return second;
        }
    }


	public readXlsCsv readTraffic(){
		Log.v("traffic","Leertráfico");
		String rutaTrafico = Environment.DIRECTORY_DOWNLOADS + "/MobilitApp/traffic";
		String urlInfoTrams = "http://www.bcn.cat/transit/dades/dadestrams.dat";
		/* FRAN: change URL */
		String urlTrams = "http://opendata-ajuntament.barcelona.cat/resources/opendata/TRANSIT_RELACIO_TRAMS.xls";
		// String urlTrams = "http://opendata.bcn.cat/opendata/en/descarrega-fitxer?url=http%3a%2f%2fbismartopendata.blob.core.windows.net%2fopendata%2fopendata%2fTRANSIT_RELACIO_TRAMS.xls&name=TRANSIT_RELACIO_TRAMS.xls";

        try {
//leer xls
			Double[][] coord;
			ExcelReader.RowConverter<Tramo> converter = (HSSFRow) -> new Tramo(HSSFRow[0], HSSFRow[1], HSSFRow[2]);
			Log.v("xsl", "crear workbbook");
			ExcelReader<Tramo> reader = ExcelReader.builder(Tramo.class)
					.converter(converter)
					.withHeader()
					.csvDelimiter(';')
					.sheets(1)
					.build();
			Log.v("xsl", "creado worrkboook");

			List<Tramo> listaTramos;
			listaTramos = reader.read(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MobilitApp/traffic/trams.xls");


			//   crear objeto infotramos csv

			ExcelReader.RowConverter<infoTramo> converter2 = (Row) -> new infoTramo(Row[0], Row[1], Row[2], Row[3]);
			Log.v("xsl", "crear csv");
			ExcelReader<infoTramo> reader2 = ExcelReader.builder(infoTramo.class)
					.converter(converter2)
					.csvDelimiter('#')
					.build();
			Log.v("xsl", "creado csv");
			List<infoTramo> infoTramos;
			infoTramos = reader2.read(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MobilitApp/traffic/infotrams.csv");

			Log.v("xsl", "infotramossize:" + infoTramos.size() + "");


/*

            List<infoTramo> infoTramos;

            CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
            List myEntries = reader.readAll();
*/

			for (Tramo tramos : listaTramos) {
				//  Log.v("xsl: ", tramos.coordenadas+"");

				if (tramos.coordenadas != null) {
					String[] pares = tramos.coordenadas.split(",0");
					int i = 0;
					coord = new Double[pares.length][2];

					while (i < pares.length) {
						//quitar espacios en blanco
						//    Log.v("xsl", "i: " + i);
//extraer latitude longitude
						pares[i] = pares[i].replaceAll("\\s", "");
						String[] latLong = pares[i].split(",");

						coord[i][0] = Double.parseDouble(latLong[0]);
						coord[i][1] = Double.parseDouble(latLong[1]);
						i++;

					}
					//guardar en objeto tramo
					tramos.setCoord(coord);

				}
			}

			return new readXlsCsv(listaTramos, infoTramos);

		} catch (Exception e) {

            Log.e ("xsl",e.toString());
            return null;

	    }


	}


    public static class Tramo{
        public String tram;
        public String descripcion;
        public String coordenadas;
        public Double[][] coord;

        public Tramo(String tram, String descripcion,String coordenadas) {
            this.tram = tram;
            this.descripcion = descripcion;
            this.coordenadas = coordenadas;
        }
        public Double[][] getcoord(){
            return this.coord;

        }
        public void setCoord(Double[][] coord){
            this.coord = coord;

        }

    }
    public static class infoTramo{
        public String id;
        public String time;
        public String status;
        public String futurstatus;

        public infoTramo(String id, String time,String status, String futurstatus) {
            this.id = id;
            this.time = time;
            this.status = status;
            this.futurstatus = futurstatus;
        }
        public String getTime(){
            return this.time;

        }
        public void setTime(String time){
            this.time = time;

       }
        public String getStatus(){
            return this.status;

        }
        public void setStatus(String status){
            this.status = status;

        }
        public String getId(){
            return this.id;

        }
        public void setId(String id){
            this.id = id;

        }
        public String getFuturstatus(){
            return this.futurstatus;

        }
        public void setFuturstatus(String futurstatus){
            this.futurstatus = futurstatus;

        }

    }

	public void downloaFilefromUrl(String ruta,String url,String filename) {

        String rutaCarpeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + "/MobilitApp/traffic";

        try {
            File folder = new File(rutaCarpeta);
            boolean success = true;

            if (!folder.exists()) {
                success = folder.mkdir();
                Log.v("carpeta: ", "Carpeta crreada?" + String.valueOf(success));
            }
            if (success) {

                File file = new File(rutaCarpeta + "/" + filename);

                if (file.exists()) {
                    file.delete();
                    Log.v("carpeta: ", "borrando info_trams");
                }

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir(ruta, filename);
                //get download service and enqueue file
                DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                //


            } else {
                // Do something else on failure
                Log.v("download: ", "no se puede crear carpeta");
            }


        } catch (Exception e) {

        }

    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {
           readXlsCsv resultado = readTraffic();
           List<Tramo> Tramos = resultado.getXLS();
           List<infoTramo> infoTramos = resultado.getCSV();

           LatLng latlng = null;
           ArrayList<LatLng> latlngList;

           //   int i =0;

           for(Tramo tramos : Tramos) {
               int i=0;
               latlngList = new ArrayList<LatLng>();
               if (tramos.getcoord() != null){
                   //Log.v("xsl", "n coordenadas de tramo: "+tramos.getcoord().length+"");

                   while (i < tramos.getcoord().length) {
                       // Log.v("xsl", "llenando lista vacia");
                       //Log.v("xsl", "lat"+tramos.getcoord()[i][0]+"long"+tramos.getcoord()[i][1]+"");
                       latlng = new LatLng(tramos.getcoord()[i][1], tramos.getcoord()[i][0]);
                       latlngList.add(latlng);
                       i++;
                   }
                   //  Log.v("xsl", "lista terminada de llenar");
                   if (!latlngList.isEmpty()) {

                       googleMap.addPolyline(new PolylineOptions().addAll(latlngList).width(4).color(getTrafficColor(tramos,infoTramos)));
                       iV2.setImageResource(R.drawable.leyendatrafico);
                       ll2.setVisibility(View.VISIBLE);
                       ll.setVisibility(View.GONE);
                       hlv.setVisibility(View.GONE);
/*
                       Arrays.fill(valors3,"=");
                       hlv3.setAdapter(new AnotherAdapter(MainActivity.c, R.layout.dostv, valors3, leyenda));

                       //densidad de pantalla
                       ViewGroup.MarginLayoutParams params = (MarginLayoutParams) hlv.getLayoutParams();
                       int dens = MainActivity.c.getResources().getDisplayMetrics().densityDpi;
                       params.setMargins(15 * dens / 160, 15 * dens / 160, 15 * dens / 160, 15 * dens / 160);
                       hlv3.setLayoutParams(params);

                       ll2.setVisibility(View.VISIBLE);
*/
                   } else {
                       //  Log.v("xsl", "lista vacia");
                   }
               }
           }

            JSONArray incidencias=new JSONArray();


            try {
                String urlTraficoIcons = "http://infocar.dgt.es/etraffic/img/iconosIncitar/";
                incidencias = new getIncidencias().execute().get();

                for (int i = 0; i < incidencias.length(); i++) {

                    JSONObject c = incidencias.getJSONObject(i);


                    Log.v("json: ", "icono: " +urlTraficoIcons+ c.getString("icono"));
                    LatLng incidenciaCoord = new LatLng(Double.parseDouble(c.getString("lat")), Double.parseDouble(c.getString("lng")));
                    String textnoHtml = Html.fromHtml(Html.fromHtml(c.getString("descripcion")).toString()).toString();

                    try {
                        Bitmap bmImg = Ion.with(getActivity())
                                .load(urlTraficoIcons+c.getString("icono")).asBitmap().get();
                        googleMap.addMarker(new MarkerOptions()
                                        .position(incidenciaCoord)
                                        .title("Incidencia")
                                        .snippet(textnoHtml)
                                        .icon(BitmapDescriptorFactory.fromBitmap(bmImg))
                        );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }




                }

            }catch (Exception e){

            }
       }
    };

    public class getIncidencias extends AsyncTask<Void, Void, JSONArray> {
        public getIncidencias() {

        }

        protected JSONArray doInBackground(Void... parameters) {

            JSONArray incidencias = new JSONArray();
            try {
                //dibujar incidencias

                String urlTraficoDgt = "http://infocar.dgt.es/etraffic/BuscarElementos";

                //guardar parametros de segment en un array
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("latNS", "44.33956524809713"));
                params.add(new BasicNameValuePair("longNS", "30.1904296875"));
                params.add(new BasicNameValuePair("latSW", "26.745610382199022"));
                params.add(new BasicNameValuePair("longSW", "-39.287109375"));
                params.add(new BasicNameValuePair("zoom", "5"));
                params.add(new BasicNameValuePair("accion", "getElementos"));
                params.add(new BasicNameValuePair("Camaras", "false"));
                params.add(new BasicNameValuePair("SensoresTrafico", "false"));
                params.add(new BasicNameValuePair("SensoresMeteorologico", "false"));
                params.add(new BasicNameValuePair("Paneles", "false"));
                params.add(new BasicNameValuePair("IncidenciasRETENCION", "true"));
                params.add(new BasicNameValuePair("IncidenciasOBRAS", "false"));
                params.add(new BasicNameValuePair("IncidenciasMETEOROLOGICA", "true"));
                params.add(new BasicNameValuePair("IncidenciasPUERTOS", "true"));
                params.add(new BasicNameValuePair("IncidenciasOTROS", "true"));
                params.add(new BasicNameValuePair("IncidenciasEVENTOS", "true"));
                params.add(new BasicNameValuePair("niveles", "false"));
                params.add(new BasicNameValuePair("caracter", "acontecimiento"));

                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(urlTraficoDgt, ServiceHandler.GET,params);
                incidencias = new JSONArray(jsonStr);

            } catch (Exception e) {
                Log.e("json: ", e.toString());

            }
            return incidencias;
        }

    }

    public class ServiceHandler {

        String response = null;
        public final static int GET = 1;
        public final static int POST = 2;

        public ServiceHandler() {

        }

        /**
         * Making service call
         * @url - url to make request
         * @method - http request method
         * */
        public String makeServiceCall(String url, int method) {
            return this.makeServiceCall(url, method, null);
        }

        /**
         * Making service call
         * @url - url to make request
         * @method - http request method
         * @params - http request params
         * */
        public String makeServiceCall(String url, int method,
                                      List<NameValuePair> params) {
            try {
                // http client
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;

                // Checking http request method type
                if (method == POST) {
                    HttpPost httpPost = new HttpPost(url);
                    // adding post params
                    if (params != null) {
                        httpPost.setEntity(new UrlEncodedFormEntity(params));
                    }

                    httpResponse = httpClient.execute(httpPost);

                } else if (method == GET) {
                    // appending params to url
                    if (params != null) {
                        String paramString = URLEncodedUtils
                                .format(params, "utf-8");
                        url += "?" + paramString;
                    }
                    HttpGet httpGet = new HttpGet(url);

                    httpResponse = httpClient.execute(httpGet);

                }
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;

        }
    }

	public void cleanMap() {

		googleMap.clear();
        googleMap.setTrafficEnabled(false);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		Log.v("onMapReady", "MAP_READY");
		LatLng barcelona = new LatLng(41.396531, 2.159532);

// BAYO		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setMapToolbarEnabled(true);
		googleMap.getUiSettings().setCompassEnabled(true);
		// Map centered in Barcelona (Fran)
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(barcelona, 11));
		this.googleMap = googleMap;

		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
                String shours, sminutes, ssec;
                double parameter;
                if (hmap.get(marker) != null) {

                    double spe = hmap.get(marker).getAverageSpeed();
                    double horascal = hmap.get(marker).getTotalDuration() / 3600.0;

                    long seconds = hmap.get(marker).getTotalDuration();
                    double km = hmap.get(marker).getTotalDistance() / 1000.0;
                    long hours = seconds / (60 * 60);
                    ll2.setVisibility(View.GONE);

                    if (hours < 10) {
                        shours = "0" + Long.toString(hours);
                    } else {
                        shours = Long.toString(hours);
                    }
                    seconds = seconds % (60 * 60);
                    long minutes = seconds / 60;
                    if (minutes < 10) {
                        sminutes = "0" + Long.toString(minutes);
                    } else {
                        sminutes = Long.toString(minutes);
                    }
                    seconds = seconds % 60;
                    if (seconds < 10) {
                        ssec = "0" + Long.toString(seconds);
                    } else {
                        ssec = Long.toString(seconds);
                    }

                    String distance = String.format(spanish, "%.2f", (hmap.get(marker).getTotalDistance() / 1000));

                    String speed = String.format(spanish, "%.2f", spe);

                    if (spe < 2.7) {
                        parameter = 2.3;
                    } else {
                        parameter = 2.9;
                    }

                    String calo = String.format(spanish, "%.4f",
                            parameter * Integer.parseInt(prefspeso.getString("peso", "0")) * horascal);

                    String co2 = String.format(spanish, "%.4f", 140 * km);

                    valors[0] = distance;
                    valors[1] = shours + ":" + sminutes + ":" + ssec;
                    valors[2] = speed;

                    if (hmap.get(marker).getActivity().equalsIgnoreCase("on_foot")) {
                        valors2[0] = calo;
                        oho2[0] = getActivity().getResources().getString(R.string.calor);
                    }
                    if (hmap.get(marker).getActivity().equalsIgnoreCase("bus")
                            || hmap.get(marker).getActivity().equalsIgnoreCase("metro")
                            || hmap.get(marker).getActivity().equalsIgnoreCase("renfe")) {
                        valors2[0] = co2;
                        oho2[0] = getActivity().getResources().getString(R.string.co2);
                    }


                    iV.setImageResource(getActivityID(hmap.get(marker).getActivity()));
                    tV.setText(getActivityName(hmap.get(marker).getActivity()));

                    hlv.setAdapter(new AnotherAdapter(MainActivity.c, R.layout.dostv, valors, oho));
                    hlv2.setAdapter(new AnotherAdapter(MainActivity.c, R.layout.dostv, valors2, oho2));

                    ll.setVisibility(View.VISIBLE);
                    hlv.setVisibility(View.VISIBLE);

                    ViewGroup.MarginLayoutParams params = (MarginLayoutParams) hlv.getLayoutParams();

                    int dens = MainActivity.c.getResources().getDisplayMetrics().densityDpi;

                    if (hmap.get(marker).getActivity().equalsIgnoreCase("on_foot")
                            || hmap.get(marker).getActivity().equalsIgnoreCase("metro")
                            || hmap.get(marker).getActivity().equalsIgnoreCase("bus")
                            || hmap.get(marker).getActivity().equalsIgnoreCase("renfe")) {
                        params.setMargins(15 * dens / 160, 7 * dens / 160, 15 * dens / 160, 15 * dens / 160);
                        hlv.setLayoutParams(params);

                        hlv2.setVisibility(View.VISIBLE);
                    } else {
                        params.setMargins(15 * dens / 160, 15 * dens / 160, 15 * dens / 160, 15 * dens / 160);
                        hlv.setLayoutParams(params);
                        hlv2.setVisibility(View.GONE);
                    }

                }
                else{ //markers de trafico

                    ll2.setVisibility(View.GONE);
                    hlv.setVisibility(View.GONE);
                    hlv2.setVisibility(View.GONE);
                    iV.setVisibility(View.GONE);


                    tV.setText(marker.getSnippet());
                    ll.setVisibility(View.VISIBLE);
                }
            }
		});

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {

				ll.setVisibility(View.GONE);
				hlv.setVisibility(View.INVISIBLE);
                ll2.setVisibility(View.GONE);



			}
		});
	}
}
