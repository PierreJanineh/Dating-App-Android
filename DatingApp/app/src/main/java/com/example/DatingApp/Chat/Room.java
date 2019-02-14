package com.example.DatingApp.Chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
	private String roomUID;
	private ArrayList<Message> messages = new ArrayList<>();
	private List<String> tofrom;
	private List<String> seenby;
	
	public Room(){
	
	}
	
	public Room(String roomUID, ArrayList<Message> messages, List<String> tofrom, List<String> seenby) {
		this.roomUID = roomUID;
		this.messages = messages;
		this.tofrom = tofrom;
		this.seenby = seenby;
	}
	
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
	
	public void setTofrom(List<String> tofrom) {
		this.tofrom = tofrom;
	}
	
	public void setSeenby(List<String> seenby) {
		this.seenby = seenby;
	}
	
	public List<String> getTofrom() {
		return tofrom;
	}
	
	public List<String> getSeenby() {
		return seenby;
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	
	public String getRoomUID() {
		return roomUID;
	}
	
	public void addMessage(Message message){
		this.messages.add(message);
	}
	
	public void setRoomUID(String roomUID) {
		this.roomUID = roomUID;
	}
	
}
