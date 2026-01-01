package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyAppAuthService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final SpotifyCardService cardService;
    private final SpotifyAppAuthService appAuthService;

    public TrackController(SpotifyCardService cardService,
                           SpotifyAppAuthService appAuthService) {
        this.cardService = cardService;
        this.appAuthService = appAuthService;
    }

    /**
     * Devuelve la carta de una canción a partir de su ID
     */
    @GetMapping("/{id}/card")
    public TrackCardDto getTrackCard(@PathVariable String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(appAuthService.getAppAccessToken());

        // Fixed: Extract "cards" from the Map and get the first (and only) TrackCardDto
        Map<String, Object> response = cardService.generateCardsFromUrl(
                "https://open.spotify.com/track/" + id,
                headers
        );
        @SuppressWarnings("unchecked")
        List<TrackCardDto> cards = (List<TrackCardDto>) response.get("cards");
        return cards.get(0);
    }
}