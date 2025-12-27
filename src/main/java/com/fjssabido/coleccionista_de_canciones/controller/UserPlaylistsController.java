package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.PlaylistResponseDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyAppAuthService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyTrackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api")
public class UserPlaylistsController {

    private final SpotifyTrackService trackService;
    private final SpotifyAppAuthService appAuthService;

    public UserPlaylistsController(SpotifyTrackService trackService,
                                   SpotifyAppAuthService appAuthService) {
        this.trackService = trackService;
        this.appAuthService = appAuthService;
    }

    // 🔹 PLAYLISTS DE UN AMIGO (PÚBLICAS)
    @GetMapping("/users/playlists")
    public List<PlaylistResponseDto> getUserPlaylistsFromProfile(
            @RequestParam String profileUrl,
            HttpSession session
    ) {
        String userId = extractUserId(profileUrl);

        // FIX: Usamos app token para acceso público (no requiere login de usuario)
        String appToken = appAuthService.getAppAccessToken();
        // FIX: Eliminamos check de userToken y throw UNAUTHORIZED

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(appToken);

        String url = "https://api.spotify.com/v1/users/" + userId + "/playlists?limit=50";
        return fetchPlaylists(url, headers);
    }

    // 🔹 FETCH PAGINADO (YA ESTABA BIEN)
    private List<PlaylistResponseDto> fetchPlaylists(String url, HttpHeaders headers) {
        List<PlaylistResponseDto> playlists = new ArrayList<>();

        while (url != null) {
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = trackService.getRestTemplate()
                    .exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) break;

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) body.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    String id = (String) item.get("id");
                    String name = (String) item.get("name");

                    Map<String, Object> tracks =
                            (Map<String, Object>) item.get("tracks");

                    int totalTracks = tracks != null && tracks.get("total") != null
                            ? (Integer) tracks.get("total")
                            : 0;

                    playlists.add(new PlaylistResponseDto(id, name, totalTracks));
                }
            }

            url = (String) body.get("next");
        }

        return playlists;
    }

    // 🔹 EXTRAE ID DESDE URL DE PERFIL
    private String extractUserId(String profileUrl) {
        // Ej: https://open.spotify.com/user/fuyzzlove?si=xxxx
        String clean = profileUrl.split("\\?")[0];
        String[] parts = clean.split("/");
        return parts[parts.length - 1];
    }
}