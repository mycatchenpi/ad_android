package com.example.spotify.dto;

public class SongDTO {
    private String trackId;
    private String songName;
    private String artistName;
    private String duration;

    public SongDTO(String trackId, String songName, String artistName, String duration) {
        this.trackId = trackId;
        this.songName = songName;
        this.artistName = artistName;
        this.duration = duration;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = "https://open.spotify.com/track/" + trackId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
