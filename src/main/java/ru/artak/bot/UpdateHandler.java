package ru.artak.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

public interface UpdateHandler {
    <T extends Serializable, Method extends BotApiMethod<T>> Method executeUpdate(Update update);
}
