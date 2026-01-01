// Modified: SpotifyCardService.java
package com.fjssabido.coleccionista_de_canciones.service;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyCardService {

    private final SpotifyTrackService trackService;
    private final RestTemplate restTemplate = new RestTemplate();

    public SpotifyCardService(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    public Map<String, Object> generateCardsFromUrl(String url, HttpHeaders headers) {
        String id = extractId(url);
        String market = extractMarket(url); // Will default to "ES" if not found
        Map<String, Object> result = new HashMap<>();

        if (url.contains("/playlist/")) {
            // Fetch playlist info for name, with market
            String infoUrl = "https://api.spotify.com/v1/playlists/" + id + "?market=" + market;
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            Map<String, Object> info = restTemplate.exchange(infoUrl, HttpMethod.GET, entity, Map.class).getBody();
            String name = info != null ? (String) info.get("name") : "Playlist";

            List<TrackCardDto> cards = trackService.getTracksFromPlaylist(id, headers, market); // Pass market
            result.put("type", "playlist");
            result.put("name", name);
            result.put("cards", cards);
        } else if (url.contains("/track/")) {
            TrackCardDto card = trackService.getTrack(id, headers, market); // Pass market
            result.put("type", "track");
            result.put("name", card.getTitle() + " por " + card.getArtist());
            result.put("cards", List.of(card));
        } else {
            throw new IllegalArgumentException("URL no soportada");
        }

        return result;
    }

    private String extractId(String url) {
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.contains("?") ? last.substring(0, last.indexOf("?")) : last;
    }

    // Modified: Default to "ES" if no locale found (for market-restricted content)
    private String extractMarket(String url) {
        int intlIndex = url.toLowerCase().indexOf("/intl-");
        if (intlIndex != -1) {
            int start = intlIndex + 6; // after /intl-
            int end = url.indexOf("/", start);
            if (end != -1) {
                String locale = url.substring(start, end).toUpperCase();
                if (locale.length() == 2) {
                    return locale;
                }
            }
        }
        return "ES"; // Default to ES for Spanish content; change to "US" if targeting another market
    }
}