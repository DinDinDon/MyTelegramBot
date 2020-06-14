package ru.artak.client.strava;

import java.util.Date;

public class StravaCredential {

    private final String accessToken;
    private final String refreshToken;
    private final Date date;
    // 5h 45min
    private final Long timeToUpdate;

    public StravaCredential(String accessToken, String refreshToken, Date date) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.date = date;
        this.timeToUpdate = date.getTime() - 17100000L;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getTimeToUpdate() {
        return timeToUpdate;
    }
}
