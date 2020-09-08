package ru.artak.client.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class TelegramClient {

    private static final Logger logger = LogManager.getLogger(TelegramClient.class);

    public static final String TELEGRAM_BASE_URL = "https://api.telegram.org/bot";

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private final String telegramToken;

    private final int stravaClientId;

    private final String baseRedirectUrl;

    public TelegramClient(String telegramToken, int stravaClientId, String baseRedirectUrl) {
        this.telegramToken = telegramToken;
        this.stravaClientId = stravaClientId;
        this.baseRedirectUrl = baseRedirectUrl;
    }

    public GetUpdateTelegram getUpdates(Integer offset) throws IOException, InterruptedException {
        URI telegramGetUpdateUrl = URI.create(TELEGRAM_BASE_URL + telegramToken +
                "/getUpdates?offset=" + (offset + 1));
        HttpResponse<String> telegramGetUpdateResponse = sendMessage(telegramGetUpdateUrl);

        return mapper.readValue(telegramGetUpdateResponse.body(), GetUpdateTelegram.class);
    }

    public void sendSimpleText(Long chatId, String commandText) throws IOException, InterruptedException {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode(commandText, StandardCharsets.UTF_8));
        sendMessage(telegramDefaultResponseUrl);

    }

    public void sendDistanceText(Long chatId, String commandText, Number weekDistance) throws IOException, InterruptedException {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode(commandText, StandardCharsets.UTF_8) + weekDistance + "Км");
        sendMessage(telegramDefaultResponseUrl);
        logger.info("sent method /weekDistance data to user - {}", chatId);

    }

    public void sendOauthCommand(UUID randomClientID, Long chatId) throws IOException, InterruptedException {
        URI oauthUrl = URI.create(TELEGRAM_BASE_URL + telegramToken + "/sendMessage?chat_id=" + chatId + "&text="
                + URLEncoder.encode(StravaClient.STRAVA_OAUTH_ADDRESS + "authorize?client_id=" +
                stravaClientId + "&state=" + randomClientID + "&response_type=code&redirect_uri=" + baseRedirectUrl +
                "/exchange_token&approval_prompt=force&&scope=activity:read", StandardCharsets.UTF_8));
        sendMessage(oauthUrl);
        logger.debug("sent to user - {}, randomClientID - {} Oauth link", chatId, randomClientID);
    }

    private HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }


}
