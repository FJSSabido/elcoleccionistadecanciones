package com.fjssabido.coleccionista_de_canciones.service;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Service
public class SpotifyCardService {

    private final SpotifyTrackService trackService;

    public SpotifyCardService(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    public List<TrackCardDto> generateCardsFromUrl(String url, HttpHeaders headers) {
        String id = extractId(url);
        if (url.contains("/playlist/")) {
            return trackService.getTracksFromPlaylist(id, headers);
        } else if (url.contains("/track/")) {
            // FIX: Soporte para single track
            TrackCardDto card = trackService.getTrack(id, headers);
            return List.of(card);
        }
        throw new IllegalArgumentException("URL no soportada");
    }

    private String extractId(String url) {
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.contains("?") ? last.substring(0, last.indexOf("?")) : last;
    }
}