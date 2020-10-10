package ru.artak.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandlStrategy {
    UpdateHandl updateHandl;

    public void setUpdateHandl(UpdateHandl updateHandl) {
        this.updateHandl = updateHandl;
    }

    public BotApiMethod<Message> executeUpdate(Update update){
       return updateHandl.executeUpdate(update);
    }

}
