package com.mobi.mobilitapp.Capture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mobi.mobilitapp.Inicio.ANDROID_ID;

/**
 * Created by Mahmoud Elbayoumy on 10/22/2017.
 */

public class LocationFinder {

    /*  GPS Location Finder Parameters - Mahmoud Elbayoumy 23/10/2017 */
    int GPS_TIME_INTERVAL = 60000; // get gps location every 1 min
    int MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    static public LocationManager locationManager;
    static public LocationListener locationListener;
    static List<LocationCoordinates> gpsData;
    long timestamp;
    Activity activity;
    private static String FILE_STORE_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";

    public LocationFinder(Activity activity) {
        this.activity = activity;
    }
        /*  GPS Location Finder Part - Mahmoud Elbayoumy 23/10/2017 */
        public void createLocationFinder(){

            gpsData = new ArrayList<LocationCoordinates>();

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                Log.d("BAYO 23", "LocationFinder granted permission");

                locationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        //       Log.d("BAYO 23", "LocationFinderEnabled:" + locationFinderEnabled);
                        timestamp = new Date().getTime();
                        float lat = (float) (location.getLatitude());
                        float lon = (float) (location.getLongitude());
                        gpsData.add(new LocationCoordinates(timestamp, lat, lon));
                       // Log.d("BAYO 23 LOC:", "Lat:" + lat + " , Lon: " + lon);
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {


                    }

                };


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);


            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }
        } else {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            Log.d("BAYO", "LocationFinder need no permissions");

            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                    timestamp = new Date().getTime();
                    float lat = (float) (location.getLatitude());
                    float lon = (float) (location.getLongitude());
                    gpsData.add(new LocationCoordinates(timestamp, lat, lon));

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }


            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }
    }

    public void destroyLocationFinder(){
        try {
            Log.d("BAYO 23", "LocationManager before:" + locationManager.toString());
            Log.d("BAYO 23", "LocationListener before:" + locationListener.toString());
            locationManager.removeUpdates(locationListener);
            if(locationManager != null) locationManager = null;
            if(locationListener != null) locationListener = null;
            if(locationManager != null){
                Log.d("BAYO 23", "LocationManager after:" + locationManager.toString());
            }else{
                Log.d("BAYO 23", "LocationManager after: is null");
            }
            if(locationListener != null) {
                Log.d("BAYO 23", "LocationListener after:" + locationListener.toString());
            }else{
                Log.d("BAYO 23", "LocationListener after: is null");
            }
            Log.d("BAYO 23", "LocationFinder disabled");
            //return true;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO 23", "Why GPS not down" + e.toString());
            //return false;
        }
    }

    public void saveLocationFinderData(int captureID, String activityType){

        Log.d("BAYO GPS", "Saving sensor data to files");

        //final TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

        String FILENAME_FORMAT =
                //tm.getDeviceId()
                ANDROID_ID
                        + "_" + String.format("%04d", captureID)
                        + "_" + activityType
                        + "_%s" // sensor type
                        + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
                        + ".csv";


        String filename = String.format(FILENAME_FORMAT, "GPS");
        CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
        csv.createGpsDataFile(gpsData);

    }
}