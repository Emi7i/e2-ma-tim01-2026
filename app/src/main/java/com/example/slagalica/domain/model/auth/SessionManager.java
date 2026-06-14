package com.example.slagalica.domain.model.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {
    private final FirebaseAuth firebaseAuth;
    private final UserProfileRepository userProfileRepository;
    private final MutableLiveData<UserProfile> currentProfile = new MutableLiveData<>();

    @Inject
    public SessionManager(FirebaseAuth firebaseAuth, UserProfileRepository userProfileRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userProfileRepository = userProfileRepository;
    }

    public boolean isLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public LiveData<UserProfile> getCurrentProfile() {
        return currentProfile;
    }

    public void loadCurrentProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        userProfileRepository.getProfile(user.getUid())
                .thenAccept(currentProfile::postValue)
                .exceptionally(ex -> { currentProfile.postValue(null); return null; });
    }

    public void clear() {
        firebaseAuth.signOut();
        currentProfile.postValue(null);
    }
}
