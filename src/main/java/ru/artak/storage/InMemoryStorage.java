package ru.artak.storage;

import ru.artak.client.strava.model.StravaCredential;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {
	
	private static volatile InMemoryStorage instance;
	
	private final Map<String, Integer> stateToChatId = new ConcurrentHashMap<>();
	
	private final Map<Integer, StravaCredential> chatIdToAccessToken = new ConcurrentHashMap<>();
	
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
	public void saveStravaCredentials(Integer chatId, StravaCredential credential) {
		chatIdToAccessToken.put(chatId, credential);
	}
	
	@Override
	public StravaCredential getStravaCredentials(Integer chatId) {
		return chatIdToAccessToken.get(chatId);
	}
	
}
