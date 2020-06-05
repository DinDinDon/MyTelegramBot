package ru.artak.client.telegram;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramClient {
	
	// TODO получить переменные через main(String[] args), передавая в конструктор
	public static final String TELEGRAM_TOKEN = "bot1263503443:AAG5Dz5XzWNjscLVjgl_kDEJqLx4zUlcJy8";
	public static final String TELEGRAM_BASE_URL = "https://api.telegram.org";
	
	private final HttpClient httpClient =
		HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	
	
	public TelegramClient() {
	}
	
	// TODO реализовать метод
	public void getUpdates(){
	
	}
	
	public HttpResponse<String> sendMessage(URI uri) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
		
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
}
