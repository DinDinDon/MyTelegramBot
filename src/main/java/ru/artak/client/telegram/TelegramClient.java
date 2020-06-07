package ru.artak.client.telegram;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramClient {


    public static final String TELEGRAM_BASE_URL = "https://api.telegram.org";
    public static String telegramToken;


    private final HttpClient httpClient =
            HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();


    public TelegramClient(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    // TODO реализовать метод
    public void getUpdates() {

    }

    public HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }


}
