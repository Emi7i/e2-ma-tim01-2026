package com.example.slagalica.domain.model.match.games;

import java.util.List;

public class Asocijacija {

    private final String playerOneName;
    private final String playerTwoName;
    private final String timerText;
    private final List<AsocijacijaKolona> columns;
    private final String finalSolution;
    private boolean finalSolved;

    public Asocijacija(String playerOneName,
                       String playerTwoName,
                       String timerText,
                       List<AsocijacijaKolona> columns,
                       String finalSolution,
                       boolean finalSolved) {
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.timerText = timerText;
        this.columns = columns;
        this.finalSolution = finalSolution;
        this.finalSolved = finalSolved;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public String getTimerText() {
        return timerText;
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
}