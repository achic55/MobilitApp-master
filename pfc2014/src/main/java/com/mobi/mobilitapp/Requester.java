package com.mobi.mobilitapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class Requester implements ConnectionCallbacks, OnConnectionFailedListener {

	private Context mContext;
	private String mType;
	private boolean mgps;
	LocationManager locationManager;
	boolean isGPSEnabled;
	Activity mActivity;

	// Stores the PendingIntent used to send activity recognition events back to the app
	private PendingIntent mActivityRecognitionPendingIntent;

	// Stores the current instantiation of the activity recognition client
	private GoogleApiClient myClient;

	public Requester(Context context) {
		// Save the context
		mContext = context;

		// Initialize the globals to null
		mActivityRecognitionPendingIntent = null;
		myClient = null;

		locationManager = (LocationManager) mContext.getSystemService(LocationService.LOCATION_SERVICE);
		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

	}

	/**
	 * Returns the current PendingIntent to the caller.
	 *
	 * @return The PendingIntent used to request activity recognition updates
	 */
	public PendingIntent getRequestPendingIntent() {
		return mActivityRecognitionPendingIntent;
	}

	/**
	 * Sets the PendingIntent used to make activity recognition update requests
	 * 
	 * @param intent
	 *            The PendingIntent
	 */
	public void setRequestPendingIntent(PendingIntent intent) {
		mActivityRecognitionPendingIntent = intent;
	}

	/**
	 * Start the activity recognition update request process by getting a
	 * connection.
	 */
	public void requestUpdates(String type, boolean gps) {

		mType = type;
		mgps = gps;

		requestConnection();
	}

	/**
	 * Make the actual update request. This is called from onConnected().
	 */
	private void continueRequestActivityUpdates(String type) {

		if (type.equalsIgnoreCase("AR")) {
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(getClient(),
					AppUtils.UPDATE_INTERVAL_ACTIVITY, createRequestPendingIntent(type));
		} else {

			if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

				LocationServices.FusedLocationApi.requestLocationUpdates(getClient(), createLR(mgps),
						createRequestPendingIntent(type));

			}
		}
		// Disconnect the client
		requestDisconnection();
	}

	/**
	 * Request a connection to Location Services. This call returns immediately,
	 * but the request is not complete until onConnected() or
	 * onConnectionFailure() is called.
	 */
	private void requestConnection() {
		getClient().connect();
	}

	/**
	 * Get the current activity recognition client, or create a new one if
	 * necessary. This method facilitates multiple requests for a client, even
	 * if a previous request wasn't finished. Since only one client object
	 * exists while a connection is underway, no memory leaks occur.
	 *
	 * @return An ActivityRecognitionClient object
	 */
	private GoogleApiClient getClient() {
		if (myClient == null) {

			myClient = new GoogleApiClient.Builder(mContext).addApi(ActivityRecognition.API)
					.addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
					.build();
		}
		return myClient;
	}

	/**
	 * Get the current activity recognition client and disconnect from Location
	 * Services
	 */
	private void requestDisconnection() {
		getClient().disconnect();
	}

//requesting updates in background
	private PendingIntent createRequestPendingIntent(String type) {

		PendingIntent pendingIntent;
		// If the PendingIntent already exists
		if (null != getRequestPendingIntent()) {

			// Return the existing intent
			return mActivityRecognitionPendingIntent;

			// If no PendingIntent exists
		} else {

			if (type.equalsIgnoreCase("AR")) {

				Intent intent = new Intent(mContext, ActivityRecognitionService.class);

				pendingIntent = PendingIntent.getService(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

				setRequestPendingIntent(pendingIntent);
			} else {

				Intent intent = new Intent(mContext, LocationService.class);

				pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

				setRequestPendingIntent(pendingIntent);
			}
			return pendingIntent;
		}

	}

	private LocationRequest createLR(boolean gps) {
		LocationRequest locationRequest = null;
		if (!gps) {
			locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
					.setInterval(AppUtils.UPDATE_INTERVAL_LOCATION)
					.setFastestInterval(AppUtils.FASTEST_INTERVAL_LOCATION);
		} else {
			if (isGPSEnabled)
				locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
						.setInterval(AppUtils.UPDATE_INTERVAL_GPS); // queremos localización rapidamente !!! cada 8s con GPS
		}
		return locationRequest;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {

			try {
				connectionResult.startResolutionForResult((Activity) mContext,
						AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// display an error or log it here.
			}

			/*
			 * If no resolution is available, display Google Play service error
			 * dialog. This may direct the user to Google Play Store if Google
			 * Play services is out of date.
			 */
		} else {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), (Activity) mContext,
					AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (dialog != null) {
				dialog.show();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		continueRequestActivityUpdates(mType);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}
}
