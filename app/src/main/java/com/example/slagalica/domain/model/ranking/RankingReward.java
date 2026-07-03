package com.example.slagalica.domain.model.ranking;

import com.google.firebase.firestore.Exclude;

public class RankingReward {

    private String rewardId;
    private String userId;
    private String cycleId;
    private String cycleType;
    private int placement;
    private int tokenReward;
    private long createdAtMillis;
    private boolean seen;

    public RankingReward() {
    }

    public RankingReward(
            String rewardId,
            String userId,
            String cycleId,
            RankingCycleType cycleType,
            int placement,
            int tokenReward,
            long createdAtMillis,
            boolean seen
    ) {
        this.rewardId = rewardId;
        this.userId = userId;
        this.cycleId = cycleId;
        this.cycleType = cycleType.name();
        this.placement = placement;
        this.tokenReward = tokenReward;
        this.createdAtMillis = createdAtMillis;
        this.seen = seen;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getCycleType() {
        return cycleType;
    }

    public void setCycleType(String cycleType) {
        this.cycleType = cycleType;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public int getTokenReward() {
        return tokenReward;
    }

    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Exclude
    public RankingCycleType getCycleTypeEnum() {
        return RankingCycleType.valueOf(cycleType);
    }
}
