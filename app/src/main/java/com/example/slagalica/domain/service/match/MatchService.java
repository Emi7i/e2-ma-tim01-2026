package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.MatchSessionData;
import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;
import com.example.slagalica.repository.impl.MatchRepository;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MatchService {
    private final MatchRepository matchRepository;

    @Inject
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public CompletableFuture<String> create(MatchSessionData data) {
        return matchRepository.create(data);
    }

    public CompletableFuture<Void> update(String matchId, MatchSessionData data) {
        return matchRepository.update(matchId, data);
    }

    public CompletableFuture<MatchSessionData> get(String matchId) {
        return matchRepository.get(matchId);
    }

    public void observe(String matchId, OnSessionUpdateListener<MatchSessionData> listener) {
        matchRepository.observe(matchId, listener);
    }
}