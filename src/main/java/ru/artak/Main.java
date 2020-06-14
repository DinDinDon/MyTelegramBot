package ru.artak;

import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.server.BotHttpServer;
import ru.artak.service.StravaService;
import ru.artak.service.TelegramService;
import ru.artak.storage.InMemoryStorage;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        String telegramToken = args[0];
        String stravaClientSecret = args[1];
        Integer stravaClientId = Integer.valueOf(args[2]);

        StravaClient stravaClient = new StravaClient(stravaClientId, stravaClientSecret);
        TelegramClient telegramClient = new TelegramClient(telegramToken, stravaClient);

        InMemoryStorage inMemoryStorage = InMemoryStorage.getInstance();

        StravaService stravaService = new StravaService(telegramClient, inMemoryStorage, stravaClient);
        BotHttpServer botHttpServer = new BotHttpServer(stravaService, inMemoryStorage);

        Thread httpServerThread = new Thread(() -> {
            try {
                botHttpServer.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        httpServerThread.start();

        TelegramService telegramService = new TelegramService(telegramClient, inMemoryStorage);
        telegramService.sendGet();

    }
}




