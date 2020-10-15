package ru.artak.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import ru.artak.bot.UpdateHandler;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.util.UUID;

public class TelegramService implements UpdateHandler {

    private static final Logger logger = LogManager.getLogger(TelegramService.class);

    private final StravaService stravaService;

    private final Storage storage;

    private final int stravaClientId;

    private final String baseRedirectUrl;

    private final String telegramBotDefaultText =
            "Телеграм бот для работы с ресурсом Strava.com\n" +
                    "\n" +
                    "Список доступных команд:\n" +
                    "\n" +
                    "/auth - авторизация через OAuth\n" +
                    "/weekdistance - набеганное расстояние за календарную неделю\n" +
                    "/monthdistance - набеганное расстояние за календарный месяц\n" +
                    "/deauthorize - деавторизация\n ";

    private final String whenUserAlreadyAuthorized = "Вы уже авторизованы!";
    private final String telegramNoAuthorizationText = "Вы не авторизованы. Используйте команду /auth";
    private final String telegramDeauthorizationText = "Вы деавторизированы!";
    private final String telegramWeekDistanceText = "Вы пробежали ";
    private final String errorText = "Ошибка, пожалуйста повторите позднее ";


    public TelegramService(Storage storage, StravaService stravaService, int stravaClientId, String baseRedirectUrl) {
        this.storage = storage;
        this.stravaService = stravaService;
        this.stravaClientId = stravaClientId;
        this.baseRedirectUrl = baseRedirectUrl;
    }

    @Override
    public BotApiMethod executeUpdate(Update update) {

        if (update.hasMessage()) {
            logger.debug("received a new request in telegram from user - {}", update.getMessage().getChatId());
            switch (update.getMessage().getText()) {
                case "/auth":
                    return handleAuthCommand(update.getMessage().getChatId());
                case "/weekdistance":
                    return sendInlineKeyBoardMessageWeek(update.getMessage().getChatId());
                case "/deauthorize":
                    return handleDeauthorizationCommand(update.getMessage().getChatId());
                case "/monthdistance":
                    return sendInlineKeyBoardMessageMonth(update.getMessage().getChatId());
                default:
                    return handleDefaultCommand(update.getMessage().getChatId());
            }
        } else if (update.hasCallbackQuery()) {
            try {
                switch (update.getCallbackQuery().getData()) {
                    case "currentWeekDistance":
                        return handleDistance(update.getCallbackQuery().getMessage().getChatId(), FindIntervalType.CURRENTWEEKDISTANCE);
                    case "lastWeekDistance":
                        return handleDistance(update.getCallbackQuery().getMessage().getChatId(), FindIntervalType.LASTWEEKDISTANCE);
                    case "currentMonthDistance":
                        return handleDistance(update.getCallbackQuery().getMessage().getChatId(), FindIntervalType.CURRENTMONTHDISTANCE);
                    case "lastMonthDistance":
                        return handleDistance(update.getCallbackQuery().getMessage().getChatId(), FindIntervalType.LASTMONTHDISTANCE);

                }
            } catch (Exception e) {
                logger.debug("mistake telegram bot handler ", e);

                return (new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId()).setText("error , please try again"));
            }
        }
        return null;
    }

    private SendMessage handleDistance(Long chatId, FindIntervalType interval) throws IOException, InterruptedException {
        logger.debug("received a request distance for user - {}, interval - {}", chatId, interval);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential == null || credential.isStatus()) {
            return (new SendMessage().setChatId(chatId).setText(telegramNoAuthorizationText));
        }
        Number runningDistance = stravaService.getRunningDistance(chatId, credential, interval);
        logger.info("sent method distance data to user - {} , interval - {}", chatId, interval);
        return (new SendMessage().setChatId(chatId).setText(telegramWeekDistanceText + runningDistance + "Км"));
    }


    private SendMessage handleDefaultCommand(Long chatId) {
        return getButtons(chatId);
    }

    private SendMessage handleAuthCommand(Long chatId) {
        final UUID randomClientID = UUID.randomUUID();
        logger.debug("received a request /auth for user - {}, randomClientID - {}", chatId, randomClientID);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential != null && !credential.isStatus()) {
            return (new SendMessage().setChatId(chatId).setText(whenUserAlreadyAuthorized));
        }
        storage.saveStateForUser(randomClientID, chatId);
        logger.debug("sent to user - {}, randomClientID - {} Oauth link", chatId, randomClientID);
        return (new SendMessage().setChatId(chatId).setText(StravaClient.STRAVA_OAUTH_ADDRESS + "authorize?client_id=" +
                stravaClientId + "&state=" + randomClientID + "&response_type=code&redirect_uri=" + baseRedirectUrl +
                "/exchange_token&approval_prompt=force&&scope=activity:read"));
    }

    private SendMessage handleDeauthorizationCommand(Long chatId) {
        logger.debug("received a request /deauthorize for user - {}", chatId);
        StravaCredential credential = storage.getStravaCredentials(chatId);

        if (credential == null) {
            return (new SendMessage().setChatId(chatId).setText(telegramNoAuthorizationText));
        }
        String accessToken = credential.getAccessToken();
        try {
            stravaService.deauthorize(chatId, accessToken);
            return (new SendMessage().setChatId(chatId).setText(telegramDeauthorizationText));
        } catch (Exception e) {
            logger.warn("failed to deauthorize user - {}", chatId, e);
            return (new SendMessage().setChatId(chatId).setText(errorText));
        }
    }

    private SendMessage getButtons(Long chatId) {

        return ReplyKeyboardMarkupBuilder.create(chatId)
                .setKeyboardMarkupConfig(true, true, false)
                .setText(telegramBotDefaultText)
                .row()
                .button("/auth")
                .endRow()
                .row()
                .button("/weekdistance")
                .endRow()
                .row()
                .button("/monthdistance")
                .endRow()
                .row()
                .button("/deauthorize")
                .endRow()
                .build();
    }

    private SendMessage sendInlineKeyBoardMessageWeek(long chatId) {

        return InlineKeyboardBuilder.create(chatId)
                .setText("Выберите область:")
                .row()
                .button("currentWeekDistance", "currentWeekDistance")
                .endRow()
                .row()
                .button("lastWeekDistance", "lastWeekDistance")
                .endRow()
                .build();
    }

    private SendMessage sendInlineKeyBoardMessageMonth(long chatId) {

        return InlineKeyboardBuilder.create(chatId)
                .setText("Выберите область:")
                .row()
                .button("currentMonthDistance", "currentMonthDistance")
                .endRow()
                .row()
                .button("lastMonthDistance", "lastMonthDistance")
                .endRow()
                .build();
    }
}