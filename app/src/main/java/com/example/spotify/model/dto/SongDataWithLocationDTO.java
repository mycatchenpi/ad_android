package com.example.spotify.model.dto;

public class SongDataWithLocationDTO {
    private String songURI;
    private LocationData location;

    private String username;

    public String getSongURI() {
        return songURI;
    }

    public SongDataWithLocationDTO(String songURI, LocationData location, String username) {
        this.songURI = songURI;
        this.location = location;
        this.username = username;
    }

    public void setSongURI(String songURI) {
        this.songURI = songURI;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class LocationData {
        private double latitude;
        private double longitude;

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
