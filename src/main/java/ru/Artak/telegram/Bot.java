package ru.Artak.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.Artak.telegram.modul.JsTelegram;
import ru.Artak.strava.modul.Strava;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class Bot {
    private final String TOKEN = "bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8";
    private final String ADRESS = "https://api.telegram.org";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();
    private final Object lock = new Object();
    Map<String, Integer> map = new HashMap<>();
    private Random random = new Random();


    public void sendGet() throws IOException, InterruptedException {
        int randomClientID = random.nextInt(100000)+1;
        Integer previousUpdateId = 0;
        while (true) {
            synchronized (lock) {
                HttpRequest requestGetUpdate = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(ADRESS + "/" + TOKEN + "/getUpdates?offset=-1"))
                        .build();
                HttpResponse<String> response = httpClient.send(requestGetUpdate, HttpResponse.BodyHandlers.ofString());
                HttpHeaders headers = response.headers();
                JsTelegram js = mapper.readValue(response.body(), JsTelegram.class);
                Integer updateId = js.getResult().get(0).getUpdate_id();
                Integer chatId = js.getResult().get(0).getMessage().getChat().getId();
                String text = js.getResult().get(0).getMessage().getText();

                if (updateId > previousUpdateId) {
                    switch (text) {
                        case "/auth":
                            HttpRequest requestRedirectAuthorizeStrava = HttpRequest.newBuilder()
                                    .GET()
                                    .uri(URI.create(ADRESS + "/" + TOKEN + "/sendMessage?chat_id=" +
                                            chatId + "&text="
                                            + URLEncoder.encode(Strava.ADRESS + "authorize?client_id=46301&state="+randomClientID+"&response_type=code&redirect_uri=http://localhost:8080" +
                                            "/exchange_token&approval_prompt=force&&scope=activity:read", StandardCharsets.UTF_8)))
                                    .build();
//                            map.put("12345",chatId);
                            HttpResponse<String> responseRedirectStrava = httpClient.send(requestRedirectAuthorizeStrava, HttpResponse.BodyHandlers.ofString());
                            //пока ручками ставлю
                            String authorizationCode = "1de9a1874e73c9aa4e4c14525b580cd5a17f4a08";
                            HttpRequest requestPostStrava = HttpRequest.newBuilder()
                                    .uri(URI.create(Strava.ADRESS + "token?client_id=46301&client_secret=" + Strava.SECRET + "&code=" + authorizationCode + "&grant_type=authorization_code"))
                                    .header("Authorization", "Bearer " + Strava.SECRET)
                                    .POST(noBody())
                                    .build();
                            HttpResponse<String> responsePostStrava = httpClient.send(requestPostStrava, HttpResponse.BodyHandlers.ofString());
                            System.out.println(responsePostStrava.statusCode());
                            Strava strava = mapper.readValue(responsePostStrava.body(), Strava.class);
                            System.out.println();

                            break;
                        default:
                            HttpRequest requestDefaultAnswer = HttpRequest.newBuilder()
                                    .GET()
                                    .uri(URI.create(ADRESS + "/" + TOKEN + "/sendMessage?chat_id=" +
                                            chatId + "&text=" + URLEncoder.encode("Для начало работы выберите  /auth", StandardCharsets.UTF_8)))
                                    .build();
                            HttpResponse<String> responseDefault = httpClient.send(requestDefaultAnswer, HttpResponse.BodyHandlers.ofString());
                            break;
                    }


                    previousUpdateId = updateId;

                } else {
                    lock.wait(500);
                }


            }
        }
    }


}