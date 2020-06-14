package ru.artak.service;

import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.StravaOauthResp;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;

import java.io.IOException;
import java.util.Date;


public class StravaService {

    private static final String AUTHORIZED_TEXT = "Strava аккаунт был успешно подключен!";
    private static final String FAILED_AUTHORIZED_TEXT = "Strava аккаунт не был подключен!";
    private final TelegramClient telegramClient;
    private final StravaClient stravaClient;
    private final Storage storage;

    public StravaService(TelegramClient telegramClient, Storage storage, StravaClient stravaClient) {
        this.telegramClient = telegramClient;
        this.storage = storage;
        this.stravaClient = stravaClient;
    }

    public void obtainCredentials(String state, String authorizationCode) throws IOException, InterruptedException {
        if (authorizationCode.equals(storage.getAuthorizationCode(state))) {
            StravaOauthResp strava = stravaClient.getStravaCredentials(authorizationCode);
            Integer chatID = storage.getChatIdByState(state);
            StravaCredential stravaCredential = new StravaCredential(strava.getAccessToken(), strava.getRefreshToken(), new Date());
            storage.saveStravaCredentials(chatID, stravaCredential);
            telegramClient.sendSimpleText(chatID, AUTHORIZED_TEXT);
        }
    }

    public void sendFailedAuthorizedText() throws IOException, InterruptedException {
        telegramClient.sendSimpleText(storage.getLastChatId(), FAILED_AUTHORIZED_TEXT);
    }

}
