package ru.artak.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.ResultActivities;
import ru.artak.client.strava.StravaOauthResp;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.utils.DistanceRange;
import ru.artak.utils.UtilsFormatKm;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


public class StravaService {

    private static final Logger logger = LogManager.getLogger(StravaService.class);
    private final String AUTHORIZED_TEXT = "Strava аккаунт был успешно подключен!";
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

        Long chatId = storage.getChatIdByState(state);
        storage.saveStravaCredentials(chatId, stravaCredential);
        logger.info("saved credentials for user - {}", chatId);
        telegramClient.sendSimpleText(chatId, AUTHORIZED_TEXT);
    }


    private StravaCredential getCredentials(String authorizationCode) {
        try {
            StravaOauthResp strava = stravaClient.getStravaCredentials(authorizationCode);

            return new StravaCredential(strava.getAccessToken(), strava.getRefreshToken(), strava.getExpiresAt());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("no date from StravaAPI");
        }
    }

    public Number getRunningDistance(Long chatId, StravaCredential credential, FindInterval interval) throws IOException, InterruptedException {
        DistanceInterval distanceInterval = getDistanceInterval(interval);
        Long from = distanceInterval.getFrom();
        Long to = distanceInterval.getTo();

        List<ResultActivities> responseActivities = stravaClient.getActivities(chatId, credential, from, to);
        List<ResultActivities> correctActivities = getCorrectDateWithTimeZone(responseActivities, interval);

        float resultRunningDistance = getRunningDistanceFormat(correctActivities);

        return UtilsFormatKm.getFormatKm(resultRunningDistance);
    }

    private List<ResultActivities> getCorrectDateWithTimeZone(List<ResultActivities> resultActivities, FindInterval interval) {
        LocalDateTime monday;
        LocalDateTime sunday;
        LocalDateTime firstDay;
        LocalDateTime lastDay;
        List<ResultActivities> correctData;
        switch (interval) {
            case CURRENTWEEKDISTANCE:
                monday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY);
                sunday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY);
                correctData = new ArrayList<>();
                for (ResultActivities iter : resultActivities) {
                    if (iter.getStartDate().isBefore(monday) || iter.getStartDate().isAfter(sunday)) {
                        continue;
                    }
                    correctData.add(iter);
                }
                return correctData;
            case LASTWEEKDISTANCE:
                monday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY).minusWeeks(1);
                sunday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(DayOfWeek.SUNDAY).minusWeeks(1);
                correctData = new ArrayList<>();
                for (ResultActivities iter : resultActivities) {
                    if (iter.getStartDate().isBefore(monday) || iter.getStartDate().isAfter(sunday)) {
                        continue;
                    }
                    correctData.add(iter);
                }
                return correctData;
            case CURRENTMONTHDISTANCE:
                firstDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth());
                lastDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(TemporalAdjusters.lastDayOfMonth());
                correctData = new ArrayList<>();
                for (ResultActivities iter : resultActivities) {
                    if (iter.getStartDate().isBefore(firstDay) || iter.getStartDate().isAfter(lastDay)) {
                        continue;
                    }
                    correctData.add(iter);
                }
                return correctData;
            case LASTMONTHDISTANCE:
                firstDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
                lastDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(TemporalAdjusters.lastDayOfMonth()).minusMonths(1);
                correctData = new ArrayList<>();
                for (ResultActivities iter : resultActivities) {
                    if (iter.getStartDate().isBefore(firstDay) || iter.getStartDate().isAfter(lastDay)) {
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

    private DistanceInterval getDistanceInterval(FindInterval interval) {

        switch (interval) {
            case CURRENTWEEKDISTANCE:

                return DistanceRange.getWeekRange(FindInterval.CURRENTWEEKDISTANCE);
            case LASTWEEKDISTANCE:

                return DistanceRange.getWeekRange(FindInterval.LASTWEEKDISTANCE);
            case CURRENTMONTHDISTANCE:

                return DistanceRange.getMonthRange(FindInterval.CURRENTMONTHDISTANCE);
            case LASTMONTHDISTANCE:

                return DistanceRange.getMonthRange(FindInterval.LASTMONTHDISTANCE);
        }
        return null;
    }

    public void deauthorize(Long chatId, String accessToken) throws IOException, InterruptedException {
        stravaClient.deauthorizeUser(accessToken);
        storage.removeUser(chatId);
    }

}
