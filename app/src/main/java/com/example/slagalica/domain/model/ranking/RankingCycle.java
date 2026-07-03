package com.example.slagalica.domain.model.ranking;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class RankingCycle {

    @DocumentId
    private String cycleId;

    private String type;
    private long startMillis;
    private long endMillis;
    private boolean rewardsDistributed;
    private long rewardedAtMillis;

    public RankingCycle() {
    }

    public RankingCycle(
            String cycleId,
            RankingCycleType type,
            long startMillis,
            long endMillis,
            boolean rewardsDistributed,
            long rewardedAtMillis
    ) {
        this.cycleId = cycleId;
        this.type = type.name();
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.rewardsDistributed = rewardsDistributed;
        this.rewardedAtMillis = rewardedAtMillis;
    }

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public boolean isRewardsDistributed() {
        return rewardsDistributed;
    }

    public void setRewardsDistributed(boolean rewardsDistributed) {
        this.rewardsDistributed = rewardsDistributed;
    }

    public long getRewardedAtMillis() {
        return rewardedAtMillis;
    }

    public void setRewardedAtMillis(long rewardedAtMillis) {
        this.rewardedAtMillis = rewardedAtMillis;
    }

    @Exclude
    public RankingCycleType getCycleType() {
        return RankingCycleType.valueOf(type);
    }
}
