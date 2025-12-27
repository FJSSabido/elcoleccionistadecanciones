package com.fjssabido.coleccionista_de_canciones.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SpotifyAppAuthService {

    @Value("${spotify.client-id:}")
    private String clientId;

    @Value("${spotify.client-secret:}")
    private String clientSecret;

    @Value("${spotify.token-url:https://accounts.spotify.com/api/token}")
    private String tokenUrl;

    private final RestTemplate restTemplate;

    // 🔐 Token y expiración
    private String appAccessToken;
    private Instant tokenExpiresAt;

    public SpotifyAppAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Token de aplicación para acceder a datos públicos de Spotify
     */
    public synchronized String getAppAccessToken() {
        if (appAccessToken == null || tokenExpired()) {
            authenticateApp();
        }
        return appAccessToken;
    }

    /**
     * Client Credentials Flow (OAuth)
     */
    private void authenticateApp() {

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Spotify client_id o client_secret no están configurados"
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException(
                    "No se pudo obtener el App Access Token desde Spotify"
            );
        }

        this.appAccessToken = (String) responseBody.get("access_token");

        // Spotify devuelve expires_in en segundos
        Integer expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600);
        this.tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60); // margen de seguridad
    }

    private boolean tokenExpired() {
        return tokenExpiresAt == null || Instant.now().isAfter(tokenExpiresAt);
    }
}
