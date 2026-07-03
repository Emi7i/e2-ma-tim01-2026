package com.example.slagalica.domain.model.match.games.common;

public interface IGame {
    int getId();
    int getRoundLength();
    int getRounds();
    int getCurrentRound();
    String getCurrentPlayer();
    void setCurrentPlayer(String playerId);
    String getMatchId();
    String getPlayer1Id();
    String getPlayer2Id();
    boolean hasEnded();

    void startNewRound();
    String getOtherPlayer();
    void setOnPointsChangedListener(OnPointsChangedListener listener);
    void setOnGameEndedListener(OnGameEndedListener listener);
    void setOnActivePlayerChangedListener(OnActivePlayerChangedListener listener);
}
