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
            HttpHeaders headers
    ) {

        List<TrackCardDto> cards = new ArrayList<>();
        String url = "https://api.spotify.com/v1/playlists/" +
                playlistId + "/tracks?limit=100";

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

    // FIX: Nuevo método para fetch de un solo track
    public TrackCardDto getTrack(String trackId, HttpHeaders headers) {
        String url = "https://api.spotify.com/v1/tracks/" + trackId;
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("No se pudo obtener el track");
        }
        return TrackCardDto.fromSpotifyTrack(body);
    }
}
