package ru.artak.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.ResultActivities;
import ru.artak.client.strava.StravaOauthResp;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.utils.UtilsFormatKm;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


public class StravaService {

    private static final Logger logger = LogManager.getLogger(StravaService.class);
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

    public void obtainCredentials(UUID state, String authorizationCode) throws IOException, InterruptedException, TelegramApiException {
        StravaCredential stravaCredential = getCredentials(authorizationCode);

        Long chatID = storage.getChatIdByState(state);
        storage.saveStravaCredentials(chatID, stravaCredential);
        logger.info("saved credentials for user - {}", chatID);
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

    public Number getRunningDistance(Long chatId, StravaCredential credential, String interval) throws IOException, InterruptedException {
        Long utcOffset = stravaClient.getUtcOffset(chatId, credential);
        DistanceInterval distanceInterval = getDistanceInterval(interval, utcOffset);
        Long from = distanceInterval.getFrom();
        Long to = distanceInterval.getTo();
        List<ResultActivities> responseActivities = stravaClient.getActivities(chatId, credential, from, to);
        float resultRunningDistance = getRunningDistanceFormat(responseActivities);

        return UtilsFormatKm.getFormatKm(resultRunningDistance);
    }
    @Deprecated
    private List<ResultActivities> getCorrectDateWithTimeZone(List<ResultActivities> resultActivities, String interval) {
        if (interval.equals("currentWeekDistance")) {
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

        return resultActivities;
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

    private DistanceInterval getDistanceInterval(String interval, Long utcOffset) {
        if (interval.equals("currentWeekDistance")) {
            Long from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY).toEpochSecond(ZoneOffset.UTC) - utcOffset - 10;
            Long to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY).toEpochSecond(ZoneOffset.UTC) - utcOffset;

            return new DistanceInterval(from, to);
        }
        if (interval.equals("lastWeekDistance")) {
            Long from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY).minusWeeks(1).toEpochSecond(ZoneOffset.UTC) - utcOffset - 10;
            Long to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY).minusWeeks(1).toEpochSecond(ZoneOffset.UTC) - utcOffset;

            return new DistanceInterval(from, to);
        }
        if (interval.equals("currentMonthDistance")) {
            Long from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth()).toEpochSecond(ZoneOffset.UTC) -
                    utcOffset - 10;
            Long to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(TemporalAdjusters.lastDayOfMonth()).toEpochSecond(ZoneOffset.UTC) - utcOffset;

            return new DistanceInterval(from, to);
        }
        if (interval.equals("lastMonthDistance")) {
            Long from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toEpochSecond(ZoneOffset.UTC) -
                    utcOffset - 10;
            Long to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).toEpochSecond(ZoneOffset.UTC) - utcOffset;

            return new DistanceInterval(from, to);
        }

        return null;

    }

    public void deauthorize(Long chatId, String accessToken) throws IOException, InterruptedException {
        stravaClient.deauthorizeUser(accessToken);
        storage.removeUser(chatId);
    }

}
