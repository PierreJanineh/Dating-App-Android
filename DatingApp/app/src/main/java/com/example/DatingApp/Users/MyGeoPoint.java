package com.example.DatingApp.Users;

import com.google.firebase.firestore.GeoPoint;
import java.io.Serializable;

public class MyGeoPoint implements Serializable {
	
	private double lat, lng;
	
	public MyGeoPoint(GeoPoint geoPoint) {
		this.lat = geoPoint.getLatitude();
		this.lng = geoPoint.getLongitude();
	}
	
	public double getLat() {
		return lat;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public double getLng() {
		return lng;
	}
	
	public void setLng(double lng) {
		this.lng = lng;
	}
}