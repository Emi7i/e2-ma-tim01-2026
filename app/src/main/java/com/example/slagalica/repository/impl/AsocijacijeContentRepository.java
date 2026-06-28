package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsocijacijeContentRepository {
    CompletableFuture<List<AsocijacijaDocument>> getAllAsocijacije();
    CompletableFuture<Void> saveAsocijacija(AsocijacijaDocument asocijacijaDocument);
}