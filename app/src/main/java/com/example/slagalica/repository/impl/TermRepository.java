package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.korakpokorak.TermWithHints;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TermRepository {
    CompletableFuture<List<TermWithHints>> getAllTerms();
    CompletableFuture<TermWithHints> getRandomTermWithHints();
    CompletableFuture<Void> saveTerm(TermWithHints term);
}
