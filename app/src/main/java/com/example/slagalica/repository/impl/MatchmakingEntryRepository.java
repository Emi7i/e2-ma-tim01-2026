package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.MatchmakingEntry;
import com.example.slagalica.domain.model.match.games.common.OnMatchmakingUpdateListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.concurrent.CompletableFuture;

public interface MatchmakingEntryRepository {
    CompletableFuture<Boolean> exists(String userId);
    CompletableFuture<String> add(String userId); // returns match id
    CompletableFuture<Void> delete(String userId);

    CompletableFuture<MatchmakingEntry> getOldest(String excludeUserId);

    CompletableFuture<Void> claim(String userId, String matchedWith);
    ListenerRegistration observeEntry(String userId, OnMatchmakingUpdateListener listener);
}