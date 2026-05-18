package com.example.test1;

public class RunRecord {
    private long timestamp;
    private double distance;
    private double pace;
    private double calories;
    private long steps;

    public RunRecord() {}

    public long getTimestamp() { return timestamp; }
    public double getDistance() { return distance; }
    public double getPace() { return pace; }
    public double getCalories() { return calories; }
    public long getSteps() { return steps; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setDistance(double distance) { this.distance = distance; }
    public void setPace(double pace) { this.pace = pace; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setSteps(long steps) { this.steps = steps; }
}
