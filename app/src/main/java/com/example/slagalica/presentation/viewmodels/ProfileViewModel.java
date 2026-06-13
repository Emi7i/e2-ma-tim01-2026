package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.UserProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final UserProfileRepository userProfileRepository;
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Mocked User ID
    private static final String MOCK_USER_ID = "test_user_123";

    @Inject
    public ProfileViewModel(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
        loadUserProfile();
    }

    public void loadUserProfile() {
        isLoading.setValue(true);
        userProfileRepository.getProfile(MOCK_USER_ID)
                .thenAccept(profile -> {
                    userProfile.postValue(profile);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    error.postValue(e.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
