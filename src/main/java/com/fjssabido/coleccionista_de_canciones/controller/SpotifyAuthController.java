package com.fjssabido.coleccionista_de_canciones.controller;

import com.fjssabido.coleccionista_de_canciones.service.SpotifyAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class SpotifyAuthController {

    private final SpotifyAuthService authService;

    public SpotifyAuthController(SpotifyAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/spotify/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect(authService.getAuthorizationUrl());
    }

    @GetMapping("/spotify/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        authService.exchangeCodeForToken(code);
        response.sendRedirect("/"); // vuelve al frontend
    }
}
