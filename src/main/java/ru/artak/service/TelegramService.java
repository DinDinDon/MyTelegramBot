package ru.artak.service;

import ru.artak.client.telegram.TelegramClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class TelegramService {

    private final Object lock = new Object();

    private final TelegramClient telegramClient;

    private final Storage storage;

    private final String telegramBotDefaultText = "Для начало работы выберите  /auth";

    private final String telegramWeekDistanceText = "Скоро здесь что-то будет!";

    private GetUpdateTelegram getUpdateTelegram;




    // почитай про Dependency injection
    public TelegramService(TelegramClient telegramClient, Storage storage) {
        this.telegramClient = telegramClient;
        this.storage = storage;
    }

    public void sendGet() throws IOException, InterruptedException {

        final String randomClientID = UUID.randomUUID().toString().replace("-", "");

        Integer telegramOfset = 0;


        while (true) {
            synchronized (lock) {
                getUpdateTelegram = telegramClient.getUpdates(telegramOfset);

                List<TelegramUserInfo> updateIds = telegramClient.getAllTelegramUpdateUsers(getUpdateTelegram);

                for (int i = 0; i < updateIds.size(); i++) {
                    Integer lastUpdateId = getUpdateTelegram.getResult().get(getUpdateTelegram.getResult().size() - 1).getUpdateId();
                    Integer updateId = updateIds.get(i).getUpdateId();
                    Integer chatId = updateIds.get(i).getChatId();
                    String text = updateIds.get(i).getText();

                    if (updateId <= lastUpdateId) {
                        switch (text) {
                            case "/auth":
                                handleAuthCommand(randomClientID, chatId);
                                break;
                            case "/weekDistance":
                                handleWeekDistance(chatId, telegramWeekDistanceText);
                                // TODO получить количество километров которые набегал за календарную неделю
                                break;
                            default:
                                handleDefaultCommand(chatId, telegramBotDefaultText);
                                break;
                        }

                    }
                    telegramOfset = lastUpdateId;
                }
                lock.wait(500);


            }
        }
    }

    private void handleWeekDistance(Integer chatId, String telegramWeekDistanceText) throws IOException, InterruptedException {
        telegramClient.sendSimpleText(chatId, telegramWeekDistanceText);
    }

    private void handleDefaultCommand(Integer chatId, String telegramBotStartText) throws IOException, InterruptedException {
        telegramClient.sendSimpleText(chatId, telegramBotStartText);
    }

    private void handleAuthCommand(String randomClientID, Integer chatId) throws IOException, InterruptedException {
        storage.saveStateForUser(randomClientID, chatId);

        telegramClient.sendOauthCommand(randomClientID, chatId);
    }

}