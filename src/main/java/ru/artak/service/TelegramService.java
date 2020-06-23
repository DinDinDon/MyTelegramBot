package ru.artak.service;

import ru.artak.client.telegram.TelegramClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TelegramService {

    private final Object lock = new Object();

    private final TelegramClient telegramClient;

    private final StravaService stravaService;

    private final Storage storage;

    private final String telegramBotDefaultText =
            "Телеграм бот для работы с ресурсом Strava.com\n" +
                    "\n" +
                    "Список доступных команд:\n" +
                    "\n" +
                    "/auth - авторизация через OAuth\n" +
                    "/weekdistance - набеганное расстояние за календарную неделю\n" +
                    "/deauthorize - деавторизация\n ";

    private final String whenUserAlreadyAuthorized = "Вы уже авторизованы!";
    private final String telegramNoAuthorizationText = "Вы не авторизованы. Используйте команду /auth";
    private final String telegramDeauthorizeText = "Вы деавторизированы!";

    private final String telegramWeekDistanceText = "Вы пробежали ";


    public TelegramService(TelegramClient telegramClient, Storage storage, StravaService stravaService) {
        this.telegramClient = telegramClient;
        this.storage = storage;
        this.stravaService = stravaService;
    }

    public void sendGet() {
        final String randomClientID = UUID.randomUUID().toString().replace("-", "");
        Integer telegramOffset = 0;

        while (true) {
            synchronized (lock) {
                try {
                    GetUpdateTelegram getUpdateTelegram = telegramClient.getUpdates(telegramOffset);

                    List<TelegramUserInfo> updateIds = getAllTelegramUpdateUsers(getUpdateTelegram);

                    for (TelegramUserInfo id : updateIds) {
                        Integer lastUpdateId = getUpdateTelegram.getResult().get(getUpdateTelegram.getResult().size() - 1).getUpdateId();
                        Integer updateId = id.getUpdateId();
                        Integer chatId = id.getChatId();
                        String text = id.getText();

                        if (updateId <= lastUpdateId) {
                            switch (text) {
                                case "/auth":
                                    handleAuthCommand(randomClientID, chatId);
                                    break;
                                case "/weekdistance":
                                    handleWeekDistance(chatId);
                                    break;
                                case "/deauthorize":
                                    handleDeauthorizeCommand(chatId);
                                    break;
                                default:
                                    handleDefaultCommand(chatId, telegramBotDefaultText);
                                    break;
                            }
                        }
                        telegramOffset = lastUpdateId;
                    }
                    lock.wait(500);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void handleWeekDistance(Integer chatId) throws IOException, InterruptedException {
        try {
            stravaService.accessIsAlive(chatId);
            Number weekDistance = stravaService.getRunningWeekDistance(chatId);
            telegramClient.sendDistanceText(chatId, telegramWeekDistanceText, weekDistance);
        } catch (Exception e) {
            handleDefaultCommand(chatId, telegramNoAuthorizationText);
            e.printStackTrace();
        }
    }

    private void handleDefaultCommand(Integer chatId, String anyText) throws IOException, InterruptedException {
        telegramClient.sendSimpleText(chatId, anyText);
    }

    private void handleAuthCommand(String randomClientID, Integer chatId) throws IOException, InterruptedException {
        try {
            storage.getStravaCredentials(chatId).getAccessToken();
            handleDefaultCommand(chatId, whenUserAlreadyAuthorized);
        } catch (Exception e) {
            storage.saveStateForUser(randomClientID, chatId);
            telegramClient.sendOauthCommand(randomClientID, chatId);
            e.printStackTrace();
        }
    }

    private void handleDeauthorizeCommand(Integer chatId) throws IOException, InterruptedException {
        try {
            stravaService.deauthorize(chatId);
            telegramClient.sendSimpleText(chatId, telegramDeauthorizeText);
        } catch (Exception e) {
            telegramClient.sendSimpleText(chatId, telegramNoAuthorizationText);
            e.printStackTrace();
        }
    }

    private List<TelegramUserInfo> getAllTelegramUpdateUsers(GetUpdateTelegram getUpdateTelegram) {
        return getUpdateTelegram.getResult().stream()
                .map(result ->
                        new TelegramUserInfo(
                                result.getMessage().getChat().getId(),
                                result.getMessage().getText(),
                                result.getUpdateId()))
                .collect(Collectors.toList());
    }

}