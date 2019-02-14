package com.example.DatingApp.Users;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class MyUser implements Serializable {
	
	private User user;
	private GeoPoint geoPoint;
	
	private String uid;
	private String name;
	private String token;
	private MyGeoPoint latlng;
	private List<String> messages;
	private String img_url;
	private String distance;
	private UserInfo info;
	private String docID;
	private List<String> favs;
	
	public MyUser(User user) {
		this.uid = user.getUid();
		this.name = user.getName();
		this.token = user.getToken();
		this.latlng = new MyGeoPoint(user.getLatlng());
		this.messages = user.getMessages();
		this.img_url = user.getImg_url();
		this.info = user.getInfo();
		this.docID = user.getDocID();
		this.favs = user.getFavs();
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
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public MyGeoPoint getLatlng() {
		return latlng;
	}
	
	public void setLatlng(MyGeoPoint latlng) {
		this.latlng = latlng;
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	public String getImg_url() {
		return img_url;
	}
	
	public void setImg_url(String img_url) {
		this.img_url = img_url;
	}
	
	public String getDistance() {
		return distance;
	}
	
	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	public UserInfo getInfo() {
		return info;
	}
	
	public void setInfo(UserInfo info) {
		this.info = info;
	}
}
