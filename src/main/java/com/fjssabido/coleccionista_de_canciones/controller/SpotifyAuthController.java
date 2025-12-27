package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.service.SpotifyAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class SpotifyAuthController {

    private final SpotifyAuthService authService;

    public SpotifyAuthController(SpotifyAuthService authService) {
        this.authService = authService;
    }

    /**
     * Redirige al login de Spotify
     */
    @GetMapping("/spotify/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect(authService.getAuthorizationUrl());
    }

    /**
     * Callback de Spotify
     * 👉 Guarda el token en sesión
     */
    @GetMapping("/spotify/callback")
    public String callback(
            @RequestParam String code,
            HttpSession session
    ) {
        String accessToken = authService.exchangeCodeForToken(code);
        session.setAttribute("SPOTIFY_USER_TOKEN", accessToken);
        return "redirect:/";
    }
}
