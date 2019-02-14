package com.example.DatingApp.Chat;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class Message implements Serializable, Comparable<Message> {

    private String content;
    private Timestamp timestamp;
    private List<String> tofrom;
    private Boolean isItMe;
    
	public Message(){
    
    }

    public Message(String content, Timestamp timestamp, List<String> tofrom) {
        this.content = content;
        this.timestamp = timestamp;
        this.tofrom = tofrom;
    }
	
	public Boolean getIsItMe() {
		return isItMe;
	}
	
	public void setIsItMe(Boolean itMe) {
		isItMe = itMe;
	}
	
    public String getContent() {
        return content;
    }
    
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public List<String> getTofrom() {
		return tofrom;
	}
	
	public void setTofrom(List<String> tofrom) {
		this.tofrom = tofrom;
	}
	
	@Override
	public int compareTo(Message o) {
		if (getTimestamp() == null || o.getTimestamp() == null) {
			return 0;
		}
		return getTimestamp().compareTo(o.getTimestamp());
	}
}
