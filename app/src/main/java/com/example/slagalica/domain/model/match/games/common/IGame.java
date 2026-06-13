package com.example.slagalica.domain.model.match.games.common;

public interface IGame {
    long getId();
    int getRoundLength();
    int getRounds();
    int getMaxPoints();
    int getMinPoints();
    int getCurrentRound();
    long getCurrentPlayer();
    long getMatchId();
    long getPlayer1Id();
    long getPlayer2Id();
    boolean hasEnded();
    void setOnPointsChangedListener(OnPointsChangedListener listener);
    void setOnGameEndedListener(OnGameEndedListener listener);
}
