package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.progression.RegionStats;
import com.example.slagalica.domain.model.progression.RegionStatsDocument;
import com.example.slagalica.repository.impl.RegionStatsRepository;

import java.util.ArrayList;
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
    private final MutableLiveData<List<RegionStats>> regionStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean>           isLoading   = new MutableLiveData<>(false);

    @Inject
    public RegionViewModel(RegionStatsRepository regionStatsRepository) {
        this.regionStatsRepository = regionStatsRepository;
    }

    public void loadRegionStats() {
        isLoading.setValue(true);
        regionStatsRepository.getAllRegionStats()
                .thenAccept(docs -> {
                    regionStats.postValue(mapToRegionStats(docs));
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    isLoading.postValue(false);
                    return null;
                });
    }

    private List<RegionStats> mapToRegionStats(List<RegionStatsDocument> docs) {
        Map<String, RegionStats> map = new LinkedHashMap<>();
        for (String[] pair : REGION_ICONS) {
            map.put(pair[0], new RegionStats(pair[0], pair[1]));
        }

        for (RegionStatsDocument doc : docs) {
            RegionStats rs = map.get(doc.getRegionKey());
            if (rs == null) continue;
            rs.setTotalPlayers(doc.getRegisteredPlayers());
            rs.setActivePlayers(doc.getActivePlayers());
            rs.setTotalMonthlyStars(doc.getTotalMonthlyStars());
            rs.setFirstPlaces(doc.getFirstPlaces());
            rs.setSecondPlaces(doc.getSecondPlaces());
            rs.setThirdPlaces(doc.getThirdPlaces());
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
