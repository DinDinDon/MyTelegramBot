package ru.artak.storage;

import ru.artak.client.strava.model.StravaCredential;

public interface Storage {
	
	void saveStateForUser(String state, Integer chatId);
	
	Integer getChatIdByState(String state);
	
	void saveStravaCredentials(Integer chatId, StravaCredential credential);
	
	StravaCredential getStravaCredentials(Integer chatId);
}
