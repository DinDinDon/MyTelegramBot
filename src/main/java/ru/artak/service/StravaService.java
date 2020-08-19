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

    public void obtainCredentials(UUID state, String authorizationCode) throws IOException, InterruptedException {
        StravaCredential stravaCredential = getCredentials(authorizationCode);

        Long chatID = storage.getChatIdByState(state);
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

    public Number getRunningWeekDistance(Long chatId) throws IOException, InterruptedException {
        String accessToken = storage.getStravaCredentials(chatId).getAccessToken();
        WeekInterval weekInterval = getWeekInterval();
        Long from = weekInterval.getFrom();
        Long to = weekInterval.getTo();
        List<ResultActivities> responseActivities = stravaClient.getActivities(chatId, accessToken, from, to);
        List<ResultActivities> correctActivities = getCorrectDateWithTimeZone(responseActivities);
        float resultRunningDistance = getRunningDistanceFormat(correctActivities);

        return UtilsFormatKm.getFormatKm(resultRunningDistance);
    }

    private List<ResultActivities> getCorrectDateWithTimeZone(List<ResultActivities> resultActivities) {
        LocalDateTime monday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY);
        LocalDateTime sunday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY);
        List<ResultActivities> correctData = new ArrayList<>();
        for (ResultActivities iter : resultActivities) {
            if (iter.getStartDate().isBefore(monday) || iter.getStartDate().isAfter(sunday)) {
                continue;
            }
            correctData.add(iter);
        }

        return correctData;
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
        LocalDateTime from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY);
        LocalDateTime to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY);

        return new WeekInterval(
                from.toInstant(ZoneOffset.UTC).getEpochSecond(),
                to.toInstant(ZoneOffset.UTC).getEpochSecond()
        );
    }

    public void deauthorize(Long chatId, String accessToken) throws IOException, InterruptedException {
        stravaClient.deauthorizeUser(accessToken);
        storage.removeUser(chatId);
    }

}
