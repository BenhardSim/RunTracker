package org.example.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Goal {

    private int goalId;
    private String deadLine;
    private int totalCal;
    private String desc;
    private String status;

    @JsonCreator
    public Goal(@JsonProperty("goalId") int goalId,
                @JsonProperty("deadLine") String deadLine,
                @JsonProperty("totalCal") int totalCal,
                @JsonProperty("desc") String desc,
                @JsonProperty("status") String status){
        this.goalId = goalId;
        this.deadLine = deadLine;
        this.totalCal = totalCal;
        this.desc = desc;

        LocalDate targetDate = LocalDate.parse(deadLine, DateTimeFormatter.ISO_DATE);
        LocalDate currentDate = LocalDate.now();

        if(targetDate.isAfter(currentDate)){
            this.status = "OnProgress";
        } else if (targetDate.isBefore(currentDate)) {
            this.status = "DeadLine Exceeded";
        } else {
            this.status = status;
        }
    }

    // setter
    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setTotalCal(int totalCal) {
        this.totalCal = totalCal;
    }

    // getter
    public int getGoalId() {
        return this.goalId;
    }
    public int getTotalCal() {
        return this.totalCal;
    }
    public String getDeadLine() {
        return this.deadLine;
    }
    public String getDesc() {
        return this.desc;
    }
    public String getStatus() {
        return this.status;
    }
}
