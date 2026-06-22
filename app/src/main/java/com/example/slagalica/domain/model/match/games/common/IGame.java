package com.example.slagalica.domain.model.match.games.common;

public interface IGame {
    long getId();
    int getRoundLength();
    int getRounds();
    int getCurrentRound();
    long getCurrentPlayer();
    void setCurrentPlayer(long playerId);
    long getMatchId();
    long getPlayer1Id();
    long getPlayer2Id();
    boolean hasEnded();

    void startNewRound();
    long getOtherPlayer();
    void setOnPointsChangedListener(OnPointsChangedListener listener);
    void setOnGameEndedListener(OnGameEndedListener listener);
    void setOnActivePlayerChangedListener(OnActivePlayerChangedListener listener);
}
