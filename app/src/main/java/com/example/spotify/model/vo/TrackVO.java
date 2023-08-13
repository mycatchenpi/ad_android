package com.example.spotify.model.vo;

public class TrackVO {

    private String url;
    private String name;
    private String artist;

    public TrackVO(String url,String name, String artist ) {
        this.url = url;
        this.artist = artist;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
