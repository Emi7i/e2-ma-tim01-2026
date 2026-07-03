package com.example.slagalica.domain.model.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {
    private final FirebaseAuth firebaseAuth;
    private final UserProfileRepository userProfileRepository;
    private final RegionStatsRepository regionStatsRepository;
    private final MutableLiveData<UserProfile> currentProfile = new MutableLiveData<>();
    private boolean isOnline = false;

    @Inject
    public SessionManager(FirebaseAuth firebaseAuth,
                          UserProfileRepository userProfileRepository,
                          RegionStatsRepository regionStatsRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userProfileRepository = userProfileRepository;
        this.regionStatsRepository = regionStatsRepository;
    }

    public boolean isLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public LiveData<UserProfile> getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(UserProfile profile) {
        currentProfile.postValue(profile);
    }

    public void loadCurrentProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        userProfileRepository.getProfile(user.getUid())
                .thenAccept(profile -> {
                    currentProfile.postValue(profile);
                    if (profile != null && profile.getRegion() != null) {
                        applyOnlineState(profile, true);
                    }
                })
                .exceptionally(ex -> { currentProfile.postValue(null); return null; });
    }

    public void setUserOnline(boolean online) {
        UserProfile profile = currentProfile.getValue();
        if (profile == null || profile.getRegion() == null) return;
        applyOnlineState(profile, online);
    }

    // profile.isActive() is the persisted source of truth (survives app kill/relaunch),
    // unlike isOnline which only lives in memory for this process. Without this check,
    // relaunching the app during testing (no clean logout) would increment activePlayers
    // again every time without ever decrementing it.
    private void applyOnlineState(UserProfile profile, boolean online) {
        isOnline = online;
        if (profile.isActive() == online) return;
        profile.setActive(online);
        regionStatsRepository.incrementField(profile.getRegion(), "activePlayers", online ? 1L : -1L);
        userProfileRepository.saveProfile(profile);
    }

    public String getCurrentUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    public void clear() {
        setUserOnline(false);
        firebaseAuth.signOut();
        currentProfile.postValue(null);
        isOnline = false;
    }
}
