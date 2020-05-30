package ru.Artak;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {
        Main main = new Main();
//
        main.sendGET();
//        main.sendMes();

    }
//    private void sendMes() throws IOException, InterruptedException {
//        HttpRequest request = null;
//        request = HttpRequest.newBuilder()
//                .GET()
//                .uri(URI.create("https://api.telegram.org/bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8/sendMessage?chat_id=243787596&text=hello"))
//                .build();
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//    }

    private void sendGET() throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        HttpRequest request = null;
        int count = 0;
        while (true) {
            Thread.sleep(500);
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.telegram.org/bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8/getUpdates?offset=-1"))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            HttpHeaders headers = response.headers();
            JsTelegram js = mapper.readValue(response.body(), JsTelegram.class);
            System.out.println(js);

            if (js.getResult().get(0).getUpdate_id() >= count) {

                request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://api.telegram.org/bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8/sendMessage?chat_id=" +
                                js.getResult().get(0).getMessage().getChat().getId() + "&text=hello"))
                        .build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                count = js.getResult().get(0).getUpdate_id() + 1;
            }
            wait();


//		 print status code
//        System.out.println(response.statusCode());

//		 print response body
//        System.out.println(response.body());

//

        }
    }
}




