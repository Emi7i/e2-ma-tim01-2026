package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.profile.UserProfile;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserProfileRepository {
    CompletableFuture<UserProfile> getProfile(String userId);
    CompletableFuture<Void> saveProfile(UserProfile profile);
    CompletableFuture<UserProfile> findByUsername(String username);
    CompletableFuture<List<UserProfile>> getAllProfiles();
}
