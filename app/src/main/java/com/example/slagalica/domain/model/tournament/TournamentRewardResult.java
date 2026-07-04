package com.example.slagalica.domain.model.tournament;

public class TournamentRewardResult {

    private long winnerStarDelta;
    private long loserStarDelta;
    private int winnerTokenReward;
    private int loserTokenReward;

    public TournamentRewardResult(
            long winnerStarDelta,
            long loserStarDelta,
            int winnerTokenReward,
            int loserTokenReward
    ) {
        this.winnerStarDelta = winnerStarDelta;
        this.loserStarDelta = loserStarDelta;
        this.winnerTokenReward = winnerTokenReward;
        this.loserTokenReward = loserTokenReward;
    }

    public long getWinnerStarDelta() { return winnerStarDelta; }
    public long getLoserStarDelta() { return loserStarDelta; }
    public int getWinnerTokenReward() { return winnerTokenReward; }
    public int getLoserTokenReward() { return loserTokenReward; }
}
