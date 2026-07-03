package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.ranking.RankingCycle;
import com.example.slagalica.domain.model.ranking.RankingEntry;
import com.example.slagalica.domain.model.ranking.RankingReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RankingRepository {

    CompletableFuture<Void> recordClassicMatch(
            String matchId,
            UserProfile player1,
            long player1StarDelta,
            UserProfile player2,
            long player2StarDelta,
            long finishedAtMillis
    );

    CompletableFuture<List<RankingEntry>> getLeaderboard(String cycleId);

    CompletableFuture<List<RankingCycle>> getUndistributedExpiredCycles(
            long nowMillis
    );

    CompletableFuture<Void> finalizeExpiredCycles(long nowMillis);

    CompletableFuture<RankingReward> getFirstUnseenRewardForUser(
            String userId
    );

    CompletableFuture<Void> markRewardSeen(String rewardId);
}
