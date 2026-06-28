package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;
import com.example.slagalica.domain.model.match.games.korakpokorak.TermWithHints;
import com.example.slagalica.repository.impl.KorakPoKorakRepository;
import com.example.slagalica.repository.impl.TermRepository;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KorakPoKorakService {

    private final TermRepository termRepository;
    private final KorakPoKorakRepository sessionRepository;

    @Inject
    public KorakPoKorakService(TermRepository termRepository, KorakPoKorakRepository sessionRepository) {
        this.termRepository = termRepository;
        this.sessionRepository = sessionRepository;
    }

    public CompletableFuture<TermWithHints> getRandomTermWithHints() {
        return termRepository.getRandomTermWithHints();
    }

    public CompletableFuture<Void> updateSessionData(String matchId, KorakPoKorakSessionData data) {
        return sessionRepository.updateSessionData(matchId, data);
    }

    public CompletableFuture<KorakPoKorakSessionData> getSessionData(String matchId) {
        return sessionRepository.getSessionData(matchId);
    }
    public void observeSessionData(String matchId, OnSessionUpdateListener<KorakPoKorakSessionData> listener) {
        sessionRepository.observeSessionData(matchId, listener);
    }
}
