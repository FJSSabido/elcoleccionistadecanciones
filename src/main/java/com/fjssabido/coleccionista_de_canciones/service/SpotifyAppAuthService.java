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
public class SpotifyAppAuthService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    // 🔴 ESTE CAMPO NO EXISTÍA
    private String appAccessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ TOKEN QUE USA TODA LA APP PARA DATOS PÚBLICOS
    public String getAppAccessToken() {
        if (appAccessToken == null) {
            authenticateApp();
        }
        return appAccessToken;
    }

    // 🔐 AUTENTICACIÓN APP (Client Credentials Flow)
    private void authenticateApp() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("No se pudo obtener el App Access Token");
        }

        // ✅ AQUÍ SE GUARDA (ANTES NO EXISTÍA)
        this.appAccessToken = (String) responseBody.get("access_token");
    }
}
