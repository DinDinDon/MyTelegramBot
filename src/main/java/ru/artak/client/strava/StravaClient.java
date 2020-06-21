package ru.artak.client.strava;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.strava.model.ResultActivities;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class StravaClient {

    public static final String STRAVA_OAUTH_ADDRESS = "https://www.strava.com/oauth/";
    public static final String STRAVA_API_ADDRESS = "https://www.strava.com/api/v3";
    private final ObjectMapper mapper = new ObjectMapper();

    private int stravaClientId;
    private String stravaSecret;
    private HttpClient httpClientForStrava = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public StravaClient(int stravaClientId, String stravaSecret) {
        this.stravaClientId = stravaClientId;
        this.stravaSecret = stravaSecret;
    }

    public StravaOauthResp getStravaCredentials(String authorizationCode) throws IOException, InterruptedException {
        HttpRequest requestPostStrava = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_OAUTH_ADDRESS + "token?client_id=" + stravaClientId + "&client_secret=" + stravaSecret + "&code=" +
                        authorizationCode + "&grant_type=authorization_code"))
                .header("Authorization", "Bearer " + stravaSecret)
                .POST(noBody())
                .build();
        HttpResponse<String> stravaAccessToken = httpClientForStrava.send(requestPostStrava, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(stravaAccessToken.body(), StravaOauthResp.class);
    }

    public StravaOauthResp getUpdateAccessToken(String refreshToken) throws IOException, InterruptedException {
        HttpRequest requestForUpdateAccess = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_API_ADDRESS+"/oauth/token?client_id=" + stravaClientId + "&client_secret=" + stravaSecret +
                        "&grant_type=refresh_token&refresh_token=" +refreshToken))
                .header("Authorization", "Bearer " + stravaSecret)
                .POST(noBody())
                .build();
        HttpResponse<String> responseUpdateAccess = httpClientForStrava.send(requestForUpdateAccess, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(responseUpdateAccess.body(),StravaOauthResp.class);
    }

    public List<ResultActivities> getActivities(String accessToken, Long after, Long before) throws IOException, InterruptedException {
        HttpRequest requestForGetActivities = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_API_ADDRESS + "/athlete/activities?&after=" + after + "&before=" + before))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> responseActivities = httpClientForStrava.send(requestForGetActivities, HttpResponse.BodyHandlers.ofString());
        List<ResultActivities> activities = mapper.readValue(responseActivities.body(), new TypeReference<>() {
        });
        System.out.println();

        return activities;
    }

    public void deauthorizeUser(String accessToken) throws IOException, InterruptedException {
        HttpRequest requestForDeauthorize = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_OAUTH_ADDRESS+"/deauthorize"))
                .header("Authorization", "Bearer " + accessToken)
                .POST(noBody())
                .build();
        HttpResponse<String> responseDeauthorize = httpClientForStrava.send(requestForDeauthorize, HttpResponse.BodyHandlers.ofString());
    }



}
