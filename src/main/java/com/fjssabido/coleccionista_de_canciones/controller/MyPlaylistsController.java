// Modified: MyPlaylistsController.java
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
    public Map<String, Object> getMyPlaylists(HttpSession session) {
        String userToken = (String) session.getAttribute("SPOTIFY_USER_TOKEN");
        if (userToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes conectarte primero con Spotify");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        // NEW: Fetch de /me para obtener display_name
        String meUrl = "https://api.spotify.com/v1/me";
        HttpEntity<Void> meEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> meResponse = trackService.getRestTemplate()
                .exchange(meUrl, HttpMethod.GET, meEntity, Map.class);

        Map<String, Object> meBody = meResponse.getBody();
        String displayName = meBody != null ? (String) meBody.get("display_name") : "Tú";

        String playlistsUrl = "https://api.spotify.com/v1/me/playlists?limit=50";
        List<PlaylistResponseDto> playlists = fetchPlaylists(playlistsUrl, headers);

        Map<String, Object> result = new HashMap<>();
        result.put("displayName", displayName);
        result.put("playlists", playlists);
        return result;
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

                    // NEW: Extraer owner
                    Map<String, Object> ownerMap = (Map<String, Object>) item.get("owner");
                    String owner = ownerMap != null ? (String) ownerMap.get("display_name") : "Unknown";

                    // NEW: Extraer imageUrl
                    List<Map<String, Object>> images = (List<Map<String, Object>>) item.get("images");
                    String imageUrl = images != null && !images.isEmpty() ? (String) images.get(0).get("url") : "";

                    playlists.add(new PlaylistResponseDto(id, name, totalTracks, owner, imageUrl));
                }
            }

            url = (String) body.get("next");
        }

        return playlists;
    }
}