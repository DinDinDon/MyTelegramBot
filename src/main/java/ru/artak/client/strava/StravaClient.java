package ru.artak.client.strava;


import ru.artak.Main;

// TODO для работы со strava
public class StravaClient {

    public  static int stravaClientId;
    public  static String stravaSecret;
    public static final String STRAVA_OAUTH_ADDRESS = "https://www.strava.com/oauth/";

    public StravaClient(int stravaClientId, String stravaSecret) {
        this.stravaClientId = stravaClientId;
        this.stravaSecret = stravaSecret;
    }
}
