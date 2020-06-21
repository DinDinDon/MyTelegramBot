package ru.artak.service;

import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.ResultActivities;
import ru.artak.client.strava.StravaOauthResp;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;

import java.io.IOException;
import java.time.*;
import java.util.*;


public class StravaService {

    private static final String AUTHORIZED_TEXT = "Strava аккаунт был успешно подключен!";
    private final String type = "Run";
    private final TelegramClient telegramClient;
    private final StravaClient stravaClient;
    private final Storage storage;

    public StravaService(TelegramClient telegramClient, Storage storage, StravaClient stravaClient) {
        this.telegramClient = telegramClient;
        this.storage = storage;
        this.stravaClient = stravaClient;
    }

    public void obtainCredentials(String state, String authorizationCode) throws IOException, InterruptedException {
        StravaCredential stravaCredential = getCredentials(authorizationCode);

        Integer chatID = storage.getChatIdByState(state);
        storage.saveStravaCredentials(chatID, stravaCredential);
        telegramClient.sendSimpleText(chatID, AUTHORIZED_TEXT);

    }

    private StravaCredential getCredentials(String authorizationCode) {
        try {
            StravaOauthResp strava = stravaClient.getStravaCredentials(authorizationCode);

            return new StravaCredential(strava.getAccessToken(), strava.getRefreshToken(), strava.getExpiresAt());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("no date from StravaAPI");
        }
    }

    public Number getRunningWeekDistance(Integer chatId) {
        String accessToken = storage.getStravaCredentials(chatId).getAccessToken();
        WeekInterval weekInterval = getWeekInterval();
        Long after = weekInterval.getAfter();
        Long before = weekInterval.getBefor();
        List<ResultActivities> resultActivities;
        try {
            resultActivities = stravaClient.getActivities(accessToken, after, before);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("no Activities Data");
        }
        float resultRunningDistance = getRunningDistanceFormat(resultActivities);

        return UtilsFormatKm.getFormatKm(resultRunningDistance);
    }

    private float getRunningDistanceFormat(List<ResultActivities> resultActivities) {
        float resultRunningDistance = 0.0f;
        for (ResultActivities resultActivity : resultActivities) {
            if (resultActivity.getType().equals(type)) {
                resultRunningDistance += resultActivity.getDistance();
            }
        }

        return resultRunningDistance;
    }


    private WeekInterval getWeekInterval() {
        LocalDateTime afterToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(00, 00, 00))
                .with(DayOfWeek.MONDAY);
        LocalDateTime beforeToday = LocalDateTime.now().with(DayOfWeek.SUNDAY).plusDays(1);

        Instant instantMonday = beforeToday.toInstant(ZoneOffset.MIN);
        Long before = instantMonday.getEpochSecond();

        Instant instantSunday = afterToday.toInstant(ZoneOffset.MAX);
        Long after = instantSunday.getEpochSecond();

        return new WeekInterval(after, before);
    }

}
