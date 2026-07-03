package com.example.slagalica.domain.model.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.service.progression.LeagueService;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {
    private final FirebaseAuth firebaseAuth;
    private final UserProfileRepository userProfileRepository;
    private final RegionStatsRepository regionStatsRepository;
    private final LeagueService leagueService;
    private final MutableLiveData<UserProfile> currentProfile = new MutableLiveData<>();
    private boolean isOnline = false;

    @Inject
    public SessionManager(FirebaseAuth firebaseAuth,
                          UserProfileRepository userProfileRepository,
                          RegionStatsRepository regionStatsRepository,
                          LeagueService leagueService) {
        this.firebaseAuth = firebaseAuth;
        this.userProfileRepository = userProfileRepository;
        this.regionStatsRepository = regionStatsRepository;
        this.leagueService = leagueService;
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
                    if (profile != null) {
                        grantDailyTokens(profile);
                    }
                    currentProfile.postValue(profile);
                    if (profile != null && profile.getRegion() != null) {
                        applyOnlineState(profile, true);
                    }
                })
                .exceptionally(ex -> { currentProfile.postValue(null); return null; });
    }

    // Grants the day's token allowance (base + league bonus, spec 2b) at most
    // once per calendar day. Uses a targeted field update, same reasoning as
    // applyOnlineState below: a full saveProfile(profile) here could race with
    // and clobber a concurrent write (e.g. stars from an in-flight match).
    private void grantDailyTokens(UserProfile profile) {
        if (!leagueService.grantDailyTokensIfNeeded(profile)) {
            return;
        }
        Map<String, Object> fields = new HashMap<>();
        fields.put("numTokens", profile.getNumTokens());
        fields.put("lastTokenGrantDate", profile.getLastTokenGrantDate());
        userProfileRepository.updateFields(profile.getUserId(), fields);
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
    //
    // Uses a targeted field update rather than saveProfile(profile) — this runs on
    // every login/app-open, and a full-object overwrite here can race with any other
    // write to the same document (e.g. stars from a match, a backfill) and silently
    // clobber it back to whatever stale snapshot this profile object was fetched from.
    private void applyOnlineState(UserProfile profile, boolean online) {
        isOnline = online;
        if (profile.isActive() == online) return;
        profile.setActive(online);
        regionStatsRepository.incrementField(profile.getRegion(), "activePlayers", online ? 1L : -1L);
        userProfileRepository.updateFields(profile.getUserId(), java.util.Collections.singletonMap("active", online));
    }

    // Whether the app is currently foregrounded (kept up to date by
    // AppActivity.onStart()/onStop() via setUserOnline). Used to decide between
    // an in-app banner and a system notification for events like league changes.
    public boolean isOnline() {
        return isOnline;
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
