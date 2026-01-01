package com.fjssabido.coleccionista_de_canciones.dto;

import java.util.List;
import java.util.Map;

public class TrackCardDto {

    private String id;
    private String title;
    private String artist;
    private String album;
    private String imageUrl;
    private String spotifyUrl;
    // FIX: Nuevos campos
    private String year;
    private String duration;

    // =====================
    // FACTORY METHOD
    // =====================
    @SuppressWarnings("unchecked")
    public static TrackCardDto fromSpotifyTrack(Map<String, Object> track) {
        TrackCardDto dto = new TrackCardDto();

        dto.id = (String) track.get("id");
        dto.title = (String) track.get("name");

        // Artist (primer artista)
        List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
        if (artists != null && !artists.isEmpty()) {
            dto.artist = (String) artists.get(0).get("name");
        }

        // Album
        Map<String, Object> album = (Map<String, Object>) track.get("album");
        if (album != null) {
            dto.album = (String) album.get("name");

            List<Map<String, Object>> images = (List<Map<String, Object>>) album.get("images");
            if (images != null && !images.isEmpty()) {
                dto.imageUrl = (String) images.get(0).get("url");
            }

            // FIX: Extraer year de release_date (solo año)
            String releaseDate = (String) album.get("release_date");
            if (releaseDate != null) {
                dto.year = releaseDate.substring(0, 4);
            }
        }

        // FIX: Duration en formato MM:SS
        Integer durationMs = (Integer) track.get("duration_ms");
        if (durationMs != null) {
            int minutes = durationMs / 60000;
            int seconds = (durationMs % 60000) / 1000;
            dto.duration = String.format("%d:%02d", minutes, seconds);
        }

        // Spotify URL
        Map<String, Object> externalUrls = (Map<String, Object>) track.get("external_urls");
        if (externalUrls != null) {
            dto.spotifyUrl = (String) externalUrls.get("spotify");
        }

        return dto;
    }

    // =====================
    // GETTERS
    // =====================
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    // FIX: Getters nuevos
    public String getYear() {
        return year;
    }

    public String getDuration() {
        return duration;
    }

    public String getName() {
        return title;
    }

    public String getCoverUrl() {
        return imageUrl;
    }
}