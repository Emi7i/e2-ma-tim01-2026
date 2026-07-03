package com.example.slagalica.domain.model.match.games.mojbroj;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MojBrojSessionData {
    private int currentRound;
    private String currentPlayer;
    private boolean hasEnded;
    private int goalNumber;
    private List<Integer> operands;
    private List<String> player1Tokens;
    private int player1Result;
    private List<String> player2Tokens;
    private int player2Result;
}