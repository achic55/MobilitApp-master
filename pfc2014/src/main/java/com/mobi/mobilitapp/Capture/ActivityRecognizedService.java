package com.mobi.mobilitapp.Capture;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mahmoud Elbayoumy on 10/26/2017.
 */

public class ActivityRecognizedService extends IntentService {

    //Context context;
    //String output;
    long timestamp;
    int vehicle;
    int bicycle;
    int foot;
    int running;
    int still;
    int tilting;
    int walking;
    int unknown;

    public static List<ARSample> arData = new ArrayList<ARSample>();
    //public static boolean startLogging;

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActivityRecognizedService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            timestamp = new Date().getTime();
            // Creating logs with probability headers [Vehicle,Bicycle,Foot,Running,Still,Tilting,Walking,Unknown]
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    vehicle = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    bicycle = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    foot = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.RUNNING: {
                    running = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "Running: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.STILL: {
                    still = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "Still: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.TILTING: {
                    tilting = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "Tilting: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.WALKING: {
                    walking = activity.getConfidence();
                    //Log.d( "BAYO ActivityRecogition", "Walking: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    unknown = activity.getConfidence();
                  //  Log.d( "BAYO ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    break;
                }
            }
            //Log.d( "BAYO AR", Boolean.toString(startLogging));
            //if(startLogging) {
              //  Log.d( "BAYO AR", "file: " + Long.toString(timestamp) + "," +
                //      vehicle + "," + bicycle + "," + foot + "," + running + "," +
                  //  still + "," + tilting + "," + walking + "," + unknown );

                arData.add(new ARSample(timestamp, vehicle, bicycle, foot, running, still, tilting, walking, unknown));
           // }
        }
    }

    /*public List<ARSample> getARData(){
        if(ARData == null)
            Log.d( "BAYO AR", "Data is null");
        else
            Log.d( "BAYO AR", "Data is NOT null");
        return ARData;
    }*/
}
