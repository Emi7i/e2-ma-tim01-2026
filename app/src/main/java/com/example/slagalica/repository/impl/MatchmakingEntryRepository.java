package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.MatchmakingEntry;

import java.util.concurrent.CompletableFuture;

public interface MatchmakingEntryRepository {
    CompletableFuture<Boolean> exists(String userId);
    CompletableFuture<Void> add(String userId);
    CompletableFuture<Void> delete(String userId);

    CompletableFuture<MatchmakingEntry> getOldest(String excludeUserId);
}