// Modified: UserPlaylistsController.java
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
    public Map<String, Object> getUserPlaylistsFromProfile(
            @RequestParam String profileUrl,
            HttpSession session
    ) {
        String userId = extractUserId(profileUrl);

        // FIX: Usamos app token para acceso público (no requiere login de usuario)
        String appToken = appAuthService.getAppAccessToken();
        // FIX: Eliminamos check de userToken y throw UNAUTHORIZED

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(appToken);

        // NEW: Fetch del perfil del usuario para obtener display_name
        String userProfileUrl = "https://api.spotify.com/v1/users/" + userId;
        HttpEntity<Void> profileEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> profileResponse = trackService.getRestTemplate()
                .exchange(userProfileUrl, HttpMethod.GET, profileEntity, Map.class);

        Map<String, Object> profileBody = profileResponse.getBody();
        String displayName = profileBody != null ? (String) profileBody.get("display_name") : userId;

        String playlistsUrl = "https://api.spotify.com/v1/users/" + userId + "/playlists?limit=50";
        List<PlaylistResponseDto> playlists = fetchPlaylists(playlistsUrl, headers);

        Map<String, Object> result = new HashMap<>();
        result.put("displayName", displayName);
        result.put("playlists", playlists);
        return result;
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

    // 🔹 EXTRAE ID DESDE URL DE PERFIL
    private String extractUserId(String profileUrl) {
        // Ej: https://open.spotify.com/user/fuyzzlove?si=xxxx
        String clean = profileUrl.split("\\?")[0];
        String[] parts = clean.split("/");
        return parts[parts.length - 1];
    }
}