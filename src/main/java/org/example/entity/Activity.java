package org.example.entity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Activity {
    private int activityId;
    private String date;
    private int duration;
    private int calories;
    private int distance;
    private int steps;
    private int avgHeartBeat;

    @JsonCreator
    public Activity(@JsonProperty("activityId") int activityId,
                    @JsonProperty("date") String date,
                    @JsonProperty("duration") int duration,
                    @JsonProperty("calories") int calories,
                    @JsonProperty("distance") int distance,
                    @JsonProperty("steps") int steps,
                    @JsonProperty("avgHeartBeat") int avgHeartBeat){
        this.activityId = activityId;
        this.date = date;
        this.duration = duration;
        this.calories = calories;
        this.distance = distance;
        this.steps = steps;
        this.avgHeartBeat = avgHeartBeat;
    }

    // setter
    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public void setCalories(int calories) {
        this.calories = calories;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public void setSteps(int steps) {
        this.steps = steps;
    }
    public void setAvgHeartBeat(int avgHeartBeat) {
        this.avgHeartBeat = avgHeartBeat;
    }

    // getter
    public int getActivityId() {
        return activityId;
    }
    public String getDate(){
        return this.date;
    }
    public int getDuration(){
        return this.duration;
    }
    public int getCalories(){
        return this.calories;
    }
    public int getDistance(){
        return this.distance;
    }
    public int getSteps(){
        return this.steps;
    }
    public int getAvgHeartBeat() {
        return this.avgHeartBeat;
    }

}
