package com.example.slagalica.domain.model.match.games.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameSession {
    private final String matchId;
    private final String player1Id;
    private final String player2Id;

    @Setter
    private String currentPlayer;

    @Setter
    private int currentRound = 0;

    @Setter
    private boolean hasEnded = false;

    public GameSession(String matchId, String player1Id, String player2Id) {
        this.matchId = matchId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.currentPlayer = player1Id;
    }
}
