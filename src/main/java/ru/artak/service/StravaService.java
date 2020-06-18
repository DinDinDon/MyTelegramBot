package ru.artak.service;

import ru.artak.client.strava.StravaClient;
import ru.artak.client.strava.StravaCredential;
import ru.artak.client.strava.model.ResultActivities;
import ru.artak.client.strava.model.StravaOauthResp;
import ru.artak.storage.Storage;
import ru.artak.client.telegram.TelegramClient;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.WEEK_OF_MONTH;


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

    public double getWeekDistance(Integer chatId) throws IOException, InterruptedException {
        double weekDistance = 0.0;
        String accessToken = storage.getStravaCredentials(chatId).getAccessToken();

          ResultActivities[] resultActivities =  stravaClient.getActivities(accessToken);
        for (int i = 0; i < resultActivities.length; i++) {
           Date date =  resultActivities[i].getStartDate();
           if(checkWeekDay(date)==true){
               weekDistance +=resultActivities[i].getDistance();
           }
        }
        return weekDistance;
    }

    public boolean checkWeekDay(Date date){
        Date today = new Date();
        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.setTime(today);

        int dayToday = calendarForToday.get(DAY_OF_WEEK) ;
        int weekCurrent = calendarForToday.get(WEEK_OF_MONTH);

        Calendar calendarForStrava = Calendar.getInstance();
        calendarForStrava.setTime(date);

        int dayOnWeekStrava = calendarForStrava.get(DAY_OF_WEEK);
        int weekStrava = calendarForStrava.get(WEEK_OF_MONTH);

        if(weekCurrent==weekStrava) {
            if (dayToday == 1)

                return true;
            if (dayOnWeekStrava <= dayToday && dayOnWeekStrava != 1)

                return true;
        }

        return false;
    }

}
