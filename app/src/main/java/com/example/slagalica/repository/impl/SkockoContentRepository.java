package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SkockoContentRepository {
    CompletableFuture<List<SkockoCombinationDocument>> getAllCombinations();
    CompletableFuture<Void> saveCombination(SkockoCombinationDocument document);
}