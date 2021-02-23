package com.mobi.mobilitapp;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityRecognitionService extends IntentService {

	private static final String MyTAG = "MyTAG";

	public ActivityRecognitionService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ActivityRecognitionService() {
		super("AR");
		// TODO Auto-generated constructor stub
	}

	int activityType;
	String activityName;

	@Override
	protected void onHandleIntent(Intent intent) {

		// If the incoming intent contains an update
		if (ActivityRecognitionResult.hasResult(intent)) {

			// Get the update
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			
			// Get the most probable activity
			DetectedActivity mostProbableActivity = result.getMostProbableActivity();

			// Get an integer describing the type of activity

			activityType = mostProbableActivity.getType();
			activityName = getNameFromType(activityType);

			/*
			 * At this point, you have retrieved all the information for the
			 * current update. You can display this information to the user in a
			 * notification, or send it to an Activity or Service in a broadcast
			 * Intent.
			 */
			Intent intentActivity = new Intent("activityRecognition");
			sendActivityLocationBroadcast(intentActivity, activityName);

		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	private void sendActivityLocationBroadcast(Intent _intent, String _activity) {
		
		_intent.putExtra("activity", _activity);
		sendBroadcast(_intent);
	}

	private String getNameFromType(int activityType) {
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE: // 0
			// vehicle changed to transport
			return "vehicle";
		case DetectedActivity.ON_BICYCLE: // 1
			// bicycle changed to transport
			return "bicycle";
		case DetectedActivity.ON_FOOT: // 2
			return "on_foot";
		case DetectedActivity.STILL: // 3
			return "still";
		case DetectedActivity.UNKNOWN: // 4
			return "unknown";
		case DetectedActivity.TILTING: // 5
			// tilting changed to still
			return "still";
		}
		return "null";
	}
}