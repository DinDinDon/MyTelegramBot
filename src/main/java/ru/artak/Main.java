package ru.artak;

import org.apache.commons.lang3.StringUtils;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.server.BotHttpServer;
import ru.artak.service.StravaService;
import ru.artak.service.TelegramService;
import ru.artak.storage.InMemoryStorage;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        // чтение конфигурации, должны быть заданы параметры запуска или переменные окружения. Приоритет у переменных окружения.
        String telegramToken = System.getenv("TELEGRAM_TOKEN");
        String stravaClientSecret = System.getenv("STRAVA_CLIENT_SECRET");
        String stravaBaseRedirectUrl = System.getenv("STRAVA_BASE_REDIRECT_URL");
    
        String stravaClientIdString = System.getenv("STRAVA_CLIENT_ID");
        int stravaClientId = 0;
        if (StringUtils.isNotBlank(stravaClientIdString)) stravaClientId = Integer.parseInt(stravaClientIdString);
    
        String portString = System.getenv("PORT");
        int port = 8080;
        if (StringUtils.isNotBlank(portString)) port = Integer.parseInt(portString);
    
        if (args.length < 3 && (StringUtils.isBlank(telegramToken) && stravaClientId == 0 && StringUtils.isBlank(stravaClientSecret))) {
            throw new IllegalArgumentException("Please define required configuration variables");
        }
    
        if (StringUtils.isBlank(telegramToken)) telegramToken = args[0];
        if (StringUtils.isBlank(stravaClientSecret)) stravaClientSecret = args[1];
        if (stravaClientId == 0) stravaClientId = Integer.parseInt(args[2]);
        if (StringUtils.isBlank(stravaBaseRedirectUrl)) stravaBaseRedirectUrl = "http://localhost:8080";
    
        if (StringUtils.isBlank(telegramToken)) {
            throw new IllegalArgumentException("doesn't define telegram token");
        }
        if (StringUtils.isBlank(stravaClientSecret)) {
            throw new IllegalArgumentException("doesn't define strava client secret");
        }
        if (stravaClientId == 0) {
            throw new IllegalArgumentException("doesn't define strava client id");
        }
    
        // инициализация зависимостей
        StravaClient stravaClient = new StravaClient(stravaClientId, stravaClientSecret);
        TelegramClient telegramClient = new TelegramClient(telegramToken, stravaClientId, stravaBaseRedirectUrl);
    
        InMemoryStorage inMemoryStorage = InMemoryStorage.getInstance();
    
        StravaService stravaService = new StravaService(telegramClient, inMemoryStorage, stravaClient);
        BotHttpServer botHttpServer = new BotHttpServer(stravaService, port);
    
        // запуск сервера
        Thread httpServerThread;
        try {
            httpServerThread = new Thread(() -> {
                try {
                    botHttpServer.run();
                } catch (IOException e) {
                    throw new RuntimeException("http server doesn't started", e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("error on create http server thread", e);
        }
        httpServerThread.start();


//        TelegramService telegramService = new TelegramService(telegramClient, inMemoryStorage, stravaService);

        System.out.println("started http server");
    
        // инициализация и запуск обработчика запросов telegram api
        TelegramService telegramService = new TelegramService(telegramClient, inMemoryStorage, stravaService);
        System.out.println("starting telegram bot handler");
        telegramService.sendGet();
    }
}




