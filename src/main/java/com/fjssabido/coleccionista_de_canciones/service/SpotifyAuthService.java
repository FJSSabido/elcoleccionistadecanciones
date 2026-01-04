package com.fjssabido.coleccionista_de_canciones.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SpotifyAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String appAccessToken;
    private long appTokenExpiresAt = 0;


    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spotify.scopes}")
    private String scopes;

    private synchronized String getAccessToken() {

        // Si el token sigue siendo válido, reutilízalo
        if (appAccessToken != null && System.currentTimeMillis() < appTokenExpiresAt) {
            return appAccessToken;
        }

        // Si no, pide uno nuevo (Client Credentials Flow)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Error al obtener el app access token de Spotify");
        }

        appAccessToken = (String) response.getBody().get("access_token");

        Integer expiresIn = (Integer) response.getBody().get("expires_in");

        // Guardamos el momento de expiración (con margen de seguridad)
        appTokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L) - 60_000;

        return appAccessToken;
    }


    /**
     * URL a la que redirigimos al usuario para hacer login en Spotify
     */
    public String getAuthorizationUrl() {
        return "https://accounts.spotify.com/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&scope=" + scopes
                + "&redirect_uri=" + redirectUri;
    }

    /**
     * Intercambia el 'code' por un access_token
     * 👉 DEVUELVE el token (antes era void)
     */
    public String exchangeCodeForToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Error al obtener el token de Spotify");
        }

        return (String) response.getBody().get("access_token");
    }

    public HttpHeaders getAuthHeaders() {

        // Asegura que el token existe / está actualizado
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return headers;
    }
}
