package ru.artak.client.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.client.telegram.model.Result;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TelegramClient {


    public static final String TELEGRAM_BASE_URL = "https://api.telegram.org";
    public static String telegramToken;
    private final ObjectMapper mapper = new ObjectMapper();


    private final HttpClient httpClient =
            HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();


    public TelegramClient(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    // TODO реализовать метод
    public GetUpdateTelegram getUpdates() throws IOException, InterruptedException {
        URI telegramGetUpdateUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken +
                "/getUpdates" +
                "?offset=-1");
        HttpResponse<String> telegramGetUpdateResponse = sendMessage(telegramGetUpdateUrl);
        GetUpdateTelegram getUpdateTelegram = mapper.readValue(telegramGetUpdateResponse.body(), GetUpdateTelegram.class);

        return getUpdateTelegram;
    }


    public URI getDefaultTelegramUri(Integer chatId) {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode("Для начало работы выберите  /auth", StandardCharsets.UTF_8));
        return telegramDefaultResponseUrl;
    }


    public URI getWeekDistanceUrl(Integer chatId) {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + TelegramClient.telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode("Скоро здесь что-то будет!", StandardCharsets.UTF_8));
        return telegramDefaultResponseUrl;
    }


    public URI getAuthCommandUrl(String randomClientID, Integer chatId) {
        URI url = URI.create(TELEGRAM_BASE_URL + "/" + telegramToken + "/sendMessage?chat_id=" + chatId + "&text="
                + URLEncoder.encode(StravaClient.STRAVA_OAUTH_ADDRESS + "authorize?client_id=" +
                StravaClient.stravaClientId + "&state=" + randomClientID + "&response_type=code&redirect_uri=http://localhost:8080" +
                "/exchange_token&approval_prompt=force&&scope=activity:read", StandardCharsets.UTF_8));
        return url;
    }


    public HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }


}
