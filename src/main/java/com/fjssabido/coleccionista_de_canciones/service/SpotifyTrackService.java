// Modified: SpotifyTrackService.java
package com.fjssabido.coleccionista_de_canciones.service;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SpotifyTrackService {
    private final RestTemplate restTemplate = new RestTemplate();

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public List<TrackCardDto> getTracksFromPlaylist(
            String playlistId,
            HttpHeaders headers,
            String market // Market is always provided now
    ) {

        List<TrackCardDto> cards = new ArrayList<>();
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=100&market=" + market;

        while (url != null) {
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) break;

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) body.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> track =
                            (Map<String, Object>) item.get("track");

                    if (track == null) continue;

                    cards.add(TrackCardDto.fromSpotifyTrack(track));
                }
            }

            url = (String) body.get("next");
        }

        return cards;
    }

    public TrackCardDto getTrack(String trackId, HttpHeaders headers, String market) {
        String url = "https://api.spotify.com/v1/tracks/" + trackId + "?market=" + market;
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("No se pudo obtener el track");
        }
        return TrackCardDto.fromSpotifyTrack(body);
    }
}