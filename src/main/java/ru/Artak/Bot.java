package ru.Artak;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Bot {
    private final String TOKEN = "bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8";
    private final String ADRESS = "https://api.telegram.org";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    ObjectMapper mapper = new ObjectMapper();


    private final Object lock = new Object();

    public Bot() {

    }


//    StringBuilder builder = new StringBuilder();

    void sendGET() throws IOException, InterruptedException {
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

                if (updateId > previousUpteId) {
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(ADRESS + "/" + TOKEN + "/sendMessage?chat_id=" +
                                    chatId + "&text=hello"))
                            .build();
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    previousUpteId = updateId;

                } else lock.wait(500);


            }
        }
    }


}