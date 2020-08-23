package ru.artak.client.strava;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import ru.artak.client.strava.model.ResultActivities;
import ru.artak.storage.Storage;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.List;


import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class StravaClient {

    private static final Logger logger = Logger.getLogger(StravaClient.class);
    public static final String STRAVA_OAUTH_ADDRESS = "https://www.strava.com/oauth/";
    public static final String STRAVA_API_ADDRESS = "https://www.strava.com/api/v3";
    private final ObjectMapper mapper = new ObjectMapper();

    private int stravaClientId;
    private String stravaSecret;
    private Storage storage;
    private HttpClient httpClientForStrava = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public StravaClient(int stravaClientId, String stravaSecret, Storage storage) {
        this.stravaClientId = stravaClientId;
        this.stravaSecret = stravaSecret;
        this.storage = storage;
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

    public List<ResultActivities> getActivities(Long chatId, String accessToken, Long from, Long to) throws IOException, InterruptedException {
        String accessTokenUpdate = checkAccessAndUpdate(chatId);

        HttpRequest requestForGetActivities = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_API_ADDRESS + "/athlete/activities?&before=" + to + "&after=" + from))
                .header("Authorization", "Bearer " + accessTokenUpdate)
                .GET()
                .build();

        HttpResponse<String> responseActivities = httpClientForStrava.send(requestForGetActivities, HttpResponse.BodyHandlers.ofString());
        List<ResultActivities> activities = mapper.readValue(responseActivities.body(), new TypeReference<>() {
        });

        return activities;
    }

    public String checkAccessAndUpdate(Long chatId) throws IOException, InterruptedException {
        StravaCredential credential = storage.getStravaCredentials(chatId);
        Long timeToExpired = credential.getTimeToExpired();
        String refreshToken = credential.getRefreshToken();
        LocalDateTime dateTimeToExpired = LocalDateTime.ofInstant(Instant.ofEpochSecond(timeToExpired), ZoneId.systemDefault());
        LocalDateTime today = LocalDateTime.now(ZoneId.systemDefault());
        if (today.isAfter(dateTimeToExpired)) {
            credential = updateAccessToken(chatId, refreshToken);
            logger.info("update accessToken");
            return credential.getAccessToken();
        }

        return credential.getAccessToken();
    }

    private StravaCredential updateAccessToken(Long chatId, String refreshToken) throws IOException, InterruptedException {
        StravaOauthResp strava = getUpdateAccessToken(refreshToken);
        StravaCredential stravaCredential = new StravaCredential(strava.getAccessToken(), strava.getRefreshToken(), strava.getExpiresAt());

        storage.saveStravaCredentials(chatId, stravaCredential);

        return stravaCredential;
    }

    public StravaOauthResp getUpdateAccessToken(String refreshToken) throws IOException, InterruptedException {
        HttpRequest requestForUpdateAccess = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_API_ADDRESS + "/oauth/token?client_id=" + stravaClientId + "&client_secret=" + stravaSecret +
                        "&grant_type=refresh_token&refresh_token=" + refreshToken))
                .POST(noBody())
                .build();
        HttpResponse<String> responseUpdateAccess = httpClientForStrava.send(requestForUpdateAccess, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(responseUpdateAccess.body(), StravaOauthResp.class);
    }

    public void deauthorizeUser(String accessToken) throws IOException, InterruptedException {
        HttpRequest requestForDeauthorize = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_OAUTH_ADDRESS + "/deauthorize"))
                .header("Authorization", "Bearer " + accessToken)
                .POST(noBody())
                .build();

        httpClientForStrava.send(requestForDeauthorize, HttpResponse.BodyHandlers.ofString());
    }


}
