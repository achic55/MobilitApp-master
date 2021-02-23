package com.mobi.mobilitapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlacesService {

	private String API_KEY;

	public PlacesService(String apikey) {
		this.API_KEY = apikey;
	}

	public void setApiKey(String apikey) {
		this.API_KEY = apikey;
	}

	public ArrayList<String> findPlaces(double latitude, double longitude, double latitude2, double longitude2,
			String placeSpacification) {

		String urlString = makeUrl(latitude, longitude, placeSpacification);
		String urlString2 = makeUrl(latitude2, longitude2, placeSpacification);

		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			String json = getJSON(urlString);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");

			String json2 = getJSON(urlString2);
			JSONObject object2 = new JSONObject(json2);
			JSONArray array2 = object2.getJSONArray("results");

			try {
				Place place = Place.jsonToPlace((JSONObject) array.get(0));
				Place place2 = Place.jsonToPlace((JSONObject) array2.get(0));
				if (place != null)
					arrayList.add(place.getName());
				if (place2 != null)
					arrayList.add(place2.getName());
			} catch (Exception e) {
			}
			return arrayList;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	// https://maps.googleapis.com/maps/api/place/search/json?types=subway_station&location=41.424748,2.142273&radius=150&key=AIzaSyD7VIF3TBcXYRZ0ZNFx_iZ8mk1neEhxswQ

	private String makeUrl(double latitude, double longitude, String place) {
		StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");

		urlString.append("&location=");
		urlString.append(Double.toString(latitude));
		urlString.append(",");
		urlString.append(Double.toString(longitude));
		urlString.append("&radius=200");
		urlString.append("&types=" + place);
		urlString.append("&key=" + API_KEY);

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
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 8);
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
