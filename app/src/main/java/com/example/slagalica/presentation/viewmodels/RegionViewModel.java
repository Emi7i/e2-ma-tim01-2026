package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.RegionStats;
import com.example.slagalica.domain.model.progression.RegionStatsDocument;
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
    private final MutableLiveData<List<RegionStats>> regionStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean>           isLoading   = new MutableLiveData<>(false);

    @Inject
    public RegionViewModel(RegionStatsRepository regionStatsRepository,
                            UserProfileRepository userProfileRepository) {
        this.regionStatsRepository = regionStatsRepository;
        this.userProfileRepository = userProfileRepository;
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

    private List<RegionStats> mapToRegionStats(List<RegionStatsDocument> docs, List<UserProfile> profiles) {
        Map<String, RegionStats> map = new LinkedHashMap<>();
        for (String[] pair : REGION_ICONS) {
            map.put(pair[0], new RegionStats(pair[0], pair[1]));
        }

        for (RegionStatsDocument doc : docs) {
            RegionStats rs = map.get(doc.getRegionKey());
            if (rs == null) continue;
            rs.setTotalPlayers(doc.getRegisteredPlayers());
            rs.setActivePlayers(doc.getActivePlayers());
            rs.setFirstPlaces(doc.getFirstPlaces());
            rs.setSecondPlaces(doc.getSecondPlaces());
            rs.setThirdPlaces(doc.getThirdPlaces());
        }

        // totalMonthlyStars isn't tracked as a Firestore counter anywhere — compute it
        // live by summing each region's users' stars (monthlyStars is never written
        // anywhere; numStars is the field gameplay actually populates).
        Map<String, Long> starsByRegion = new HashMap<>();
        for (UserProfile profile : profiles) {
            String region = profile.getRegion();
            if (region == null || !map.containsKey(region)) continue;
            starsByRegion.merge(region, profile.getNumStars(), Long::sum);
        }
        for (Map.Entry<String, Long> entry : starsByRegion.entrySet()) {
            map.get(entry.getKey()).setTotalMonthlyStars(entry.getValue());
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
