package ru.artak.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

public class UpdateHandlerImpl {
    UpdateHandler updateHandler;

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public BotApiMethod<Serializable> executeUpdate(Update update) {
        return updateHandler.executeUpdate(update);
    }

}
