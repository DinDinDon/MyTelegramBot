package ru.artak.storage;

import ru.artak.client.strava.StravaCredential;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {

    private static volatile InMemoryStorage instance;

    private final Map<UUID, Long> stateToChatId = new ConcurrentHashMap<>();

    private final Map<Long, StravaCredential> chatIdToAccessToken = new ConcurrentHashMap<>();

    public static InMemoryStorage getInstance() {
        if (instance == null) {
            synchronized (InMemoryStorage.class) {
                if (instance == null) {
                    instance = new InMemoryStorage();
                }
            }
        }
        return instance;
    }

    private InMemoryStorage() {
    }

    @Override
    public void saveStateForUser(UUID state, Long chatId) {
        stateToChatId.put(state, chatId);
    }

    @Override
    public Long getChatIdByState(UUID state) {
        return stateToChatId.get(state);
    }

    @Override
    public void saveStravaCredentials(Long chatId, StravaCredential credential) {
        chatIdToAccessToken.put(chatId, credential);
    }

    @Override
    public StravaCredential getStravaCredentials(Long chatId) {
        return chatIdToAccessToken.get(chatId);
    }

    @Override
    public void removeUser(Long chatId) {
        chatIdToAccessToken.remove(chatId);
    }

}
