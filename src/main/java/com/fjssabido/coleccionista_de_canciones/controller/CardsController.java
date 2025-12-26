package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CardsController {

    private final SpotifyCardService cardService;

    public CardsController(SpotifyCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public List<TrackCardDto> generateCards(@RequestParam String url) {
        try {
            return cardService.generateCardsFromUrl(url);
        } catch (RuntimeException e) {
            if ("NOT_AUTHENTICATED".equals(e.getMessage())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Debes conectarte primero con Spotify"
                );
            }
            throw e;
        }
    }
}

