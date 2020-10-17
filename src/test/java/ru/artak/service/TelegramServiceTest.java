package ru.artak.service;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.artak.client.strava.StravaCredential;
import ru.artak.storage.Storage;

import java.util.UUID;

public class TelegramServiceTest {
	
	private Storage storage = Mockito.mock(Storage.class);
	
	private StravaService stravaService = Mockito.mock(StravaService.class);
	
	private static final int STRAVA_CLIENT_ID = 111;
	private static final String BASE_REDIRECT_URI = "redirectUri";
	
	private TelegramService service = new TelegramService(storage, stravaService, STRAVA_CLIENT_ID, BASE_REDIRECT_URI);
	
	@Test
	public void shouldBuildAuthUrlSuccess() {
		// given
		Long chatId = 1L;
		
		Chat chat = new Chat();
		ReflectionTestUtils.setField(chat, "id", chatId);
		
		Message message = new Message();
		ReflectionTestUtils.setField(message, "chat", chat);
		ReflectionTestUtils.setField(message, "text", "/auth");
		
		Update update = new Update();
		ReflectionTestUtils.setField(update, "message", message);
		
		Mockito.when(storage.getStravaCredentials(chatId))
			.thenReturn(new StravaCredential("accessToken", "refreshToken", 0L, true));
		
		// when
		SendMessage actualResult = (SendMessage) service.executeUpdate(update);
		
		// then
		ArgumentCaptor<UUID> stateCaptor = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Long> chatIdCaptor = ArgumentCaptor.forClass(Long.class);
		
		Mockito.verify(storage).saveStateForUser(stateCaptor.capture(), chatIdCaptor.capture());
		
		UUID expectedState = stateCaptor.getValue();
		
		String expectedTest = "https://www.strava.com/oauth/authorize?client_id=" + STRAVA_CLIENT_ID +
			"&state=" + expectedState + "&response_type=code&redirect_uri=" + BASE_REDIRECT_URI
			+ "/exchange_token&approval_prompt=force&&scope=activity:read";
		
		SendMessage expectedResult = new SendMessage(chatId, expectedTest);
		
		Assertions.assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void shouldReturnAlreadyAuthorizeMessage() {
		// given
		Long chatId = 1L;
		
		Chat chat = new Chat();
		ReflectionTestUtils.setField(chat, "id", chatId);
		
		Message message = new Message();
		ReflectionTestUtils.setField(message, "chat", chat);
		ReflectionTestUtils.setField(message, "text", "/auth");
		
		Update update = new Update();
		ReflectionTestUtils.setField(update, "message", message);
		
		Mockito.when(storage.getStravaCredentials(chatId))
			.thenReturn(new StravaCredential("accessToken", "refreshToken", 0L, false));
		
		// when
		SendMessage actualResult = (SendMessage) service.executeUpdate(update);
		
		// then
		String expectedTest = "Вы уже авторизованы!";
		
		SendMessage expectedResult = new SendMessage(chatId, expectedTest);
		
		Assertions.assertEquals(expectedResult, actualResult);
	}
	
}