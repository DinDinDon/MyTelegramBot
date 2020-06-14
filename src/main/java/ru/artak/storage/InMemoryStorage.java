package ru.artak.storage;

import ru.artak.client.strava.StravaCredential;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {

    private static volatile InMemoryStorage instance;

    private final Map<String, Integer> stateToChatId = new ConcurrentHashMap<>();

    private final Map<Integer, StravaCredential> chatIdToAccessToken = new ConcurrentHashMap<>();

    private final Map<String, String> stateToAuthorizationCode = new ConcurrentHashMap<>();

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
    public void saveStateForUser(String state, Integer chatId) {
        stateToChatId.put(state, chatId);
    }

    @Override
    public Integer getChatIdByState(String state) {
        return stateToChatId.get(state);
    }

    @Override
    public Integer getLastChatId() {
        Integer[] s = stateToChatId.values().toArray(new Integer[0]);
        return s[s.length - 1];
    }

    @Override
    public String getAuthorizationCode(String state) {
        return stateToAuthorizationCode.get(state);
    }

    @Override
    public void saveAuthorizationCodeForUser(String state, String authorizationCode) {
        stateToAuthorizationCode.put(state, authorizationCode);
    }


    @Override
    public void saveStravaCredentials(Integer chatId, StravaCredential credential) {
        chatIdToAccessToken.put(chatId, credential);
    }

    @Override
    public StravaCredential getStravaCredentials(Integer chatId) {
        return chatIdToAccessToken.get(chatId);
    }

}
