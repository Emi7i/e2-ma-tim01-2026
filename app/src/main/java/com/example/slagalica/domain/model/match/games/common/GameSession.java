package com.example.slagalica.domain.model.match.games.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameSession {
    private final long matchId;
    private final long player1Id;
    private final long player2Id;

    @Setter
    private long currentPlayer;

    @Setter
    private int currentRound = 0;

    @Setter
    private boolean hasEnded = false;

    public GameSession(long matchId, long player1Id, long player2Id) {
        this.matchId = matchId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.currentPlayer = player1Id;
    }
}
