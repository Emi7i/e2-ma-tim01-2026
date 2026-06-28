package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;

import java.util.concurrent.CompletableFuture;

public interface KorakPoKorakRepository {
    CompletableFuture<Void> updateSessionData(String matchId, KorakPoKorakSessionData data);
    CompletableFuture<KorakPoKorakSessionData> getSessionData(String matchId);
    void observeSessionData(String matchId, OnSessionUpdateListener<KorakPoKorakSessionData> listener);
}