package com.example.slagalica.domain.service.progression;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class RegionCycleService {

    public static final String TIER_GOLD   = "gold";
    public static final String TIER_SILVER = "silver";
    public static final String TIER_BRONZE = "bronze";

    private final UserProfileRepository userProfileRepository;
    private final RegionStatsRepository regionStatsRepository;

    @Inject
    public RegionCycleService(UserProfileRepository userProfileRepository,
                               RegionStatsRepository regionStatsRepository) {
        this.userProfileRepository = userProfileRepository;
        this.regionStatsRepository = regionStatsRepository;
    }

    // Ranks regions by this cycle's total stars, credits the top 3 regions'
    // historical placement counters, assigns every player in a top-3 region the
    // matching avatar-border tier, then resets everyone's monthlyStars to 0 for
    // the next cycle. Region-vs-region only, not a per-player rank list.
    public CompletableFuture<Void> endCycleNow() {
        return userProfileRepository.getAllProfiles().thenCompose(profiles -> {
            Map<String, Long> starsByRegion = new HashMap<>();
            for (UserProfile profile : profiles) {
                String region = profile.getRegion();
                if (region == null) continue;
                starsByRegion.merge(region, profile.getMonthlyStars(), Long::sum);
            }

            List<String> ranked = new ArrayList<>(starsByRegion.keySet());
            ranked.sort((a, b) -> Long.compare(starsByRegion.get(b), starsByRegion.get(a)));

            String first  = ranked.size() > 0 ? ranked.get(0) : null;
            String second = ranked.size() > 1 ? ranked.get(1) : null;
            String third  = ranked.size() > 2 ? ranked.get(2) : null;

            List<CompletableFuture<Void>> writes = new ArrayList<>();
            for (UserProfile profile : profiles) {
                String region = profile.getRegion();
                String tier = null;
                if (region != null) {
                    if (region.equals(first))            tier = TIER_GOLD;
                    else if (region.equals(second))      tier = TIER_SILVER;
                    else if (region.equals(third))       tier = TIER_BRONZE;
                }
                profile.setRegionRankTier(tier);
                profile.setMonthlyStars(0);
                writes.add(userProfileRepository.saveProfile(profile));
            }

            if (first  != null) writes.add(regionStatsRepository.incrementField(first,  "firstPlaces",  1L));
            if (second != null) writes.add(regionStatsRepository.incrementField(second, "secondPlaces", 1L));
            if (third  != null) writes.add(regionStatsRepository.incrementField(third,  "thirdPlaces",  1L));

            return CompletableFuture.allOf(writes.toArray(new CompletableFuture[0]));
        });
    }

    // One-time migration helper: monthlyStars was only just wired up, so existing
    // profiles have monthlyStars=0 despite having real lifetime numStars. Seeds
    // monthlyStars from numStars for any profile that hasn't accrued any monthly
    // stars yet, so region totals aren't sitting at zero for pre-existing accounts.
    public CompletableFuture<Void> backfillMonthlyStarsFromTotal() {
        return userProfileRepository.getAllProfiles().thenCompose(profiles -> {
            List<CompletableFuture<Void>> writes = new ArrayList<>();
            for (UserProfile profile : profiles) {
                if (profile.getMonthlyStars() == 0 && profile.getNumStars() > 0) {
                    profile.setMonthlyStars(profile.getNumStars());
                    writes.add(userProfileRepository.saveProfile(profile));
                }
            }
            return CompletableFuture.allOf(writes.toArray(new CompletableFuture[0]));
        });
    }
}
