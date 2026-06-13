package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    // Mocked User ID
    private static final String MOCK_USER_ID = "test_user_123";

    @Inject
    public StatisticsViewModel(UserStatisticsRepository userStatisticsRepository) {
        this.userStatisticsRepository = userStatisticsRepository;
        loadUserStatistics();
    }

    public void loadUserStatistics() {
        isLoading.setValue(true);
        userStatisticsRepository.getStatistics(MOCK_USER_ID)
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
