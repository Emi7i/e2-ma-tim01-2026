package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.ranking.RankingCycle;
import com.example.slagalica.domain.model.ranking.RankingCycleType;
import com.example.slagalica.domain.model.ranking.RankingEntry;
import com.example.slagalica.domain.model.ranking.RankingReward;
import com.example.slagalica.domain.service.ranking.RankingCycleUtils;
import com.example.slagalica.repository.impl.RankingRepository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RankingViewModel extends ViewModel {

    private final RankingRepository rankingRepository;

    private final MutableLiveData<List<RankingEntry>> entries =
            new MutableLiveData<>(Collections.emptyList());

    private final MutableLiveData<RankingCycle> displayedCycle =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<RankingReward> pendingReward =
            new MutableLiveData<>();

    private final AtomicBoolean rewardCheckInProgress =
            new AtomicBoolean(false);

    @Inject
    public RankingViewModel(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public void loadLeaderboard(RankingCycleType type) {
        long nowMillis = System.currentTimeMillis();
        RankingCycle cycle =
                RankingCycleUtils.currentCycle(type, nowMillis);

        displayedCycle.setValue(cycle);
        loading.setValue(true);

        rankingRepository.getLeaderboard(cycle.getCycleId())
                .thenAccept(result -> {
                    entries.postValue(result);
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(
                            throwable.getMessage() == null
                                    ? "Greška pri učitavanju rang liste."
                                    : throwable.getMessage()
                    );
                    loading.postValue(false);
                    return null;
                });
    }

    public void finalizeExpiredCyclesAndLoadReward(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        if (!rewardCheckInProgress.compareAndSet(false, true)) {
            return;
        }

        rankingRepository
                .finalizeExpiredCycles(System.currentTimeMillis())
                .thenCompose(ignored ->
                        rankingRepository
                                .getFirstUnseenRewardForUser(userId)
                )
                .thenAccept(reward -> {
                    pendingReward.postValue(reward);
                    rewardCheckInProgress.set(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(
                            throwable.getMessage() == null
                                    ? "Greška pri raspodeli nagrada."
                                    : throwable.getMessage()
                    );
                    rewardCheckInProgress.set(false);
                    return null;
                });
    }

    public void markRewardSeen(String rewardId) {
        if (rewardId == null || rewardId.trim().isEmpty()) {
            pendingReward.setValue(null);
            return;
        }

        rankingRepository.markRewardSeen(rewardId)
                .thenRun(() -> pendingReward.postValue(null))
                .exceptionally(throwable -> {
                    error.postValue(
                            throwable.getMessage() == null
                                    ? "Nagrada nije označena kao pregledana."
                                    : throwable.getMessage()
                    );
                    return null;
                });
    }

    public LiveData<List<RankingEntry>> getEntries() {
        return entries;
    }

    public LiveData<RankingCycle> getDisplayedCycle() {
        return displayedCycle;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<RankingReward> getPendingReward() {
        return pendingReward;
    }
}
