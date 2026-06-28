package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBrojSessionData;

import java.util.concurrent.CompletableFuture;

public interface MojBrojRepository {
    CompletableFuture<Void> updateSessionData(String matchId, MojBrojSessionData data);
    CompletableFuture<MojBrojSessionData> getSessionData(String matchId);
    void observeSessionData(String matchId, OnSessionUpdateListener<MojBrojSessionData> listener);
}