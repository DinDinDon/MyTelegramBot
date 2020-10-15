package ru.artak.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.artak.service.TelegramService;

import java.io.Serializable;

public class PoolingBot extends TelegramLongPollingBot {

    private static final Logger logger = LogManager.getLogger(PoolingBot.class);

    private final String telegramToken;

    private final String telegramBotName;

    private final UpdateHandlerImpl updateHandlerImpl;

    private final TelegramService telegramService;

    public PoolingBot(String telegramToken, String telegramBotName, UpdateHandlerImpl updateHandlerImpl, TelegramService telegramService) {
        this.telegramToken = telegramToken;
        this.telegramBotName = telegramBotName;
        this.updateHandlerImpl = updateHandlerImpl;
        this.telegramService = telegramService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateHandlerImpl.setUpdateHandler(telegramService);
        BotApiMethod<Serializable> response =  updateHandlerImpl.executeUpdate(update);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            try {
                logger.debug("exception in TgHandler ", e);
                execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("error , please try again"));
            } catch (TelegramApiException telegramApiException) {
                try {
                    execute(new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId()).setText("error , please try again"));
                } catch (TelegramApiException apiException) {
                    logger.error("Unexpected situation", apiException);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotName;
    }

    @Override
    public String getBotToken() {
        return telegramToken;
    }
}
