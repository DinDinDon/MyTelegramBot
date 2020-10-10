package ru.artak.client.telegram;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TelegramClient  {

    private final String TELEGRAM_BASE_URL = "https://api.telegram.org/bot";

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private final String telegramToken;

    public TelegramClient(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public void sendSimpleText(Long chatId, String commandText) throws IOException, InterruptedException {
        URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + telegramToken + "/sendMessage?chat_id=" +
                chatId + "&text=" + URLEncoder.encode(commandText, StandardCharsets.UTF_8));

        sendMessage(telegramDefaultResponseUrl);
    }

    private HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
