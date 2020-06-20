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
        Number weekDistance;
        String accessToken = storage.getStravaCredentials(chatId).getAccessToken();
        Map<String, Long> afterAndBeforTime = getAfterAndBeforeTime();
        Long after = afterAndBeforTime.get("after");
        Long before = afterAndBeforTime.get("before");
        List<ResultActivities> resultActivities;
        try {
            resultActivities = stravaClient.getActivities(accessToken, after, before);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("no Activities Data");
        }
        float resultRunningDistance = getRunningDistanceFormat(resultActivities);
        weekDistance = getFormatKm(resultRunningDistance);

        return weekDistance;
    }

    private float getRunningDistanceFormat(List<ResultActivities> resultActivities ){
        float resultRunningDistance =  0.0f;
        for (ResultActivities resultActivity : resultActivities) {
            if (resultActivity.getType().equals("Run")) {
                resultRunningDistance += resultActivity.getDistance();
            }
        }

        return resultRunningDistance;
    }

    private Number getFormatKm(float distance) {
        float result = distance / 1000;
        if (result - Math.round(result) == 0)
            return Math.round(result);

        return Math.round(result * 10.0) / 10.0;
    }

    private Map<String, Long> getAfterAndBeforeTime() {
        Map<String, Long> afterAndBeforTime = new HashMap<>();
        LocalTime TimeMonday = LocalTime.of(00, 00, 00);
//        LocalTime TimeSunday = LocalTime.of(23, 59, 59);

        LocalDateTime afterToday = LocalDateTime.of(LocalDate.now(), TimeMonday).with(DayOfWeek.MONDAY);
//        LocalDateTime beforeToday = LocalDateTime.of(LocalDate.now(), TimeSunday).with(DayOfWeek.SUNDAY).plusDays(1);
        LocalDateTime beforeToday = LocalDateTime.now().with(DayOfWeek.SUNDAY).plusDays(1);


        Instant instantMonday = beforeToday.toInstant(ZoneOffset.MAX);
        Long before = instantMonday.getEpochSecond();

        Instant instantSunday = afterToday.toInstant(ZoneOffset.MAX);
        Long after = instantSunday.getEpochSecond();

        afterAndBeforTime.put("before", before);
        afterAndBeforTime.put("after", after);

        return afterAndBeforTime;
    }

}
