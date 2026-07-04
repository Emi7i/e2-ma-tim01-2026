package com.example.slagalica.domain.model.match.games;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpojniceSessionData {
    private int currentRound;
    private int currentPlayer;
    private boolean hasEnded;

    private String spojnice1Id;
    private String spojnice2Id;
    private int startingPlayerOfRound;
    private int currentLeftIndex;
    private Map<String, Integer> player1Matches;
    private Map<String, Integer> player2Matches;
    private Map<String, Integer> missedMatches;
    private boolean isWaitingForNextPlayer;
}
