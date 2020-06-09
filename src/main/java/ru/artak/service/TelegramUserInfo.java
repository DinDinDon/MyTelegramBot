package ru.artak.service;

public class TelegramUserInfo {

    private final Integer chatId;
    private final String text;
    private final Integer updateId;

    public TelegramUserInfo(Integer chatId, String text, Integer updateId) {
        this.chatId = chatId;
        this.text = text;
        this.updateId = updateId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }

    public Integer getUpdateId() { return updateId; }
}
