package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyAppAuthService; // FIX: Agregar import
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CardsController {

    private final SpotifyCardService cardService;
    private final SpotifyAppAuthService appAuthService; // FIX: Inyectar para app token

    public CardsController(SpotifyCardService cardService, SpotifyAppAuthService appAuthService) {
        this.cardService = cardService;
        this.appAuthService = appAuthService;
    }

    @GetMapping("/cards")
    public List<TrackCardDto> generateCards(
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

        return cardService.generateCardsFromUrl(url, headers);
    }
}