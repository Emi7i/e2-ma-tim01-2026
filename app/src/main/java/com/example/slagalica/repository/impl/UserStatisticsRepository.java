package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.progression.UserStatistics;
import java.util.concurrent.CompletableFuture;

public interface UserStatisticsRepository {
    CompletableFuture<UserStatistics> getStatistics(String userId);
    CompletableFuture<Void> saveStatistics(UserStatistics statistics);
}
