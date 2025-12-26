package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final SpotifyCardService cardService;

    public TrackController(SpotifyCardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Devuelve la carta de una canción a partir de su ID
     */
    @GetMapping("/{id}/card")
    public TrackCardDto getTrackCard(@PathVariable String id) {
        return cardService.generateCardsFromUrl(
                "https://open.spotify.com/track/" + id
        ).get(0);
    }
}
