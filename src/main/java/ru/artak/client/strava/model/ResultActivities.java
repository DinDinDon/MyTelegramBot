package ru.artak.client.strava.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultActivities {
    private String name;

    private double distance;

    @JsonProperty("start_date_local")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private LocalDateTime startDate;
    
    private String timezone;

    private String type;

    public ResultActivities() {
    }


    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public String getTimezone() { return timezone; }

    public String getType() { return type; }

}

