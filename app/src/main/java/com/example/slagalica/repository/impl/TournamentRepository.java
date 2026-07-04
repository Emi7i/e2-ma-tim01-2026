package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.tournament.TournamentMatch;
import com.example.slagalica.domain.model.tournament.TournamentRound;
import com.example.slagalica.domain.model.tournament.TournamentSession;

import java.util.concurrent.CompletableFuture;

public interface TournamentRepository {

    CompletableFuture<TournamentSession> joinTournamentQueue(
            UserProfile currentUser
    );

    CompletableFuture<TournamentSession> createDemoTournament(
            UserProfile currentUser
    );

    CompletableFuture<TournamentSession> getActiveTournamentForUser(
            String userId
    );

    CompletableFuture<TournamentMatch> getNextPlayableMatchForUser(
            String tournamentId,
            String userId
    );

    CompletableFuture<Void> recordTournamentMatchResult(
            String tournamentId,
            String tournamentMatchId,
            TournamentRound round,
            String player1Id,
            String player2Id,
            int player1Score,
            int player2Score
    );

    CompletableFuture<Void> cancelWaitingQueue(
            String userId
    );
}
