package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.RegionStats;
import com.example.slagalica.repository.impl.UserProfileRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegionViewModel extends ViewModel {

    private static final String[][] REGION_ICONS = {
        {"Vojvodina",                  "🌷"},
        {"Beograd",                    "🏙"},
        {"Sumadija i Zapadna Srbija",  "🌲"},
        {"Juzna i Istocna Srbija",     "🍵"},
        {"Kosovo i Metohija",          "🏔"}
    };

    private final UserProfileRepository userProfileRepository;
    private final MutableLiveData<List<RegionStats>> regionStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public RegionViewModel(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public void loadRegionStats() {
        isLoading.setValue(true);
        userProfileRepository.getAllProfiles()
                .thenAccept(profiles -> {
                    List<RegionStats> stats = computeRegionStats(profiles);
                    regionStats.postValue(stats);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    isLoading.postValue(false);
                    return null;
                });
    }

    private List<RegionStats> computeRegionStats(List<UserProfile> profiles) {
        Map<String, RegionStats> map = new LinkedHashMap<>();
        for (String[] pair : REGION_ICONS) {
            map.put(pair[0], new RegionStats(pair[0], pair[1]));
        }

        for (UserProfile p : profiles) {
            String region = p.getRegion();
            if (region != null && map.containsKey(region)) {
                map.get(region).addPlayer(p.getMonthlyStars());
            }
        }

        List<RegionStats> sorted = new ArrayList<>(map.values());
        sorted.sort((a, b) -> Long.compare(b.getTotalMonthlyStars(), a.getTotalMonthlyStars()));
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setRank(i + 1);
        }
        return sorted;
    }

    public static String getIconForRegion(String regionKey) {
        for (String[] pair : REGION_ICONS) {
            if (pair[0].equals(regionKey)) return pair[1];
        }
        return "📍";
    }

    public LiveData<List<RegionStats>> getRegionStats() { return regionStats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}
