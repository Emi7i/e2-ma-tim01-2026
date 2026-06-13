package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.KoZnaZna;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface KoZnaZnaRepository {
    CompletableFuture<List<KoZnaZna>> getAllKoZnaZna();
    CompletableFuture<KoZnaZna> getRandomKoZnaZna();
}
