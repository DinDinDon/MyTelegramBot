package ru.artak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.util.UUID;

public class TelegramService {

    private final ObjectMapper mapper = new ObjectMapper();

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

        // почитай про UUID: UUID.randomUUID()

        Integer previousUpdateId = 0;
        while (true) {
            synchronized (lock) {
                // TODO вынести сборку URI в TelegramClient и использовать метод getUpdates()
                getUpdateTelegram = telegramClient.getUpdates();
                // TODO сделать обработку всех сообщений, а не только первого (...get(0)), использовать закомментированный  ниже код
//				List<TelegramUserInfo> updateIds = new ArrayList<>();
//				for (Result result : getUpdateTelegram.getResult()) {
//					TelegramUserInfo telegramUserInfo =
//						new TelegramUserInfo(result.getMessage().getChat().getId(), result.getMessage().getText());
//					updateIds.add(telegramUserInfo);
//				}

                Integer updateId = getUpdateTelegram.getResult().get(0).getUpdateId();
                Integer chatId = getUpdateTelegram.getResult().get(0).getMessage().getChat().getId();
                String text = getUpdateTelegram.getResult().get(0).getMessage().getText();


                // TODO сравнивать с updateId последнего
//				Integer lastUpdateId = getUpdateTelegram.getResult().get(getUpdateTelegram.getResult().size()-1).getUpdateId();

                if (updateId > previousUpdateId) {
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
                    previousUpdateId = updateId;
                } else {
                    lock.wait(500);
                }
            }
        }
    }

    private void handleWeekDistance(Integer chatId, String telegramWeekDistanceText) throws IOException, InterruptedException {
        // TODO вынести сборку URI в TelegramClient
        telegramClient.sendSimpleText(chatId, telegramWeekDistanceText);
    }

    private void handleDefaultCommand(Integer chatId, String telegramBotStartText) throws IOException, InterruptedException {
        // TODO вынести сборку URI в TelegramClient
        telegramClient.sendSimpleText(chatId, telegramBotStartText);
    }

    private void handleAuthCommand(String randomClientID, Integer chatId) throws IOException, InterruptedException {
        // TODO вынести сборку URI в TelegramClient

        storage.saveStateForUser(randomClientID, chatId);

        telegramClient.sendOauthCommand(randomClientID, chatId);
    }

}