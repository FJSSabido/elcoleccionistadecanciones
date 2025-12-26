package com.fjssabido.coleccionista_de_canciones.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SpotifyAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final String redirectUri = "http://127.0.0.1:8080/spotify/callback";

    private String userAccessToken;

    // 🔑 ESTE es el token que usa TODA la app
    public String getUserAccessToken() {
        return userAccessToken;
    }

    public String getAuthorizationUrl() {
        String scopes = String.join(" ",
                "playlist-read-private",
                "playlist-read-collaborative"
        );

        return "https://accounts.spotify.com/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    public void exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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

        this.userAccessToken = (String) response.getBody().get("access_token");
    }
}
