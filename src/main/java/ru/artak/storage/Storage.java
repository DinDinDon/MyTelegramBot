package ru.artak.storage;

import ru.artak.client.strava.StravaCredential;

import java.util.UUID;

public interface Storage {

    void saveStateForUser(UUID state, Long chatId);

    Long getChatIdByState(UUID state);

    void saveStravaCredentials(Long chatId, StravaCredential credential);

    StravaCredential getStravaCredentials(Long chatId);

    void removeUser(Long chatId);


}
