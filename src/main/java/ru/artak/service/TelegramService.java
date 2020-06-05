package ru.artak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.artak.client.strava.model.Strava;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.client.telegram.model.Result;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.artak.client.telegram.TelegramClient.TELEGRAM_BASE_URL;
import static ru.artak.client.telegram.TelegramClient.TELEGRAM_TOKEN;

public class TelegramService {
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final Object lock = new Object();
	
	private final Random random = new Random();
	
	private final TelegramClient telegramClient;
	
	private final Storage storage;
	
	// почитай про Dependency injection
	public TelegramService(TelegramClient telegramClient, Storage storage) {
		this.telegramClient = telegramClient;
		this.storage = storage;
	}
	
	public void sendGet() throws IOException, InterruptedException {
		int randomClientID = random.nextInt(100000) + 1;
		// почитай про UUID: UUID.randomUUID()
		
		Integer previousUpdateId = 0;
		while (true) {
			synchronized (lock) {
				// TODO вынести сборку URI в TelegramClient и использовать метод getUpdates()
				URI telegramGetUpdateUrl = URI.create(TELEGRAM_BASE_URL + "/" + TELEGRAM_TOKEN +
					"/getUpdates" +
					"?offset=-1");
				
				HttpResponse<String> telegramGetUpdateResponse = telegramClient.sendMessage(telegramGetUpdateUrl);
				
				GetUpdateTelegram getUpdateTelegram = mapper.readValue(telegramGetUpdateResponse.body(), GetUpdateTelegram.class);
				
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
				Integer lastUpdateId = getUpdateTelegram.getResult().get(getUpdateTelegram.getResult().size()-1).getUpdateId();
				
				if (updateId > previousUpdateId) {
					switch (text) {
						case "/auth":
							handleAuthCommand(randomClientID, chatId);
							break;
						case "/weekDistance":
							handleWeekDistance(chatId);
							// TODO получить количество километров которые набегал за календарную неделю
							break;
						default:
							handleDefaultCommand(chatId);
							break;
					}
					previousUpdateId = updateId;
				} else {
					lock.wait(500);
				}
			}
		}
	}
	
	private void handleWeekDistance(Integer chatId) throws IOException, InterruptedException {
		// TODO вынести сборку URI в TelegramClient
		URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + TELEGRAM_TOKEN + "/sendMessage?chat_id=" +
			chatId + "&text=" + URLEncoder.encode("Скоро здесь что-то будет!", StandardCharsets.UTF_8));
		
		telegramClient.sendMessage(telegramDefaultResponseUrl);
	}
	
	private void handleDefaultCommand(Integer chatId) throws IOException, InterruptedException {
		// TODO вынести сборку URI в TelegramClient
		URI telegramDefaultResponseUrl = URI.create(TELEGRAM_BASE_URL + "/" + TELEGRAM_TOKEN + "/sendMessage?chat_id=" +
			chatId + "&text=" + URLEncoder.encode("Для начало работы выберите  /auth", StandardCharsets.UTF_8));
		
		telegramClient.sendMessage(telegramDefaultResponseUrl);
	}
	
	private void handleAuthCommand(int randomClientID, Integer chatId) throws IOException, InterruptedException {
		// TODO вынести сборку URI в TelegramClient
		URI url = URI.create(TELEGRAM_BASE_URL + "/" + TELEGRAM_TOKEN + "/sendMessage?chat_id=" + chatId + "&text="
			+ URLEncoder.encode(Strava.ADRESS + "authorize?client_id=46301&state=" + randomClientID + "&response_type=code&redirect_uri=http://localhost:8080" +
			"/exchange_token&approval_prompt=force&&scope=activity:read", StandardCharsets.UTF_8));
		
		storage.saveStateForUser(String.valueOf(randomClientID), chatId);
		
		telegramClient.sendMessage(url);
	}
	
}