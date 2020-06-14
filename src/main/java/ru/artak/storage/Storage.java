package ru.artak.storage;

import ru.artak.client.strava.StravaCredential;

public interface Storage {

    void saveStateForUser(String state, Integer chatId);

    Integer getChatIdByState(String state);

    void saveStravaCredentials(Integer chatId, StravaCredential credential);

    StravaCredential getStravaCredentials(Integer chatId);

    Integer getLastChatId();

    String getAuthorizationCode(String state);

    public void saveAuthorizationCodeForUser(String state, String authorizationCode);
}
