package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.profile.UserProfile;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserProfileRepository {
    CompletableFuture<UserProfile> getProfile(String userId);
    CompletableFuture<Void> saveProfile(UserProfile profile);
    CompletableFuture<Void> updateFields(String userId, Map<String, Object> fields);
    CompletableFuture<Void> deleteProfile(String userId);
    CompletableFuture<UserProfile> findByUsername(String username);
    CompletableFuture<List<UserProfile>> getAllProfiles();
}
