// Modified: CardsController.java
package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyAppAuthService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CardsController {

    private final SpotifyCardService cardService;
    private final SpotifyAppAuthService appAuthService;

    public CardsController(SpotifyCardService cardService, SpotifyAppAuthService appAuthService) {
        this.cardService = cardService;
        this.appAuthService = appAuthService;
    }

    @GetMapping("/cards")
    public Map<String, Object> generateCards(
            @RequestParam String url,
            HttpSession session
    ) {
        String userToken = (String) session.getAttribute("SPOTIFY_USER_TOKEN");

        String token;
        if (userToken != null) {
            token = userToken; // Usa user token si disponible (para privadas)
        } else {
            token = appAuthService.getAppAccessToken(); // Fallback a app token para públicas
        }

        if (token == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes conectarte primero con Spotify para playlists privadas"
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return cardService.generateCardsFromUrl(url, headers); // Now returns Map
    }
}