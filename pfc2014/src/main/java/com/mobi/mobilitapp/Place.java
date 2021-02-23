package com.mobi.mobilitapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Place {

	private String name;

	private Double latitude;
	private Double longitude;

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Place jsonToPlace(JSONObject point) {
		try {
			Place result = new Place();
			JSONObject geometry = (JSONObject) point.get("geometry");
			JSONObject location = (JSONObject) geometry.get("location");
			result.setLatitude((Double) location.get("lat"));
			result.setLongitude((Double) location.get("lng"));
			result.setName(point.getString("name"));

			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Place{" + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
	}

}