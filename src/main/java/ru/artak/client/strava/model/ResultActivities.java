package ru.artak.client.strava.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultActivities {

    private String name;

    private double distance;

    @JsonProperty("start_date")
    private Date startDate = new Date();


    public ResultActivities() {
    }


    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public Date getStartDate() {
        return startDate;
    }
}

