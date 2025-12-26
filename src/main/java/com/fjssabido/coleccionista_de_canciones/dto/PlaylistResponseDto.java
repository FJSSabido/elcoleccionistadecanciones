package com.fjssabido.coleccionista_de_canciones.dto;

import java.util.List;

public class PlaylistResponseDto {

    private List<TrackCardDto> cards;
    private int discarded;

    public PlaylistResponseDto(List<TrackCardDto> cards, int discarded) {
        this.cards = cards;
        this.discarded = discarded;
    }

    public List<TrackCardDto> getCards() {
        return cards;
    }

    public int getDiscarded() {
        return discarded;
    }
}

