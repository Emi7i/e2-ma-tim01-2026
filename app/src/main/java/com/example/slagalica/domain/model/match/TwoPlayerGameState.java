package com.example.slagalica.domain.model.match;

public class TwoPlayerGameState {

    private final String playerOneName;
    private final String playerTwoName;

    private int roundNumber;
    private int startingPlayer;
    private int currentPlayer;
    private int remainingSeconds;

    private int playerOneScore;
    private int playerTwoScore;

    private boolean roundFinished;
    private boolean gameFinished;

    public TwoPlayerGameState(String playerOneName,
                              String playerTwoName,
                              int roundNumber,
                              int startingPlayer,
                              int currentPlayer,
                              int remainingSeconds,
                              int playerOneScore,
                              int playerTwoScore,
                              boolean roundFinished,
                              boolean gameFinished) {
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.roundNumber = roundNumber;
        this.startingPlayer = startingPlayer;
        this.currentPlayer = currentPlayer;
        this.remainingSeconds = remainingSeconds;
        this.playerOneScore = playerOneScore;
        this.playerTwoScore = playerTwoScore;
        this.roundFinished = roundFinished;
        this.gameFinished = gameFinished;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }

    public void setStartingPlayer(int startingPlayer) {
        this.startingPlayer = startingPlayer;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public int getPlayerOneScore() {
        return playerOneScore;
    }

    public void setPlayerOneScore(int playerOneScore) {
        this.playerOneScore = playerOneScore;
    }

    public int getPlayerTwoScore() {
        return playerTwoScore;
    }

    public void setPlayerTwoScore(int playerTwoScore) {
        this.playerTwoScore = playerTwoScore;
    }

    public boolean isRoundFinished() {
        return roundFinished;
    }

    public void setRoundFinished(boolean roundFinished) {
        this.roundFinished = roundFinished;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(boolean gameFinished) {
        this.gameFinished = gameFinished;
    }

    public String getCurrentPlayerName() {
        return currentPlayer == 1 ? playerOneName : playerTwoName;
    }

    public void switchTurn() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    public void addPointsToPlayer(int player, int points) {
        if (player == 1) {
            playerOneScore += points;
        } else {
            playerTwoScore += points;
        }
    }
}