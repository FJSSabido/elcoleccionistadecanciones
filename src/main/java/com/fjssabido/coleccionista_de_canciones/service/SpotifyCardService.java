package com.fjssabido.coleccionista_de_canciones.service;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpotifyCardService {

    private final SpotifyTrackService trackService;

    public SpotifyCardService(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    public List<TrackCardDto> generateCardsFromUrl(String url) {
        if (url.contains("/playlist/")) {
            String id = extractId(url);
            return trackService.getTracksFromPlaylist(id);
        }

        throw new IllegalArgumentException("URL no soportada");
    }

    private String extractId(String url) {
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.contains("?") ? last.substring(0, last.indexOf("?")) : last;
    }
}
