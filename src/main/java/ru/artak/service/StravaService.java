package ru.artak.service;

import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.Strava;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;

import java.io.IOException;


public class StravaService {
	
	private final TelegramClient telegramClient;
	private final StravaClient stravaClient;
	private final String authorizedText = "Strava аккаунт был успешно подключен!";
	private final Storage storage;
	
	public StravaService(TelegramClient telegramClient, Storage storage, StravaClient stravaClient) {
		this.telegramClient = telegramClient;
		this.storage = storage;
		this.stravaClient = stravaClient;
	}
	

	public void obtainCredentials(String state, String authorizationCode) throws IOException, InterruptedException {
		Strava strava = stravaClient.getUpdateStrava(authorizationCode);
		storage.saveStravaCredentials(storage.getChatIdByState(state),new StravaCredential(strava.getAccessToken(),strava.getRefreshToken()));
		if(storage.getChatIdByState(state) != 0)
		telegramClient.sendSimpleText(storage.getChatIdByState(state),authorizedText);
	}
	
}
