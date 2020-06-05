package ru.artak.client.strava.model;

public class StravaCredential {
	
	private final String accessToken;
	private final String refreshToken;
	// TODO возможно понадобится хранить время протухания токена
	
	
	public StravaCredential(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
}
