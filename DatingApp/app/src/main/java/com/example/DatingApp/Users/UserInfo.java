package com.example.DatingApp.Users;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserInfo implements Serializable {

    public static final String DELIMITER = "&";
    private int weight;
    private int height;
    private String userId;
    private String about;
    private String birthDate;
    private String relationship;
    private String religion;
    private String orientation;
    private String ethnicity;
    private String reference;
    private String STDs;
    private String role;

    public UserInfo(){
    
    }

    public UserInfo(String userId,
                    String about,
                    String birthDate,
                    int height,
                    int weight,
                    String relationship,
                    String religion,
                    String orientation,
                    String role,
                    String ethnicity,
                    String reference,
                    String STDs) {
        this.userId = userId;
        this.about = about;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.relationship = relationship;
        this.religion = religion;
        this.orientation = orientation;
        this.ethnicity = ethnicity;
        this.reference = reference;
        this.STDs = STDs;
        this.role = role;
    }

    public String getAbout() {
        return about;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSTDs() {
        return STDs;
    }

    public void setSTDs(String STDs) {
        this.STDs = STDs;
    }

    @Override
    public String toString() {
        return "user=" + userId + DELIMITER
                + "about=" + about + DELIMITER
                + "birthDate=" + birthDate + DELIMITER
                + "height=" + height + DELIMITER
                + "weight=" + weight + DELIMITER
                + "relationship=" + relationship + DELIMITER
                + "religion=" + religion + DELIMITER
                + "orientation=" + orientation + DELIMITER
                + "ethnicity=" + ethnicity + DELIMITER
                + "reference=" + reference + DELIMITER
                + "stds=" + STDs;
    }

    public Map<String,String> getMapFromUserInfo(String userInfo){
        String[] split = userInfo.split(DELIMITER);
        String dataPair, key, value;
        Map<String, String> userInfoMap = new HashMap<>();
        for (int i = 0; i < split.length; i++) {
            dataPair = split[i];
            String [] splitData = dataPair.split(":");
            for (int j = 0; j < splitData.length; j+=2) {
                key = splitData[j];
                value = splitData[j+1];
                userInfoMap.put(key,value);
            }
        }
        return userInfoMap;
    }


    public String getUserID() {
        return userId;
    }

    public void setUserID(String user) {
        this.userId = user;
    }
    public Map<Object, Object> mapOfUserInfo(){
        Map<Object,Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("about", about);
        map.put("birthdate", birthDate);
        map.put("height", height);
        map.put("weight", weight);
        map.put("relationship", relationship);
        map.put("religion", religion);
        map.put("orientation", orientation);
        map.put("ethnicity", ethnicity);
        map.put("reference", reference);
        map.put("STDs", STDs);
        map.put("role", role);
        return map;
    }

}
