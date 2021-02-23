package com.mobi.mobilitapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsService {

	private double lat, lon, lat2, lon2;
	private long departure_time, arrival_time;

	public DirectionsService(double latitude, double longitude, double latitude2, double longitude2,
			long departure_time, long arrival_time) {
		this.lat = latitude;
		this.lon = longitude;
		this.lat2 = latitude2;
		this.lon2 = longitude2;
		this.departure_time = departure_time;
		this.arrival_time = arrival_time;
	}

	public String findBusorTram() {
		String urlString = makeUrl(lat, lon, lat2, lon2, departure_time);
		long jsondeparture, jsonarrival, resta1, resta2;
		String type = "none";

		try {
			String json = getJSON(urlString);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("routes");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				JSONArray legs = obj.getJSONArray("legs");
				JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

				for (int j = 0; j < steps.length(); j++) {
					if (steps.getJSONObject(j).has("transit_details")) {
						type = steps.getJSONObject(j).getJSONObject("transit_details").getJSONObject("line")
								.getJSONObject("vehicle").getString("type");

						if (type.equalsIgnoreCase("BUS") || type.equalsIgnoreCase("TRAM")) {
							jsondeparture = steps.getJSONObject(j).getJSONObject("transit_details")
									.getJSONObject("departure_time").getLong("value");
							jsonarrival = steps.getJSONObject(j).getJSONObject("transit_details")
									.getJSONObject("arrival_time").getLong("value");

							resta1 = jsondeparture - departure_time;
							resta2 = jsonarrival - arrival_time;
							if (resta1 < 0)
								resta1 = -resta1;
							if (resta2 < 0)
								resta2 = -resta2;

							if (resta1 < (5 * 60) && resta2 < (3 * 60)) //margen de 5 minutos desde que estas still hasta que detecta vehicle
								return type;
						}
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "none";
	}

	// https://maps.googleapis.com/maps/api/directions/json?origin=41.441741,2.164925&destination=41.430577,2.144862&sensor=false&departure_time=1413056371&mode=transit&alternatives=true
	private String makeUrl(double latitude, double longitude, double latitude2, double longitude2, long departure_time) {
		StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");

		urlString.append("&origin=");
		urlString.append(Double.toString(latitude));
		urlString.append(",");
		urlString.append(Double.toString(longitude));
		urlString.append("&destination=");
		urlString.append(Double.toString(latitude2));
		urlString.append(",");
		urlString.append(Double.toString(longitude2));
		urlString.append("&mode=transit");
		urlString.append("&departure_time=");
		urlString.append(Long.toString(departure_time));
		urlString.append("&alternatives=true");
        Log.v("aki_ ","consulta a directions api");
		return urlString.toString();
	}

	protected String getJSON(String url) {
		return getUrlContents(url);
	}

	private String getUrlContents(String theUrl) {
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(theUrl);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}
}
