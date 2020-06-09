package ru.artak.client.strava;


// TODO для работы со strava
public class StravaClient {

    public static final String STRAVA_OAUTH_ADDRESS = "https://www.strava.com/oauth/";
    public static int stravaClientId;
    public static String stravaSecret;


    public StravaClient(int stravaClientId, String stravaSecret) {
        this.stravaClientId = stravaClientId;
        this.stravaSecret = stravaSecret;
    }


//    private void get(){
//        HttpRequest requestPostStrava = HttpRequest.newBuilder()
//                .uri(URI.create(Strava.ADRESS + "token?client_id=46301&client_secret=" + stravaSecret + "&code=" + authorizationCode + "&grant_type=authorization_code"))
//                .header("Authorization", "Bearer " + Strava.SECRET)
//                .POST(noBody())
//                .build();
//        HttpResponse<String> stravaAccessToken = httpClient.send(requestPostStrava, HttpResponse.BodyHandlers.ofString());
//
//        Strava strava = mapper.readValue(stravaAccessToken.body(), Strava.class);
//    }
}
