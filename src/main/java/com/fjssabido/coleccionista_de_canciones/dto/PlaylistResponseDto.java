package com.fjssabido.coleccionista_de_canciones.dto;

import java.util.List;

public class PlaylistResponseDto {

    private List<TrackCardDto> cards;
    private int discarded;

    // FIX: Agregamos campos para uso en listado de playlists
    private String id;
    private String name;
    private int totalTracks;

    public PlaylistResponseDto(List<TrackCardDto> cards, int discarded) {
        this.cards = cards;
        this.discarded = discarded;
    }

    public PlaylistResponseDto(String id, String name, int totalTracks) {
        // FIX: Seteamos los campos en el constructor
        this.id = id;
        this.name = name;
        this.totalTracks = totalTracks;
    }

    public List<TrackCardDto> getCards() {
        return cards;
    }

    public int getDiscarded() {
        return discarded;
    }

    // FIX: Getters para los nuevos campos
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalTracks() {
        return totalTracks;
    }
}