package ru.artak.client.strava;


import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.strava.model.StravaOauthResp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class StravaClient {

    public static final String STRAVA_OAUTH_ADDRESS = "https://www.strava.com/oauth/";
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
}
