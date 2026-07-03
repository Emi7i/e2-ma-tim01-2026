package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.progression.RegionStatsDocument;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RegionStatsRepository {
    CompletableFuture<List<RegionStatsDocument>> getAllRegionStats();
    CompletableFuture<Void> incrementField(String regionKey, String field, long delta);
}
