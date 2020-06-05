package ru.artak.client.strava.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.artak.client.strava.Athlete;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Strava {
    
    // TODO перенести в StravaClient, получив переменные через main(String[] args), передавая в конструктор
    private final int ID = 46301;
    public static final String SECRET = "671832cb5403c96630b8a3facc66d9953b06fd1a";
    public static final String ADRESS = "https://www.strava.com/oauth/";
    
    private String refresh_token;
    
    //действует 6 часов
    private String access_token;
    private Athlete athlete;

    public Strava() {
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Athlete getAthlete() {
        return athlete;
    }


}