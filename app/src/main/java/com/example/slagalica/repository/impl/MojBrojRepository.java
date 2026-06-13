package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBrojSessionData;

import java.util.concurrent.CompletableFuture;

public interface MojBrojRepository {
    CompletableFuture<Void> updateSessionData(long matchId, MojBrojSessionData data);
    CompletableFuture<MojBrojSessionData> getSessionData(long matchId);
    void observeSessionData(long matchId, OnSessionUpdateListener<MojBrojSessionData> listener);
}