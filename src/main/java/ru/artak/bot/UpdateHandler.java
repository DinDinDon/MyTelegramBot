package ru.artak.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    BotApiMethod<Message> executeUpdate(Update update);
}
