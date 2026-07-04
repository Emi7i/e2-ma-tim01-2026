package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.MatchSessionData;
import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;

import java.util.concurrent.CompletableFuture;

public interface MatchRepository {
    CompletableFuture<String> create(MatchSessionData data);
    CompletableFuture<Void> update(String matchId, MatchSessionData data);
    CompletableFuture<MatchSessionData> get(String matchId);
    CompletableFuture<Void> delete(String matchId);
    void observe(String matchId, OnSessionUpdateListener<MatchSessionData> listener);
    CompletableFuture<Boolean> exists(String matchId);
}
