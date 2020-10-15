package ru.artak.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboardMarkupBuilder {
    private Long chatId;
    private String text;
    private boolean selective;
    private boolean resizeKeyboard;
    private boolean oneTimeKeyboard;


    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = null;

    private ReplyKeyboardMarkupBuilder() {
    }

    public static ReplyKeyboardMarkupBuilder create() {
        ReplyKeyboardMarkupBuilder builder = new ReplyKeyboardMarkupBuilder();
        return builder;
    }

    public static ReplyKeyboardMarkupBuilder create(Long chatId) {
        ReplyKeyboardMarkupBuilder builder = new ReplyKeyboardMarkupBuilder();
        builder.setChatId(chatId);
        return builder;
    }

    public ReplyKeyboardMarkupBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public ReplyKeyboardMarkupBuilder setKeyboardMarkupConfig(boolean selective, boolean resizeKeyboard, boolean oneTimeKeyboard) {
        this.selective = selective;
        this.resizeKeyboard = resizeKeyboard;
        this.oneTimeKeyboard = oneTimeKeyboard;
        return this;
    }

    public ReplyKeyboardMarkupBuilder setChatId(Long chatId) {
        this.chatId = chatId;
        return this;
    }

    public ReplyKeyboardMarkupBuilder row() {
        this.row = new KeyboardRow();
        return this;
    }

    public ReplyKeyboardMarkupBuilder button(String text) {
        row.add(new KeyboardButton().setText(text));
        return this;
    }

    public ReplyKeyboardMarkupBuilder endRow() {
        this.keyboard.add(this.row);
        this.row = null;
        return this;
    }


    public SendMessage build() {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(selective);
        keyboardMarkup.setResizeKeyboard(resizeKeyboard);
        keyboardMarkup.setOneTimeKeyboard(oneTimeKeyboard);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }


}


