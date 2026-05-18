package com.example.test1;

import com.google.firebase.firestore.GeoPoint;

public class RunningSession {
    private String name;
    private GeoPoint location;       // 장소
    private String date;
    private String time;
    private String course;         // 코스
    private int maxParticipants;   // 최대 인원수
    private String pace;           // 페이스

    // 생성자
    public RunningSession(String name, GeoPoint location, String date, String time, String course, int maxParticipants, String pace) {
        this.name=name;
        this.location = location;
        this.date = date;
        this.time = time;
        this.course = course;
        this.maxParticipants = maxParticipants;
        this.pace = pace;
    }
    public String getname(){
        return name;
    }
    public void setname(String name){
        this.name=name;
    }
    // Getter와 Setter
    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getCourse() {
        return course;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }
}