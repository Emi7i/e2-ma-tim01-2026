package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.SpojniceSessionData;
import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;

import java.util.concurrent.CompletableFuture;

public interface SpojniceSessionRepository {
    CompletableFuture<Void> updateSessionData(String matchId, SpojniceSessionData data);
    CompletableFuture<KorakPoKorakSessionData> getSessionData(String matchId);
    CompletableFuture<Void> delete(String matchId);
    void observeSessionData(String matchId, OnSessionUpdateListener<SpojniceSessionData> listener);
}
