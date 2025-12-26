package com.fjssabido.coleccionista_de_canciones.service;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SpotifyTrackService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService authService;
    private final SpotifyAppAuthService appAuthService;  // ← NUEVA INYECCIÓN

    public SpotifyTrackService(SpotifyAuthService authService, SpotifyAppAuthService appAuthService) {
        this.authService = authService;
        this.appAuthService = appAuthService;  // ← Inyectamos
    }

    public HttpHeaders authHeaders() {
        String userToken = authService.getUserAccessToken();

        HttpHeaders headers = new HttpHeaders();

        if (userToken != null && !userToken.isBlank()) {
            // Si estamos autenticados como usuario → usamos su token (necesario para privadas)
            headers.setBearerAuth(userToken);
        } else {
            // Si no → usamos token de aplicación (suficiente para playlists públicas)
            headers.setBearerAuth(appAuthService.getAppAccessToken());
        }

        return headers;
    }


    public List<TrackCardDto> getTracksFromPlaylist(String playlistId) {
        List<TrackCardDto> allCards = new ArrayList<>();
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=100";

        try {
            while (url != null) {
                HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
                System.out.println("➡️ Llamando a Spotify playlist tracks: " + url);
                System.out.println("➡️ Token presente: " + (authService.getUserAccessToken() != null));

                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body == null) break;

                List<Map> items = (List<Map>) body.get("items");
                if (items != null) {
                    for (Map item : items) {
                        Map track = (Map) item.get("track");
                        if (track == null) continue;

                        TrackCardDto card = mapTrack(track);
                        if (card != null) {
                            allCards.add(card);
                        }
                    }
                }

                url = (String) body.get("next");
            }

            if (allCards.isEmpty()) {
                throw new RuntimeException("La playlist está vacía o no tiene canciones accesibles");
            }

            System.out.println("✅ Total canciones cargadas: " + allCards.size());
            return allCards;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("Error Spotify API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Playlist no encontrada o no accesible (puede ser privada o eliminada)");
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("No autorizado: conecta con Spotify para playlists privadas");
            }
            throw new RuntimeException("Error accediendo a la playlist: " + e.getMessage());
        }
    }

    private TrackCardDto mapTrack(Map<String, Object> track) {
        if (track == null) return null;

        String title = (String) track.get("name");

        // Artistas
        List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
        String artist = artists != null && !artists.isEmpty()
                ? (String) artists.get(0).get("name")
                : "Desconocido";

        // Álbum
        Map<String, Object> album = (Map<String, Object>) track.get("album");
        String albumName = album != null ? (String) album.get("name") : "Single";

        // Año de lanzamiento
        String releaseYear = "Desconocido";
        if (album != null) {
            String releaseDate = (String) album.get("release_date");
            if (releaseDate != null && !releaseDate.isEmpty()) {
                releaseYear = releaseDate.substring(0, 4);  // Solo el año (YYYY)
            }
        }

        // Portada: la más grande disponible
        String coverUrl = "https://via.placeholder.com/300?text=No+Cover";
        if (album != null) {
            List<Map<String, Object>> images = (List<Map<String, Object>>) album.get("images");
            if (images != null && !images.isEmpty()) {
                // La primera suele ser la más grande
                coverUrl = (String) images.get(0).get("url");
            }
        }

        // Duración: convertir milisegundos a MM:SS
        Integer durationMs = (Integer) track.get("duration_ms");
        String duration = "0:00";
        if (durationMs != null) {
            int minutes = durationMs / 60000;
            int seconds = (durationMs % 60000) / 1000;
            duration = minutes + ":" + String.format("%02d", seconds);
        }

        // URL de Spotify
        Map<String, String> externalUrls = (Map<String, String>) track.get("external_urls");
        String spotifyUrl = externalUrls != null ? externalUrls.get("spotify") : "";

        return new TrackCardDto(title, artist, albumName, coverUrl, duration, spotifyUrl, releaseYear);
    }
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
