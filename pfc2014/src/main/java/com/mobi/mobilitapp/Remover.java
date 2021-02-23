package com.mobi.mobilitapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;

import android.content.IntentSender.SendIntentException;

import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;

import com.google.android.gms.location.LocationServices;

public class Remover implements ConnectionCallbacks, OnConnectionFailedListener {

	private Context mContext;
	private PendingIntent mCurrentIntent;
	private String mtype;

	// Stores the current instantiation of the activity recognition client
	private GoogleApiClient mActivityRecognitionClient;

	public Remover(Context context) {
		// Save the context
		mContext = context;

		// Initialize the globals to null
		mActivityRecognitionClient = null;

	}

	public void removeUpdates(PendingIntent requestIntent, String type) {

		mCurrentIntent = requestIntent;
		mtype = type;

		requestConnection();
	}

	/**
	 * Make the actual update request. This is called from onConnected().
	 */
	private void continueRemoveUpdates(String type) {
		/*
		 * Request updates, using the default detection interval. The
		 * PendingIntent sends updates to ActivityRecognitionIntentService
		 */
		if (type.equalsIgnoreCase("AR")) {
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(getActivityRecognitionClient(),
					mCurrentIntent);
		} else {
			LocationServices.FusedLocationApi.removeLocationUpdates(getActivityRecognitionClient(), mCurrentIntent);
		}
		mCurrentIntent.cancel();
		// Disconnect the client
		requestDisconnection();
	}

	/**
	 * Request a connection to Location Services. This call returns immediately,
	 * but the request is not complete until onConnected() or
	 * onConnectionFailure() is called.
	 */
	private void requestConnection() {
		getActivityRecognitionClient().connect();
	}

	/**
	 * Get the current activity recognition client, or create a new one if
	 * necessary. This method facilitates multiple requests for a client, even
	 * if a previous request wasn't finished. Since only one client object
	 * exists while a connection is underway, no memory leaks occur.
	 *
	 * @return An ActivityRecognitionClient object
	 */
	private GoogleApiClient getActivityRecognitionClient() {
		if (mActivityRecognitionClient == null) {

			mActivityRecognitionClient = new GoogleApiClient.Builder(mContext).addApi(ActivityRecognition.API)
					.addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
					.build();
		}
		return mActivityRecognitionClient;
	}

	/**
	 * Get the current activity recognition client and disconnect from Location
	 * Services
	 */
	private void requestDisconnection() {
		getActivityRecognitionClient().disconnect();
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
			} catch (SendIntentException e) {
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

		continueRemoveUpdates(mtype);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

	/*
	 * Implementation of OnConnectionFailedListener.onConnectionFailed If a
	 * connection or disconnection request fails, report the error
	 * connectionResult is passed in from Location Services
	 */

}
