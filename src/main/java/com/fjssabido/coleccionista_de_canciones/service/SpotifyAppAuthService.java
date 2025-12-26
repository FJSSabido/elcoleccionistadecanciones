package com.fjssabido.coleccionista_de_canciones.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SpotifyAppAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private String appAccessToken;
    private long tokenExpirationTime = 0;

    public String getAppAccessToken() {
        if (appAccessToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            refreshAppToken();
        }
        return appAccessToken;
    }

    private void refreshAppToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        Map<String, Object> bodyResp = response.getBody();
        appAccessToken = (String) bodyResp.get("access_token");
        int expiresIn = (Integer) bodyResp.get("expires_in");
        tokenExpirationTime = System.currentTimeMillis() + (expiresIn - 60) * 1000; // Renovar antes
    }

    public HttpHeaders appAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAppAccessToken());
        return headers;
    }
}