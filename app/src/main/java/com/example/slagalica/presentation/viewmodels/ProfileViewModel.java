package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
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
    private final SessionManager sessionManager;
    // Mocked User ID
    private static final String MOCK_USER_ID = "test_user_123";

    @Inject
    public ProfileViewModel(UserProfileRepository userProfileRepository,
                            SessionManager sessionManager) {
        this.userProfileRepository = userProfileRepository;
        this.sessionManager = sessionManager;
    }

    public void loadUserProfile() {
//        isLoading.setValue(true);
//        userProfileRepository.getProfile(MOCK_USER_ID)
//                .thenAccept(profile -> {
//                    userProfile.postValue(profile);
//                    isLoading.postValue(false);
//                })
//                .exceptionally(e -> {
//                    error.postValue(e.getMessage());
//                    isLoading.postValue(false);
//                    return null;
//                });
    }

    public void updateAvatar(String newAvatarUrl) {
//        UserProfile current = userProfile.getValue();
        UserProfile current = sessionManager.getCurrentProfile().getValue();
        if (current != null) {
            isLoading.setValue(true);
            current.setAvatar(newAvatarUrl);
            userProfileRepository.saveProfile(current)
                .thenAccept(v -> {
                    userProfile.postValue(current);
                    sessionManager.setCurrentProfile(current);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    error.postValue(e.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
        }
    }

    public void addToken() {
        UserProfile current = sessionManager.getCurrentProfile().getValue();
        if (current != null) {
            isLoading.setValue(true);
            current.setNumTokens(current.getNumTokens() + 1);
            userProfileRepository.saveProfile(current)
                .thenAccept(v -> {
                    userProfile.postValue(current);
                    sessionManager.setCurrentProfile(current);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    error.postValue(e.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
        }
    }

    public LiveData<UserProfile> getUserProfile() {
        return sessionManager.getCurrentProfile();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
