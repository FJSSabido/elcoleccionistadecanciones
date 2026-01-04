package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.dto.PlaylistResponseDto;
import com.fjssabido.coleccionista_de_canciones.dto.TrackCardDto;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyAuthService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyCardService;
import com.fjssabido.coleccionista_de_canciones.service.SpotifyTrackService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ShareController {

    private final SpotifyTrackService trackService;
    private final SpotifyCardService cardService;
    private final SpotifyAuthService authService;

    public ShareController(
            SpotifyTrackService trackService,
            SpotifyCardService cardService,
            SpotifyAuthService authService
    ) {
        this.trackService = trackService;
        this.cardService = cardService;
        this.authService = authService;
    }

    @GetMapping("/track/{id}")
    public String shareTrack(@PathVariable String id, Model model) {

        HttpHeaders headers = authService.getAuthHeaders();

        TrackCardDto track = trackService.getTrack(id, headers, "ES");

        model.addAttribute("title", track.getTitle() + " · El Coleccionista de Canciones");
        model.addAttribute("description", track.getArtist() + " · " + track.getAlbum());
        model.addAttribute("image", track.getImageUrl());
        model.addAttribute("url", "https://tudominio.com/track/" + id);

        // para redirigir luego al frontend
        model.addAttribute("redirect", "/?spotify=https://open.spotify.com/track/" + id);

        return "share";
    }

    @GetMapping("/playlist/{id}")
    public String sharePlaylist(@PathVariable String id, Model model) {

        PlaylistResponseDto playlist = cardService.getPlaylistById(id);

        model.addAttribute("title", playlist.getName() + " · El Coleccionista de Canciones");
        model.addAttribute("description", playlist.getTotalTracks() + " canciones");
        model.addAttribute("image", playlist.getImageUrl());
        model.addAttribute("url", "https://tudominio.com/playlist/" + id);

        model.addAttribute(
                "redirect",
                "/?spotify=https://open.spotify.com/playlist/" + id
        );

        return "share";
    }


    @GetMapping("/profile/{userId}")
    public String shareProfile(@PathVariable String userId, Model model) {

        model.addAttribute("title", "Playlists de " + userId);
        model.addAttribute("description", "Descubre sus playlists públicas");
        model.addAttribute(
                "image",
                "https://tudominio.com/og/profile.png" // imagen genérica
        );
        model.addAttribute("url", "https://tudominio.com/profile/" + userId);

        model.addAttribute(
                "redirect",
                "/?profile=https://open.spotify.com/user/" + userId
        );

        return "share";
    }


}
