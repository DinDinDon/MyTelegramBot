package ru.Artak;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class Bot {
    private final String TOKEN = "bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8";
    private final String ADRESS = "https://api.telegram.org";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();
    private final Object lock = new Object();

    void sendGet() throws IOException, InterruptedException {
        HttpRequest request = null;
        Integer previousUpteId = 0;
        while (true) {
            synchronized (lock) {
                request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(ADRESS + "/" + TOKEN + "/getUpdates?offset=-1"))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                HttpHeaders headers = response.headers();
                JsTelegram js = mapper.readValue(response.body(), JsTelegram.class);
                Integer updateId = js.getResult().get(0).getUpdate_id();
                Integer chatId = js.getResult().get(0).getMessage().getChat().getId();
                String text = js.getResult().get(0).getMessage().getText();

                if (updateId > previousUpteId) {
                    switch (text) {
                        case "/auth":
                            request = HttpRequest.newBuilder()
                                    .GET()
                                    .uri(URI.create(ADRESS + "/" + TOKEN + "/sendMessage?chat_id=" +
                                            chatId + "&text=" + URLEncoder.encode(Strava.ADRESS + "authorize?client_id=46301&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=activity:read", StandardCharsets.UTF_8)))
                                    .build();
                            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                            //пока ручками ставлю
                            String authorizationCode = "b4dc2d4819d5b39e78eeea5d0bc0c1daffe1eda3";
                            request = HttpRequest.newBuilder()
                                    .uri(URI.create(Strava.ADRESS + "token?client_id=46301&client_secret=" + Strava.SECRET + "&code=" + authorizationCode + "&grant_type=authorization_code"))
                                    .header("Authorization", "Bearer " + Strava.SECRET)
                                    .POST(noBody())
                                    .build();
                            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                            System.out.println(response.statusCode());
                            Strava strava = mapper.readValue(response.body(), Strava.class);

                            break;
                        default:
                            request = HttpRequest.newBuilder()
                                    .GET()
                                    .uri(URI.create(ADRESS + "/" + TOKEN + "/sendMessage?chat_id=" +
                                            chatId + "&text=" + URLEncoder.encode("Для начало работы выберите  /auth", StandardCharsets.UTF_8)))
                                    .build();
                            break;
                    }
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    headers = response.headers();
                    previousUpteId = updateId;

                } else {
                    lock.wait(500);
                }


            }
        }
    }


}