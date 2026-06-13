package com.example.slagalica.domain.model.match.games;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;

import java.util.List;

public class Asocijacija {

    private final TwoPlayerGameState gameState;
    private final List<AsocijacijaKolona> columns;
    private final String finalSolution;

    private boolean finalSolved;
    private boolean guessWindowOpen;

    public Asocijacija(TwoPlayerGameState gameState,
                       List<AsocijacijaKolona> columns,
                       String finalSolution) {
        this.gameState = gameState;
        this.columns = columns;
        this.finalSolution = finalSolution;
        this.finalSolved = false;
        this.guessWindowOpen = false;
    }

    public TwoPlayerGameState getGameState() {
        return gameState;
    }

    public List<AsocijacijaKolona> getColumns() {
        return columns;
    }

    public String getFinalSolution() {
        return finalSolution;
    }

    public boolean isFinalSolved() {
        return finalSolved;
    }

    public void setFinalSolved(boolean finalSolved) {
        this.finalSolved = finalSolved;
    }

    public boolean isGuessWindowOpen() {
        return guessWindowOpen;
    }

    public void setGuessWindowOpen(boolean guessWindowOpen) {
        this.guessWindowOpen = guessWindowOpen;
    }
}