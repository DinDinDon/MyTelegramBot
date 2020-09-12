package ru.artak.service;

public class TelegramUserInfo {

    private final Long chatId;
    private final String text;
    private final Integer updateId;

    public TelegramUserInfo(Long chatId, String text, Integer updateId) {
        this.chatId = chatId;
        this.text = text;
        this.updateId = updateId;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }

    public Integer getUpdateId() {
        return updateId;
    }
}
