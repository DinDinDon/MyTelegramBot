package ru.artak.service;

public class TelegramUserInfo {
	
	private final Integer chatId;
	private final String text;
	
	public TelegramUserInfo(Integer chatId, String text) {
		this.chatId = chatId;
		this.text = text;
	}
	
	public Integer getChatId() {
		return chatId;
	}
	
	public String getText() {
		return text;
	}
}
