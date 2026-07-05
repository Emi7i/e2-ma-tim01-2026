package com.example.slagalica.domain.model.match;

public class AcceptedMatch {
    public final String player1Id;
    public final String player2Id;
    public final String matchId;
    public AcceptedMatch(String player1Id, String player2Id, String matchId) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.matchId = matchId;
    }
}
