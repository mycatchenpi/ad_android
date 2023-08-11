package com.example.spotify.entity;

public class JsonResponseEntity {
    private String accessToken;
    private String userName;
    private Object recentlyPlayedTracks;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getRecentlyPlayedTracks() {
        return recentlyPlayedTracks;
    }

    public void setRecentlyPlayedTracks(Object recentlyPlayedTracks) {
        this.recentlyPlayedTracks = recentlyPlayedTracks;
    }

}
