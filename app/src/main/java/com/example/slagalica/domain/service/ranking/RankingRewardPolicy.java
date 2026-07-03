package com.example.slagalica.domain.service.ranking;

import com.example.slagalica.domain.model.ranking.RankingCycleType;

public final class RankingRewardPolicy {

    private RankingRewardPolicy() {
    }

    public static int tokensForPlacement(
            RankingCycleType cycleType,
            int placement
    ) {
        if (placement < 1 || placement > 10) {
            return 0;
        }

        if (cycleType == RankingCycleType.WEEKLY) {
            if (placement == 1) return 5;
            if (placement == 2) return 3;
            if (placement == 3) return 2;
            return 1;
        }

        if (placement == 1) return 10;
        if (placement == 2) return 6;
        if (placement == 3) return 4;
        return 2;
    }
}
