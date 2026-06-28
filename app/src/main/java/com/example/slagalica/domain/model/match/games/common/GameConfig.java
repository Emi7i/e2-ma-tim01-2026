package com.example.slagalica.domain.model.match.games.common;

import lombok.Getter;

@Getter
public class GameConfig {
    private final long id;
    private final int roundLength;
    private final int rounds;
    private final int maxPoints;
    private final int minPoints;

    public GameConfig(long id, int roundLength, int rounds, int maxPoints, int minPoints) {
        this.id = id;
        this.roundLength = roundLength;
        this.rounds = rounds;
        this.maxPoints = maxPoints;
        this.minPoints = minPoints;
    }
}