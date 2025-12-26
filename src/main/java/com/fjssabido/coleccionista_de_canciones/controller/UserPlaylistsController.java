package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.service.SpotifyAppAuthService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyTrackService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserPlaylistsController {

    private final SpotifyTrackService trackService;
    private final SpotifyAppAuthService appAuthService;

    public UserPlaylistsController(SpotifyTrackService trackService, SpotifyAppAuthService appAuthService) {
        this.trackService = trackService;
        this.appAuthService = appAuthService;
    }

    record PlaylistItem(String id, String name, int trackCount) {}

    @GetMapping("/api/me/playlists")
    public List<PlaylistItem> getMyPlaylists() {
        List<PlaylistItem> playlists = new ArrayList<>();
        String url = "https://api.spotify.com/v1/me/playlists?limit=50";

        try {
            while (url != null) {
                var entity = new HttpEntity<>(trackService.authHeaders());
                var response = trackService.getRestTemplate().exchange(
                        url, HttpMethod.GET, entity, Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body == null) break;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String id = (String) item.get("id");
                        String name = (String) item.get("name");
                        Map<String, Object> tracks = (Map<String, Object>) item.get("tracks");
                        int total = tracks != null ? (Integer) tracks.get("total") : 0;

                        playlists.add(new PlaylistItem(id, name, total));
                    }
                }

                url = (String) body.get("next");
            }

            return playlists;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado con Spotify");
            }
            throw new RuntimeException("Error cargando playlists: " + e.getMessage());
        }
    }

    @GetMapping("/api/users/{username}/playlists")
    public List<PlaylistItem> getUserPlaylists(@PathVariable String username) {
        List<PlaylistItem> playlists = new ArrayList<>();
        String url = "https://api.spotify.com/v1/users/" + username + "/playlists?limit=50";

        try {
            while (url != null) {
                var entity = new HttpEntity<>(appAuthService.appAuthHeaders());  // Usa token de app para públicas
                var response = trackService.getRestTemplate().exchange(
                        url, HttpMethod.GET, entity, Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body == null) break;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        // Filtramos solo las públicas (las privadas no aparecen aquí)
                        Boolean isPublic = (Boolean) item.get("public");
                        if (isPublic == null || !isPublic) continue;  // Opcional: saltar no públicas

                        String id = (String) item.get("id");
                        String name = (String) item.get("name");
                        Map<String, Object> tracks = (Map<String, Object>) item.get("tracks");
                        int total = tracks != null ? (Integer) tracks.get("total") : 0;

                        playlists.add(new PlaylistItem(id, name, total));
                    }
                }

                url = (String) body.get("next");
            }

            if (playlists.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado o sin playlists públicas");
            }

            return playlists;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado o username inválido");
            }
            throw new RuntimeException("Error cargando playlists públicas del usuario: " + e.getMessage());
        }
    }
}