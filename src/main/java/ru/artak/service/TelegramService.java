package ru.artak.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.telegram.model.GetUpdateTelegram;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TelegramService extends TelegramLongPollingBot {

    private static final Logger logger = LogManager.getLogger(TelegramService.class);

    private final StravaService stravaService;

    private final Storage storage;

    private final String telegramToken;

    private final String telegramBotName;

    private final int stravaClientId;

    private final String baseRedirectUrl;

    private final String telegramBotDefaultText =
            "Телеграм бот для работы с ресурсом Strava.com\n" +
                    "\n" +
                    "Список доступных команд:\n" +
                    "\n" +
                    "/auth - авторизация через OAuth\n" +
                    "/weekdistance - набеганное расстояние за календарную неделю\n" +
                    "/deauthorize - деавторизация\n ";

    private final String whenUserAlreadyAuthorized = "Вы уже авторизованы!";
    private final String telegramNoAuthorizationText = "Вы не авторизованы. Используйте команду /auth";
    private final String telegramDeauthorizeText = "Вы деавторизированы!";

    private final String telegramWeekDistanceText = "Вы пробежали ";
    private final String errorText = "Ошибка, пожалуйста повторите позднее ";


    public TelegramService(Storage storage, StravaService stravaService,
                           String telegramToken, String telegramBotName, int stravaClientId, String baseRedirectUrl) {
        this.storage = storage;
        this.stravaService = stravaService;
        this.telegramToken = telegramToken;
        this.telegramBotName = telegramBotName;
        this.stravaClientId = stravaClientId;
        this.baseRedirectUrl = baseRedirectUrl;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                logger.debug("received a new request in telegram from user - {}", update.getMessage().getChatId());
                switch (update.getMessage().getText()) {
                    case "/auth":
                        handleAuthCommand(update.getMessage().getChatId());
                        break;
                    case "/weekdistance":
                        execute(sendInlineKeyBoardMessageWeek(update.getMessage().getChatId()));
                        break;
                    case "/deauthorize":
                        handleDeauthorizeCommand(update.getMessage().getChatId());
                        break;
                    case "/monthdistance":
                        execute(sendInlineKeyBoardMessageMonth(update.getMessage().getChatId()));
                        break;
                    default:
                        handleDefaultCommand(update.getMessage().getChatId(), telegramBotDefaultText);
                        break;
                }
            } else if (update.hasCallbackQuery()) {
                switch (update.getCallbackQuery().getData()) {
                    case "currentWeekDistance":
                        handleDistance(update.getCallbackQuery().getMessage().getChatId(), "currentWeekDistance");
                        break;
                    case "lastWeekDistance":
                        handleDistance(update.getCallbackQuery().getMessage().getChatId(), "lastWeekDistance");
                        break;
                    case  "currentMonthDistance":
                        handleDistance(update.getCallbackQuery().getMessage().getChatId(), "currentMonthDistance");
                        break;
                    case "lastMonthDistance":
                        handleDistance(update.getCallbackQuery().getMessage().getChatId(), "lastMonthDistance");
                        break;

                }
            }
        } catch (Throwable e) {
            logger.error("mistake telegram bot handler ", e);
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


    private void handleDistance(Long chatId, String interval) throws IOException, InterruptedException, TelegramApiException {
        logger.debug("received a request distance for user - {}, interval - {}", chatId, interval);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential == null || credential.isStatus()) {
            execute(new SendMessage().setChatId(chatId).setText(telegramNoAuthorizationText));
            return;
        }
        Number runningDistance = stravaService.getRunningDistance(chatId, credential, interval);
        execute(new SendMessage().setChatId(chatId).setText(telegramWeekDistanceText + runningDistance + "Км"));
        logger.info("sent method distance data to user - {} , interval - {}", chatId, interval);
    }


    private void handleDefaultCommand(Long chatId, String anyText) throws TelegramApiException {
        execute(new SendMessage().setChatId(chatId).setText(telegramBotDefaultText).setReplyMarkup(getButtons()));
    }

    private void handleAuthCommand(Long chatId) throws TelegramApiException {
        final UUID randomClientID = UUID.randomUUID();
        logger.debug("received a request /auth for user - {}, randomClientID - {}", chatId, randomClientID);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential != null && !credential.isStatus()) {
            execute(new SendMessage().setChatId(chatId).setText(whenUserAlreadyAuthorized));
            return;
        }
        storage.saveStateForUser(randomClientID, chatId);
        execute(new SendMessage().setChatId(chatId).setText(StravaClient.STRAVA_OAUTH_ADDRESS + "authorize?client_id=" +
                stravaClientId + "&state=" + randomClientID + "&response_type=code&redirect_uri=" + baseRedirectUrl +
                "/exchange_token&approval_prompt=force&&scope=activity:read"));
        logger.debug("sent to user - {}, randomClientID - {} Oauth link", chatId, randomClientID);
    }

    private void handleDeauthorizeCommand(Long chatId) throws TelegramApiException {
        logger.debug("received a request /deauthorize for user - {}", chatId);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential == null) {
            execute(new SendMessage().setChatId(chatId).setText(telegramNoAuthorizationText));
            return;
        }
        String accessToken = credential.getAccessToken();
        try {
            stravaService.deauthorize(chatId, accessToken);
            execute(new SendMessage().setChatId(chatId).setText(telegramDeauthorizeText));
        } catch (Exception e) {
            execute(new SendMessage().setChatId(chatId).setText(errorText));
            logger.warn("failed to deauthorize user - {}", chatId, e);
        }

    }

    @Deprecated
    private List<TelegramUserInfo> getAllTelegramUpdateUsers(GetUpdateTelegram getUpdateTelegram) {
        return getUpdateTelegram.getResult().stream()
                .map(result ->
                        new TelegramUserInfo(
                                result.getMessage().getChat().getId(),
                                result.getMessage().getText(),
                                result.getUpdateId()))
                .collect(Collectors.toList());
    }

    private ReplyKeyboardMarkup getButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("/auth"));

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("/weekdistance"));

        KeyboardRow keyboardThirdRow = new KeyboardRow();
        keyboardThirdRow.add("/deauthorize");

        KeyboardRow keyboardFourthRow = new KeyboardRow();
        keyboardFourthRow.add("/monthdistance");

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        keyboard.add(keyboardFourthRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private SendMessage sendInlineKeyBoardMessageWeek(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("currentWeekDistance");
        inlineKeyboardButton1.setCallbackData("currentWeekDistance");
        inlineKeyboardButton2.setText("lastWeekDistance");
        inlineKeyboardButton2.setCallbackData("lastWeekDistance");

        return getSendMessage(chatId, inlineKeyboardMarkup, inlineKeyboardButton1, inlineKeyboardButton2);
    }

    private SendMessage sendInlineKeyBoardMessageMonth(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("currentMonthDistance");
        inlineKeyboardButton1.setCallbackData("currentMonthDistance");
        inlineKeyboardButton2.setText("lastMonthDistance");
        inlineKeyboardButton2.setCallbackData("lastMonthDistance");

        return getSendMessage(chatId, inlineKeyboardMarkup, inlineKeyboardButton1, inlineKeyboardButton2);
    }

    private SendMessage getSendMessage(long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, InlineKeyboardButton inlineKeyboardButton1, InlineKeyboardButton inlineKeyboardButton2) {
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return new SendMessage().setChatId(chatId).setText("Выберите интервал").setReplyMarkup(inlineKeyboardMarkup);
    }


}