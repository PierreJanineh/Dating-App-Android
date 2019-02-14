package com.example.DatingApp.Users;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable, Comparable<User> {
	public static final String TAG = "UserClass";
	public static final String NAME_DB = "name";
	public static final String TOKEN_DB = "token";
	public static final String LATLNG_DB = "latlng";
	public static final String MESSAGES_DB = "messages";
	public static final String IMG_URL_DB = "img_url";

	private String docID;
	private String uid;
	private String name;
	private String token;
	private GeoPoint latlng;
	private List<String> messages;
	private String img_url;
	private String distance;
	private UserInfo info;
	private List<String> favs;
	
	public User(){
	
	}
	
	public User(String uid,
	            String name,
	            String token,
	            GeoPoint latlng,
	            List<String> messages,
	            String img_url,
	            UserInfo info,
	            List<String> favs) {
		this.uid = uid;
		this.name = name;
		this.token = token;
		this.latlng = latlng;
		this.messages = messages;
		this.img_url = img_url;
		this.info = info;
		this.favs = favs;
	}
	
	public List<String> getFavs() {
		return favs;
	}
	
	public void setFavs(List<String> favs) {
		this.favs = favs;
	}
	
	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	public String getUid() {
		return uid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getToken() {
		return token;
	}
	
	public GeoPoint getLatlng() {
		return latlng;
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public String getImg_url() {
		return img_url;
	}
	
	public UserInfo getInfo() {
		return info;
	}
	
	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	public String getDistance() {
		return distance;
	}
	
	public void addRoom(String id){
		this.messages.add(id);
	}
	
	@Override
	public int compareTo(User o) {
		if (getDistance() == null || o.getDistance() == null) {
			return 0;
		}
		return Integer.valueOf(getDistance()).compareTo(Integer.valueOf(o.getDistance()));
	}
}