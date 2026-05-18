package com.example.test1;
public class ChatRoom {
    private String userType;
    private String name;

    public ChatRoom() {
        // Firestore에서 필요
    }

    public ChatRoom(String userType, String name) {
        this.userType = userType;
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

