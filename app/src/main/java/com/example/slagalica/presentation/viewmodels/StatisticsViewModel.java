package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private final UserStatisticsRepository userStatisticsRepository;
    private final MutableLiveData<UserStatistics> userStatistics = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SessionManager sessionManager;

    // Mocked User ID
    private static final String MOCK_USER_ID = "test_user_123";

    @Inject
    public StatisticsViewModel(UserStatisticsRepository userStatisticsRepository,
                               SessionManager sessionManager) {
        this.userStatisticsRepository = userStatisticsRepository;
        this.sessionManager = sessionManager;
        loadUserStatistics();
    }

    public void loadUserStatistics() {
        isLoading.setValue(true);
        String userId = sessionManager.getCurrentUserId();
        userStatisticsRepository.getStatistics(userId)
                .thenAccept(stats -> {
                    userStatistics.postValue(stats);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    error.postValue(e.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
    }

    public LiveData<UserStatistics> getUserStatistics() {
        return userStatistics;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
