package ru.artak.client.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


public class TelegramClient {

    public static final String TELEGRAM_BASE_URL = "https://api.telegram.org";

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient =
            HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private String telegramToken;
    private int stravaClientId;

    public TelegramClient(String telegramToken, int stravaClientId) {
        this.telegramToken = telegramToken;
        this.stravaClientId = stravaClientId;
    }

    public GetUpdateTelegram getUpdates(Integer offset) throws IOException, InterruptedException {
        URI telegramGetUpdateUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken +
                "/getUpdates?offset=" + (offset + 1));
        HttpResponse<String> telegramGetUpdateResponse = sendMessage(telegramGetUpdateUrl);

        return mapper.readValue(telegramGetUpdateResponse.body(), GetUpdateTelegram.class);
    }

    public void sendSimpleText(Integer chatId, String commandText) throws IOException, InterruptedException {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode(commandText, StandardCharsets.UTF_8));
        sendMessage(telegramDefaultResponseUrl);

    }

    public void sendDistanceText(Integer chatId, String commandText, double weekDistance) throws IOException, InterruptedException {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode(commandText, StandardCharsets.UTF_8)+weekDistance+"Км");
        sendMessage(telegramDefaultResponseUrl);

    }

    public void sendOauthCommand(String randomClientID, Integer chatId) throws IOException, InterruptedException {
        URI oauthUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken + "/sendMessage?chat_id=" + chatId + "&text="
                + URLEncoder.encode(StravaClient.STRAVA_OAUTH_ADDRESS + "authorize?client_id=" +
                stravaClientId + "&state=" + randomClientID + "&response_type=code&redirect_uri=http://localhost:8080" +
                "/exchange_token&approval_prompt=force&&scope=activity:read", StandardCharsets.UTF_8));
        sendMessage(oauthUrl);
    }

    private HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }


}
