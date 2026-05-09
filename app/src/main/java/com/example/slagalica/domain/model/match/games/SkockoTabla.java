package com.example.slagalica.domain.model.match.games;

import java.util.List;

public class SkockoTabla {

    private final String playerOneName;
    private final String playerTwoName;
    private final String timerText;
    private final List<String> secretCombination;
    private final List<SkockoPokusaj> attempts;

    private int currentRow;
    private int currentPlayer;
    private boolean solved;
    private boolean finished;
    private String winnerName;

    public SkockoTabla(String playerOneName,
                       String playerTwoName,
                       String timerText,
                       List<String> secretCombination,
                       List<SkockoPokusaj> attempts,
                       int currentRow,
                       int currentPlayer,
                       boolean solved,
                       boolean finished,
                       String winnerName) {
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.timerText = timerText;
        this.secretCombination = secretCombination;
        this.attempts = attempts;
        this.currentRow = currentRow;
        this.currentPlayer = currentPlayer;
        this.solved = solved;
        this.finished = finished;
        this.winnerName = winnerName;
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

    public List<String> getSecretCombination() {
        return secretCombination;
    }

    public List<SkockoPokusaj> getAttempts() {
        return attempts;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }
}