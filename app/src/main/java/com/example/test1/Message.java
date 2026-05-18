package com.example.test1;
public class Message {
    private String text;
    private String sender;
    private long timestamp;
    private String userId;

    public Message() {
        // Firestore를 위한 기본 생성자
    }

    public Message(String text, String sender, String userId, long timestamp) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.userId=userId;
    }
    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

