package ru.Artak.strava.modul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Athlete {

    private int id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;
    private String city;
    private String country;
    private String sex;

    public Athlete() {
    }

    public int getId() {
        return id;
    }


    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public String getCity() {
        return city;
    }


    public String getCountry() {
        return country;
    }


    public String getSex() {
        return sex;
    }

}