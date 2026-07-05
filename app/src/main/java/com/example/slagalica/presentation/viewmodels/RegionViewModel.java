package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.RegionStats;
import com.example.slagalica.domain.model.progression.RegionStatsDocument;
import com.example.slagalica.domain.service.progression.RegionCycleService;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegionViewModel extends ViewModel {

    static final String[][] REGION_ICONS = {
        {"Vojvodina",                 "🌷"},
        {"Beograd",                   "🏙"},
        {"Sumadija i Zapadna Srbija", "🌲"},
        {"Juzna i Istocna Srbija",    "🍵"},
        {"Kosovo i Metohija",         "🏔"}
    };

    private final RegionStatsRepository regionStatsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RegionCycleService    regionCycleService;
    private final MutableLiveData<List<RegionStats>> regionStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean>           isLoading   = new MutableLiveData<>(false);

    @Inject
    public RegionViewModel(RegionStatsRepository regionStatsRepository,
                            UserProfileRepository userProfileRepository,
                            RegionCycleService regionCycleService) {
        this.regionStatsRepository = regionStatsRepository;
        this.userProfileRepository = userProfileRepository;
        this.regionCycleService = regionCycleService;
    }

    public void loadRegionStats() {
        isLoading.setValue(true);
        regionStatsRepository.getAllRegionStats()
                .thenCombine(userProfileRepository.getAllProfiles(), this::mapToRegionStats)
                .thenAccept(list -> {
                    regionStats.postValue(list);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    isLoading.postValue(false);
                    return null;
                });
    }

    // One-time migration hook: pre-existing profiles never had monthlyStars
    // populated, so region totals show 0 until this seeds it from numStars.
    public void backfillMonthlyStars(Runnable onComplete) {
        isLoading.setValue(true);
        regionCycleService.backfillMonthlyStarsFromTotal()
                .thenAccept(v -> {
                    isLoading.postValue(false);
                    loadRegionStats();
                    if (onComplete != null) onComplete.run();
                })
                .exceptionally(e -> {
                    isLoading.postValue(false);
                    return null;
                });
    }

    // Testing hook: ranks regions by this cycle's stars, credits the top 3 with a
    // historical placement, and resets everyone's monthlyStars — normally this
    // would run on a monthly schedule instead of on demand.
    public void endCycleNow(Runnable onComplete) {
        isLoading.setValue(true);
        regionCycleService.endCycleNow()
                .thenAccept(v -> {
                    isLoading.postValue(false);
                    loadRegionStats();
                    if (onComplete != null) onComplete.run();
                })
                .exceptionally(e -> {
                    isLoading.postValue(false);
                    return null;
                });
    }

    private List<RegionStats> mapToRegionStats(List<RegionStatsDocument> docs, List<UserProfile> profiles) {
        Map<String, RegionStats> map = new LinkedHashMap<>();
        for (String[] pair : REGION_ICONS) {
            map.put(pair[0], new RegionStats(pair[0], pair[1]));
        }

        for (RegionStatsDocument doc : docs) {
            RegionStats rs = map.get(doc.getRegionKey());
            if (rs == null) continue;
            rs.setTotalPlayers(doc.getRegisteredPlayers());
            rs.setFirstPlaces(doc.getFirstPlaces());
            rs.setSecondPlaces(doc.getSecondPlaces());
            rs.setThirdPlaces(doc.getThirdPlaces());
        }

        // activePlayers and totalMonthlyStars are computed live from the profile list
        // rather than from stored counters. Counters drift (app kills, multi-device),
        // but the profile.active flag is the authoritative per-user online state.
        Map<String, Long> starsByRegion = new HashMap<>();
        Map<String, Long> activeByRegion = new HashMap<>();
        for (UserProfile profile : profiles) {
            String region = profile.getRegion();
            if (region == null || !map.containsKey(region)) continue;
            starsByRegion.merge(region, profile.getMonthlyStars(), Long::sum);
            if (profile.isActive()) activeByRegion.merge(region, 1L, Long::sum);
        }
        for (Map.Entry<String, Long> entry : starsByRegion.entrySet()) {
            map.get(entry.getKey()).setTotalMonthlyStars(entry.getValue());
        }
        for (Map.Entry<String, Long> entry : activeByRegion.entrySet()) {
            map.get(entry.getKey()).setActivePlayers(entry.getValue());
        }

        List<RegionStats> sorted = new ArrayList<>(map.values());
        sorted.sort((a, b) -> Long.compare(b.getTotalMonthlyStars(), a.getTotalMonthlyStars()));
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).setRank(i + 1);
        return sorted;
    }

    public static String getIconForRegion(String regionKey) {
        for (String[] pair : REGION_ICONS) {
            if (pair[0].equals(regionKey)) return pair[1];
        }
        return "📍";
    }

    public LiveData<List<RegionStats>> getRegionStats() { return regionStats; }
    public LiveData<Boolean>           getIsLoading()   { return isLoading; }
}
