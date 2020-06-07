package ru.artak.service;

import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;

public class StravaService {
	
	private final TelegramClient telegramClient;
	
	// TODO использовать StravaClient
	
	private final Storage storage;
	
	public StravaService(TelegramClient telegramClient, Storage storage) {
		this.telegramClient = telegramClient;
		this.storage = storage;
	}
	
	public void obtainCredentials(String state, String authorizationCode) {
		// 1. Сходить в strava API и получить по authorizationCode токены
		// 2. Сохранить полученные на предыдущем шаге токены в storage.saveStravaCredentials()
		// 3. Оповестить пользователя о том, что мы его авторизовали (в случае успеха) через telegramClient. Взять chatId из storage.getChatIdByState()


//        //пока ручками ставлю
//        String authorizationCode = "1de9a1874e73c9aa4e4c14525b580cd5a17f4a08";
//
//        HttpRequest requestPostStrava = HttpRequest.newBuilder()
//            .uri(URI.create(Strava.ADRESS + "token?client_id=46301&client_secret=" + Strava.SECRET + "&code=" + authorizationCode + "&grant_type=authorization_code"))
//            .header("Authorization", "Bearer " + Strava.SECRET)
//            .POST(noBody())
//            .build();
//        HttpResponse<String> stravaAccessToken = httpClient.send(requestPostStrava, HttpResponse.BodyHandlers.ofString());
//
//        Strava strava = mapper.readValue(stravaAccessToken.body(), Strava.class);
//
//        System.out.println(stravaAccessToken.statusCode());
//        System.out.println();
	}
	
}
