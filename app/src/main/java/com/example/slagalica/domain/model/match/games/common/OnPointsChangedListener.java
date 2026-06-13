package com.example.slagalica.domain.model.match.games.common;

public interface OnPointsChangedListener {
    void onPointsChanged(long playerId, int amount);
}
