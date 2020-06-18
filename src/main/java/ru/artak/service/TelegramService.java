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

    private final String telegramBotDefaultText = "Для начало работы выберите  /auth";

    private final String telegramWeekDistanceText = "Вы пробежали ";

    private final String telegramNoAuthorizationText = "Вы не авторизованы";

    public TelegramService(TelegramClient telegramClient, Storage storage, StravaService stravaService) {
        this.telegramClient = telegramClient;
        this.storage = storage;
        this.stravaService = stravaService;
    }

    public void sendGet() throws IOException, InterruptedException {
        final String randomClientID = UUID.randomUUID().toString().replace("-", "");
        Integer telegramOffset = 0;

        while (true) {
            synchronized (lock) {
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
                            case "/weekDistance":
                                try {
                                    double weekDistance = stravaService.getWeekDistance(chatId);
                                    handleWeekDistance(chatId, telegramWeekDistanceText, weekDistance);
                                } catch (Exception e) {
                                    handleDefaultCommand(chatId, telegramNoAuthorizationText);
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                handleDefaultCommand(chatId, telegramBotDefaultText);
                                break;
                        }
                    }
                    telegramOffset = lastUpdateId;
                }
                lock.wait(500);
            }
        }
    }

    private void handleWeekDistance(Integer chatId, String telegramWeekDistanceText, double weekDistance) throws IOException, InterruptedException {
        telegramClient.sendDistanceText(chatId, telegramWeekDistanceText, weekDistance);
    }

    private void handleDefaultCommand(Integer chatId, String telegramBotStartText) throws IOException, InterruptedException {
        telegramClient.sendSimpleText(chatId, telegramBotStartText);
    }

    private void handleAuthCommand(String randomClientID, Integer chatId) throws IOException, InterruptedException {
        storage.saveStateForUser(randomClientID, chatId);

        telegramClient.sendOauthCommand(randomClientID, chatId);

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