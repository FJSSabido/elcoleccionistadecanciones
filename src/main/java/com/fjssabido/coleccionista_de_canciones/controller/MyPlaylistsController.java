package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.PlaylistResponseDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyTrackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api")
public class MyPlaylistsController {

    private final SpotifyTrackService trackService;

    public MyPlaylistsController(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/my/playlists")
    public List<PlaylistResponseDto> getMyPlaylists(HttpSession session) {
        String userToken = (String) session.getAttribute("SPOTIFY_USER_TOKEN");
        if (userToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes conectarte primero con Spotify");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        String url = "https://api.spotify.com/v1/me/playlists?limit=50";
        return fetchPlaylists(url, headers);
    }

    private List<PlaylistResponseDto> fetchPlaylists(String url, HttpHeaders headers) {
        List<PlaylistResponseDto> playlists = new ArrayList<>();

        while (url != null) {
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = trackService.getRestTemplate()
                    .exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) break;

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    String id = (String) item.get("id");
                    String name = (String) item.get("name");

                    Map<String, Object> tracks = (Map<String, Object>) item.get("tracks");
                    int totalTracks = tracks != null && tracks.get("total") != null ? (Integer) tracks.get("total") : 0;

                    playlists.add(new PlaylistResponseDto(id, name, totalTracks));
                }
            }

            url = (String) body.get("next");
        }

        return playlists;
    }
}