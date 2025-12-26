package com.fjssabido.coleccionista_de_canciones.dto;

public class TrackCardDto {

    private String title;
    private String artist;
    private String album;
    private String coverUrl;
    private String duration;
    private String spotifyUrl;
    private String releaseYear;

    public TrackCardDto(String title, String artist, String album,
                        String coverUrl, String duration, String spotifyUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.spotifyUrl = spotifyUrl;
    }

    public TrackCardDto(String title, String artist, String album,
                        String coverUrl, String duration, String spotifyUrl, String releaseYear) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.spotifyUrl = spotifyUrl;
        this.releaseYear = releaseYear;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getCoverUrl() { return coverUrl; }
    public String getDuration() { return duration; }
    public String getSpotifyUrl() { return spotifyUrl; }
}
