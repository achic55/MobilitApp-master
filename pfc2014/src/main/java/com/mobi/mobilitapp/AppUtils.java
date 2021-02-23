package com.mobi.mobilitapp;

import java.util.Calendar;
import java.util.Locale;

import android.text.format.DateFormat;

public final class AppUtils {

	public final static int MILLISECONDS_PER_SECOND = 1000;
	public final static int NANOSECONDS_PER_SECOND = 1000000000;
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Update GPS frequency of locations in seconds
	public static final long UPDATE_INTERVAL_GPS = 60000; //8000

	// Update frequency of locations in seconds
	private static final int UPDATE_INTERVAL_LOCATION_IN_SECONDS = 60;
	// Update frequency of locations in milliseconds
	public static final long UPDATE_INTERVAL_LOCATION = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_LOCATION_IN_SECONDS;
	// The fastest update frequency of locations, in seconds
	private static final int FASTEST_INTERVAL_LOCATION_IN_SECONDS = 61; // "error" intencionado
	// A fast frequency ceiling of locations in milliseconds
	public static final long FASTEST_INTERVAL_LOCATION = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_LOCATION_IN_SECONDS;
	// Time frequency of activities, in seconds
	public static final int UPDATE_INTERVAL_ACTIVITY_IN_SECONDS = UPDATE_INTERVAL_LOCATION_IN_SECONDS / 4;
	// Time frequency of activities, in milliseconds
	public static final long UPDATE_INTERVAL_ACTIVITY = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_ACTIVITY_IN_SECONDS;
	//Time Between Segment Updates
	public static final int UPDATE_INTERVAL_SEGMENT_LOCATION_IN_SECONDS = 120;
	//Number of samples to estimate activity
	public static final int ACTIVITY_SAMPLES = UPDATE_INTERVAL_SEGMENT_LOCATION_IN_SECONDS
			/ UPDATE_INTERVAL_ACTIVITY_IN_SECONDS;
	//Block/Place radius, in meters
	public static final int BLOCK_RADIUS = 75;
	//Minimum transport distance, in meters
	public static final int MIN_TRANSPORT_DISTANCE = 450;
	//Smallest displacement to consider location changed, in meters
	public static final int SMALLEST_DISPLACEMENT = 25;
	//Minimum vehicle speed, in Km/h
	public static final double MIN_VEHICLE_SPEED = 10.0;
	//Maximum still speed, in Km/h
	public static final double MAX_STILL_SPEED = 1.0;
	//Maximum on_foot speed, in Km/h
	public static final double MAX_ON_FOOT_SPEED = 9.0;
	//Maximum bicycle speed, in Km/h
	public static final double MAX_BIKE_SPEED = 15.0;
	//Earth radius in meters
	public static final double EARTH_RADIUS = 6371000;

	public static final String ERROR_MESSAGE = "ERROR: Impossible to connect with Google Play Services";

	//method to convert timestamp of location into date and time
	public static String getFormatDate(long timestamp) {
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(timestamp);
		String time = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
		return time;
	}

	public static String getDay(long timestamp) {
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(timestamp);
		String day = DateFormat.format("dd-MM-yyyy", cal).toString();
		return day;
	}

	public static String convertToEngl(String date) {

		return new StringBuilder(date).reverse().toString();

	}

	public static String getDay2(long timestamp) {
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(timestamp);
		String day = DateFormat.format("yyyy-MM-dd", cal).toString();
		return day;

	}

	public static int getYear() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(timestamp);
		String year = DateFormat.format("yyyy", cal).toString();
		return Integer.parseInt(year);

	}

	public static String getTime(long timestamp) {
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(timestamp);
		String day = DateFormat.format("kk:mm:ss", cal).toString();
		return day;
	}

	//Distance between two locations, in meters
	public static double distanceCalculation(double firstLatitude, double firstLongitude, double lastLatitude,
			double lastLongitude) {
		double distance = 0;
		firstLatitude = Math.toRadians(firstLatitude);
		firstLongitude = Math.toRadians(firstLongitude);
		lastLatitude = Math.toRadians(lastLatitude);
		lastLongitude = Math.toRadians(lastLongitude);

		double x = (lastLongitude - firstLongitude) * Math.cos((firstLatitude + lastLatitude) / 2);
		double y = (firstLatitude - lastLatitude);

		distance = Math.sqrt(x * x + y * y) * AppUtils.EARTH_RADIUS;
		return distance;
	}

}
