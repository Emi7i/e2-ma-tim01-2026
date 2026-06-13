package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.Spojnice;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SpojniceRepository {
    CompletableFuture<List<Spojnice>> getAllSpojnice();
    CompletableFuture<Spojnice> getRandomSpojnice();
}
