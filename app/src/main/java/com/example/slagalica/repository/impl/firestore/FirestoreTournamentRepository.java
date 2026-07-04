package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.tournament.TournamentMatch;
import com.example.slagalica.domain.model.tournament.TournamentMatchStatus;
import com.example.slagalica.domain.model.tournament.TournamentParticipant;
import com.example.slagalica.domain.model.tournament.TournamentQueueEntry;
import com.example.slagalica.domain.model.tournament.TournamentRewardResult;
import com.example.slagalica.domain.model.tournament.TournamentRound;
import com.example.slagalica.domain.model.tournament.TournamentSession;
import com.example.slagalica.domain.model.tournament.TournamentStatus;
import com.example.slagalica.domain.service.tournament.TournamentRewardPolicy;
import com.example.slagalica.repository.impl.TournamentRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreTournamentRepository implements TournamentRepository {

    private static final String COLLECTION_PROFILES = "profiles";
    private static final String COLLECTION_QUEUE = "tournamentQueue";
    private static final String COLLECTION_TOURNAMENTS = "tournaments";
    private static final String SUBCOLLECTION_PARTICIPANTS = "participants";
    private static final String SUBCOLLECTION_MATCHES = "matches";

    private static final int TOURNAMENT_PRICE = 3;
    private static final int REQUIRED_PLAYERS = 4;

    private final FirebaseFirestore db;

    @Inject
    public FirestoreTournamentRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<TournamentSession> joinTournamentQueue(
            UserProfile currentUser
    ) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return failedFuture(new IllegalArgumentException("Korisnik nije prijavljen."));
        }

        if (currentUser.getNumTokens() < TOURNAMENT_PRICE) {
            return failedFuture(new IllegalStateException("Za turnir su potrebna 3 tokena."));
        }

        long now = System.currentTimeMillis();
        DocumentReference profileRef = db
                .collection(COLLECTION_PROFILES)
                .document(currentUser.getUserId());

        DocumentReference queueRef = db
                .collection(COLLECTION_QUEUE)
                .document(currentUser.getUserId());

        return toFuture(db.runTransaction(transaction -> {
            DocumentSnapshot profileSnapshot = transaction.get(profileRef);

            Long tokens = profileSnapshot.getLong("numTokens");
            long availableTokens = tokens == null ? currentUser.getNumTokens() : tokens;

            if (availableTokens < TOURNAMENT_PRICE) {
                throw new IllegalStateException("Za turnir su potrebna 3 tokena.");
            }

            DocumentSnapshot queueSnapshot = transaction.get(queueRef);
            if (!queueSnapshot.exists()) {
                transaction.update(
                        profileRef,
                        "numTokens",
                        FieldValue.increment(-TOURNAMENT_PRICE)
                );
                transaction.set(queueRef, queueEntryToMap(currentUser, now));
            }

            return null;
        })).thenCompose(ignored -> tryCreateTournamentFromQueue())
                .thenCompose(createdTournament -> {
                    if (createdTournament != null) {
                        return completed(createdTournament);
                    }
                    return getActiveTournamentForUser(currentUser.getUserId());
                });
    }

    @Override
    public CompletableFuture<TournamentSession> createDemoTournament(
            UserProfile currentUser
    ) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return failedFuture(new IllegalArgumentException("Korisnik nije prijavljen."));
        }

        if (currentUser.getNumTokens() < TOURNAMENT_PRICE) {
            return failedFuture(new IllegalStateException("Za turnir su potrebna 3 tokena."));
        }

        long now = System.currentTimeMillis();
        String tournamentId = "TOURNAMENT_DEMO_" + now;

        TournamentParticipant p1 = participantFromProfile(currentUser, 1);
        TournamentParticipant p2 = demoParticipant("demo_tournament_1", "Ana", "Praktikant", 2);
        TournamentParticipant p3 = demoParticipant("demo_tournament_2", "Marko", "Inženjer", 3);
        TournamentParticipant p4 = demoParticipant("demo_tournament_3", "Mina", "Diplomirani Inženjer", 4);

        List<TournamentParticipant> participants = Arrays.asList(p1, p2, p3, p4);
        List<String> playerIds = Arrays.asList(
                p1.getUserId(),
                p2.getUserId(),
                p3.getUserId(),
                p4.getUserId()
        );

        DocumentReference tournamentRef = db
                .collection(COLLECTION_TOURNAMENTS)
                .document(tournamentId);

        DocumentReference profileRef = db
                .collection(COLLECTION_PROFILES)
                .document(currentUser.getUserId());

        return toFuture(db.runTransaction(transaction -> {
            DocumentSnapshot profileSnapshot = transaction.get(profileRef);
            Long tokens = profileSnapshot.getLong("numTokens");
            long availableTokens = tokens == null ? currentUser.getNumTokens() : tokens;

            if (availableTokens < TOURNAMENT_PRICE) {
                throw new IllegalStateException("Za turnir su potrebna 3 tokena.");
            }

            transaction.update(
                    profileRef,
                    "numTokens",
                    FieldValue.increment(-TOURNAMENT_PRICE)
            );

            transaction.set(
                    tournamentRef,
                    tournamentToMap(
                            tournamentId,
                            TournamentStatus.SEMIFINALS_READY,
                            playerIds,
                            now,
                            now
                    )
            );

            for (TournamentParticipant participant : participants) {
                transaction.set(
                        tournamentRef
                                .collection(SUBCOLLECTION_PARTICIPANTS)
                                .document(participant.getUserId()),
                        participantToMap(participant)
                );
            }

            TournamentMatch semi1 = new TournamentMatch(
                    tournamentId + "_SF_1",
                    tournamentId,
                    TournamentRound.SEMIFINAL,
                    1,
                    p1.getUserId(),
                    p2.getUserId(),
                    p1.getUsername(),
                    p2.getUsername(),
                    TournamentMatchStatus.WAITING,
                    now
            );

            TournamentMatch semi2 = new TournamentMatch(
                    tournamentId + "_SF_2",
                    tournamentId,
                    TournamentRound.SEMIFINAL,
                    2,
                    p3.getUserId(),
                    p4.getUserId(),
                    p3.getUsername(),
                    p4.getUsername(),
                    TournamentMatchStatus.WAITING,
                    now
            );

            transaction.set(
                    tournamentRef.collection(SUBCOLLECTION_MATCHES).document(semi1.getMatchId()),
                    matchToMap(semi1)
            );
            transaction.set(
                    tournamentRef.collection(SUBCOLLECTION_MATCHES).document(semi2.getMatchId()),
                    matchToMap(semi2)
            );

            return null;
        })).thenCompose(ignored -> getTournamentById(tournamentId));
    }

    @Override
    public CompletableFuture<TournamentSession> getActiveTournamentForUser(
            String userId
    ) {
        CompletableFuture<TournamentSession> future = new CompletableFuture<>();

        if (userId == null) {
            future.complete(null);
            return future;
        }

        db.collection(COLLECTION_TOURNAMENTS)
                .whereArrayContains("playerIds", userId)
                .orderBy("updatedAtMillis", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    TournamentSession active = null;

                    for (QueryDocumentSnapshot document : snapshot) {
                        TournamentSession session = document.toObject(TournamentSession.class);
                        session.setTournamentId(document.getId());

                        if (session.getStatusEnum() != TournamentStatus.FINISHED
                                && session.getStatusEnum() != TournamentStatus.CANCELLED) {
                            active = session;
                            break;
                        }
                    }

                    if (active == null) {
                        future.complete(null);
                        return;
                    }

                    getTournamentById(active.getTournamentId())
                            .thenAccept(future::complete)
                            .exceptionally(e -> {
                                future.completeExceptionally(e);
                                return null;
                            });
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<TournamentMatch> getNextPlayableMatchForUser(
            String tournamentId,
            String userId
    ) {
        CompletableFuture<TournamentMatch> future = new CompletableFuture<>();

        db.collection(COLLECTION_TOURNAMENTS)
                .document(tournamentId)
                .collection(SUBCOLLECTION_MATCHES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    TournamentMatch selected = null;

                    for (QueryDocumentSnapshot document : snapshot) {
                        TournamentMatch match = document.toObject(TournamentMatch.class);
                        match.setMatchId(document.getId());

                        boolean containsUser = userId.equals(match.getPlayer1Id())
                                || userId.equals(match.getPlayer2Id());

                        boolean playable = match.getStatusEnum() == TournamentMatchStatus.WAITING
                                || match.getStatusEnum() == TournamentMatchStatus.IN_PROGRESS;

                        if (containsUser && playable) {
                            if (selected == null || match.getMatchIndex() < selected.getMatchIndex()) {
                                selected = match;
                            }
                        }
                    }

                    future.complete(selected);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> recordTournamentMatchResult(
            String tournamentId,
            String tournamentMatchId,
            TournamentRound round,
            String player1Id,
            String player2Id,
            int player1Score,
            int player2Score
    ) {
        String winnerId = player1Score >= player2Score ? player1Id : player2Id;
        String loserId = player1Score >= player2Score ? player2Id : player1Id;
        int winnerScore = player1Score >= player2Score ? player1Score : player2Score;
        int loserScore = player1Score >= player2Score ? player2Score : player1Score;
        long now = System.currentTimeMillis();

        DocumentReference tournamentRef = db
                .collection(COLLECTION_TOURNAMENTS)
                .document(tournamentId);

        DocumentReference tournamentMatchRef = tournamentRef
                .collection(SUBCOLLECTION_MATCHES)
                .document(tournamentMatchId);

        DocumentReference winnerProfileRef = db
                .collection(COLLECTION_PROFILES)
                .document(winnerId);

        DocumentReference loserProfileRef = db
                .collection(COLLECTION_PROFILES)
                .document(loserId);

        return toFuture(db.runTransaction(transaction -> {
            DocumentSnapshot matchSnapshot = transaction.get(tournamentMatchRef);
            DocumentSnapshot winnerSnapshot = transaction.get(winnerProfileRef);
            DocumentSnapshot loserSnapshot = transaction.get(loserProfileRef);

            if (!matchSnapshot.exists()) {
                throw new IllegalStateException("Turnirska partija ne postoji.");
            }

            String status = matchSnapshot.getString("status");
            if (TournamentMatchStatus.FINISHED.name().equals(status)) {
                return null;
            }

            long winnerOldStars = getLong(winnerSnapshot, "numStars", 0L);
            long loserOldStars = getLong(loserSnapshot, "numStars", 0L);

            TournamentRewardResult reward = TournamentRewardPolicy.calculate(
                    round,
                    winnerScore,
                    winnerOldStars,
                    loserScore,
                    loserOldStars
            );

            transaction.update(
                    winnerProfileRef,
                    "numStars",
                    FieldValue.increment(reward.getWinnerStarDelta()),
                    "numTokens",
                    FieldValue.increment(reward.getWinnerTokenReward())
            );

            if (reward.getLoserStarDelta() != 0L || reward.getLoserTokenReward() != 0) {
                transaction.update(
                        loserProfileRef,
                        "numStars",
                        FieldValue.increment(reward.getLoserStarDelta()),
                        "numTokens",
                        FieldValue.increment(reward.getLoserTokenReward())
                );
            }

            Map<String, Object> matchUpdate = new HashMap<>();
            matchUpdate.put("status", TournamentMatchStatus.FINISHED.name());
            matchUpdate.put("winnerId", winnerId);
            matchUpdate.put("loserId", loserId);
            matchUpdate.put("player1Score", player1Score);
            matchUpdate.put("player2Score", player2Score);
            matchUpdate.put("finishedAtMillis", now);
            transaction.update(tournamentMatchRef, matchUpdate);

            transaction.set(
                    tournamentRef
                            .collection(SUBCOLLECTION_PARTICIPANTS)
                            .document(loserId),
                    mapOf("eliminated", true),
                    SetOptions.merge()
            );

            if (round == TournamentRound.FINAL) {
                transaction.set(
                        tournamentRef
                                .collection(SUBCOLLECTION_PARTICIPANTS)
                                .document(winnerId),
                        mapOf("winner", true),
                        SetOptions.merge()
                );

                Map<String, Object> tournamentUpdate = new HashMap<>();
                tournamentUpdate.put("status", TournamentStatus.FINISHED.name());
                tournamentUpdate.put("winnerId", winnerId);
                tournamentUpdate.put("updatedAtMillis", now);
                transaction.update(tournamentRef, tournamentUpdate);
            }

            return null;
        })).thenCompose(ignored -> maybeCreateFinalMatch(tournamentId));
    }

    @Override
    public CompletableFuture<Void> cancelWaitingQueue(String userId) {
        if (userId == null) {
            return completed(null);
        }

        return toFuture(
                db.collection(COLLECTION_QUEUE)
                        .document(userId)
                        .delete()
        );
    }

    private CompletableFuture<TournamentSession> tryCreateTournamentFromQueue() {
        CompletableFuture<TournamentSession> future = new CompletableFuture<>();

        db.collection(COLLECTION_QUEUE)
                .orderBy("createdAtMillis", Query.Direction.ASCENDING)
                .limit(REQUIRED_PLAYERS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.size() < REQUIRED_PLAYERS) {
                        future.complete(null);
                        return;
                    }

                    List<TournamentQueueEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        TournamentQueueEntry entry = document.toObject(TournamentQueueEntry.class);
                        entry.setUserId(document.getId());
                        entries.add(entry);
                    }

                    createTournamentFromEntries(entries)
                            .thenAccept(future::complete)
                            .exceptionally(e -> {
                                future.completeExceptionally(e);
                                return null;
                            });
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private CompletableFuture<TournamentSession> createTournamentFromEntries(
            List<TournamentQueueEntry> entries
    ) {
        long now = System.currentTimeMillis();
        String tournamentId = "TOURNAMENT_" + now + "_" + UUID.randomUUID().toString().substring(0, 8);
        DocumentReference tournamentRef = db.collection(COLLECTION_TOURNAMENTS).document(tournamentId);

        List<String> playerIds = new ArrayList<>();
        List<TournamentParticipant> participants = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            TournamentQueueEntry entry = entries.get(i);
            playerIds.add(entry.getUserId());
            participants.add(new TournamentParticipant(
                    entry.getUserId(),
                    entry.getUsername(),
                    entry.getEmail(),
                    entry.getAvatarUrl(),
                    entry.getLeague(),
                    entry.getNumStars(),
                    i + 1,
                    false,
                    false
            ));
        }

        return toFuture(db.runTransaction(transaction -> {
            for (TournamentQueueEntry entry : entries) {
                transaction.delete(db.collection(COLLECTION_QUEUE).document(entry.getUserId()));
            }

            transaction.set(
                    tournamentRef,
                    tournamentToMap(
                            tournamentId,
                            TournamentStatus.SEMIFINALS_READY,
                            playerIds,
                            now,
                            now
                    )
            );

            for (TournamentParticipant participant : participants) {
                transaction.set(
                        tournamentRef.collection(SUBCOLLECTION_PARTICIPANTS).document(participant.getUserId()),
                        participantToMap(participant)
                );
            }

            TournamentMatch semi1 = new TournamentMatch(
                    tournamentId + "_SF_1",
                    tournamentId,
                    TournamentRound.SEMIFINAL,
                    1,
                    participants.get(0).getUserId(),
                    participants.get(1).getUserId(),
                    participants.get(0).getUsername(),
                    participants.get(1).getUsername(),
                    TournamentMatchStatus.WAITING,
                    now
            );

            TournamentMatch semi2 = new TournamentMatch(
                    tournamentId + "_SF_2",
                    tournamentId,
                    TournamentRound.SEMIFINAL,
                    2,
                    participants.get(2).getUserId(),
                    participants.get(3).getUserId(),
                    participants.get(2).getUsername(),
                    participants.get(3).getUsername(),
                    TournamentMatchStatus.WAITING,
                    now
            );

            transaction.set(tournamentRef.collection(SUBCOLLECTION_MATCHES).document(semi1.getMatchId()), matchToMap(semi1));
            transaction.set(tournamentRef.collection(SUBCOLLECTION_MATCHES).document(semi2.getMatchId()), matchToMap(semi2));

            return null;
        })).thenCompose(ignored -> getTournamentById(tournamentId));
    }

    private CompletableFuture<Void> maybeCreateFinalMatch(String tournamentId) {
        return getTournamentById(tournamentId).thenCompose(session -> {
            if (session == null || session.getStatusEnum() == TournamentStatus.FINISHED) {
                return completed(null);
            }

            List<TournamentMatch> semifinals = new ArrayList<>();
            for (TournamentMatch match : session.getMatches()) {
                if (match.getRoundEnum() == TournamentRound.SEMIFINAL) {
                    semifinals.add(match);
                }
            }

            if (semifinals.size() < 2) {
                return completed(null);
            }

            TournamentMatch first = semifinals.get(0);
            TournamentMatch second = semifinals.get(1);

            if (first.getStatusEnum() != TournamentMatchStatus.FINISHED
                    || second.getStatusEnum() != TournamentMatchStatus.FINISHED) {
                return completed(null);
            }

            for (TournamentMatch match : session.getMatches()) {
                if (match.getRoundEnum() == TournamentRound.FINAL) {
                    return completed(null);
                }
            }

            long now = System.currentTimeMillis();
            String finalMatchId = tournamentId + "_FINAL";
            String player1Id = first.getWinnerId();
            String player2Id = second.getWinnerId();
            String player1Username = usernameFor(session, player1Id);
            String player2Username = usernameFor(session, player2Id);

            TournamentMatch finalMatch = new TournamentMatch(
                    finalMatchId,
                    tournamentId,
                    TournamentRound.FINAL,
                    3,
                    player1Id,
                    player2Id,
                    player1Username,
                    player2Username,
                    TournamentMatchStatus.WAITING,
                    now
            );

            DocumentReference tournamentRef = db.collection(COLLECTION_TOURNAMENTS).document(tournamentId);
            return toFuture(db.runTransaction(transaction -> {
                transaction.set(
                        tournamentRef.collection(SUBCOLLECTION_MATCHES).document(finalMatchId),
                        matchToMap(finalMatch)
                );
                transaction.update(
                        tournamentRef,
                        "status",
                        TournamentStatus.FINAL_READY.name(),
                        "updatedAtMillis",
                        now
                );
                return null;
            }));
        });
    }

    private CompletableFuture<TournamentSession> getTournamentById(String tournamentId) {
        CompletableFuture<TournamentSession> future = new CompletableFuture<>();
        DocumentReference tournamentRef = db.collection(COLLECTION_TOURNAMENTS).document(tournamentId);

        tournamentRef.get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        future.complete(null);
                        return;
                    }

                    TournamentSession session = document.toObject(TournamentSession.class);
                    if (session == null) {
                        session = new TournamentSession();
                    }
                    session.setTournamentId(document.getId());

                    TournamentSession finalSession = session;
                    tournamentRef.collection(SUBCOLLECTION_PARTICIPANTS).get()
                            .addOnSuccessListener(participantsSnapshot -> {
                                List<TournamentParticipant> participants = new ArrayList<>();
                                for (QueryDocumentSnapshot participantDoc : participantsSnapshot) {
                                    TournamentParticipant participant = participantDoc.toObject(TournamentParticipant.class);
                                    participant.setUserId(participantDoc.getId());
                                    participants.add(participant);
                                }
                                finalSession.setParticipants(participants);

                                tournamentRef.collection(SUBCOLLECTION_MATCHES).get()
                                        .addOnSuccessListener(matchesSnapshot -> {
                                            List<TournamentMatch> matches = new ArrayList<>();
                                            for (QueryDocumentSnapshot matchDoc : matchesSnapshot) {
                                                TournamentMatch match = matchDoc.toObject(TournamentMatch.class);
                                                match.setMatchId(matchDoc.getId());
                                                matches.add(match);
                                            }
                                            finalSession.setMatches(matches);
                                            future.complete(finalSession);
                                        })
                                        .addOnFailureListener(future::completeExceptionally);
                            })
                            .addOnFailureListener(future::completeExceptionally);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private Map<String, Object> tournamentToMap(
            String tournamentId,
            TournamentStatus status,
            List<String> playerIds,
            long createdAtMillis,
            long updatedAtMillis
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("tournamentId", tournamentId);
        data.put("status", status.name());
        data.put("playerIds", playerIds);
        data.put("createdAtMillis", createdAtMillis);
        data.put("updatedAtMillis", updatedAtMillis);
        data.put("winnerId", null);
        return data;
    }

    private Map<String, Object> queueEntryToMap(UserProfile profile, long now) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", profile.getUserId());
        data.put("username", profile.getUsername());
        data.put("email", profile.getEmail());
        data.put("avatarUrl", profile.getAvatarUrl());
        data.put("league", profile.getLeague());
        data.put("numStars", profile.getNumStars());
        data.put("createdAtMillis", now);
        return data;
    }

    private TournamentParticipant participantFromProfile(UserProfile profile, int seed) {
        return new TournamentParticipant(
                profile.getUserId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getAvatarUrl(),
                profile.getLeague(),
                profile.getNumStars(),
                seed,
                false,
                false
        );
    }

    private TournamentParticipant demoParticipant(
            String userId,
            String username,
            String league,
            int seed
    ) {
        return new TournamentParticipant(
                userId,
                username,
                username.toLowerCase() + "@demo.local",
                null,
                league,
                0L,
                seed,
                false,
                false
        );
    }

    private Map<String, Object> participantToMap(TournamentParticipant participant) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", participant.getUserId());
        data.put("username", participant.getUsername());
        data.put("email", participant.getEmail());
        data.put("avatarUrl", participant.getAvatarUrl());
        data.put("league", participant.getLeague());
        data.put("numStars", participant.getNumStars());
        data.put("seed", participant.getSeed());
        data.put("eliminated", participant.isEliminated());
        data.put("winner", participant.isWinner());
        return data;
    }

    private Map<String, Object> matchToMap(TournamentMatch match) {
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", match.getMatchId());
        data.put("tournamentId", match.getTournamentId());
        data.put("round", match.getRound());
        data.put("matchIndex", match.getMatchIndex());
        data.put("player1Id", match.getPlayer1Id());
        data.put("player2Id", match.getPlayer2Id());
        data.put("player1Username", match.getPlayer1Username());
        data.put("player2Username", match.getPlayer2Username());
        data.put("winnerId", match.getWinnerId());
        data.put("loserId", match.getLoserId());
        data.put("player1Score", match.getPlayer1Score());
        data.put("player2Score", match.getPlayer2Score());
        data.put("status", match.getStatus());
        data.put("createdAtMillis", match.getCreatedAtMillis());
        data.put("startedAtMillis", match.getStartedAtMillis());
        data.put("finishedAtMillis", match.getFinishedAtMillis());
        return data;
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private String usernameFor(TournamentSession session, String userId) {
        for (TournamentParticipant participant : session.getParticipants()) {
            if (participant.getUserId().equals(userId)) {
                return participant.getUsername();
            }
        }
        return "Igrač";
    }

    private long getLong(DocumentSnapshot snapshot, String field, long defaultValue) {
        Long value = snapshot.getLong(field);
        return value == null ? defaultValue : value;
    }

    private static <T> CompletableFuture<T> completed(T value) {
        return CompletableFuture.completedFuture(value);
    }

    private static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    private static <T> CompletableFuture<T> toFuture(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        task.addOnSuccessListener(future::complete);
        task.addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
