package ru.artak.client.strava;

public class StravaCredential {

    private final String accessToken;
    private final String refreshToken;
    private final Long timeToUpdate;

    public StravaCredential(String accessToken, String refreshToken, Long timeToUpdate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.timeToUpdate = timeToUpdate;
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
