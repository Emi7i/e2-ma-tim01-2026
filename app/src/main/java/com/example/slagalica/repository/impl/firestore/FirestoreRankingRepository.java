package com.example.slagalica.repository.impl.firestore;

import android.util.Log;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.League;
import com.example.slagalica.domain.model.ranking.RankingCycle;
import com.example.slagalica.domain.model.ranking.RankingCycleType;
import com.example.slagalica.domain.model.ranking.RankingEntry;
import com.example.slagalica.domain.model.ranking.RankingReward;
import com.example.slagalica.domain.service.progression.LeagueNotificationService;
import com.example.slagalica.domain.service.progression.LeagueService;
import com.example.slagalica.domain.service.ranking.RankingCycleUtils;
import com.example.slagalica.domain.service.ranking.RankingRewardPolicy;
import com.example.slagalica.repository.impl.RankingRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreRankingRepository implements RankingRepository {

    private static final String COLLECTION_CYCLES = "rankingCycles";
    private static final String SUBCOLLECTION_ENTRIES = "entries";
    private static final String COLLECTION_PROCESSED_MATCHES =
            "rankingProcessedMatches";
    private static final String COLLECTION_REWARDS = "rankingRewards";
    private static final String COLLECTION_PROFILES = "profiles";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    private static final String TAG = "RANK";

    private final FirebaseFirestore db;
    private final LeagueService leagueService;
    private final LeagueNotificationService leagueNotificationService;
    private final SessionManager sessionManager;

    @Inject
    public FirestoreRankingRepository(
            FirebaseFirestore db,
            LeagueService leagueService,
            LeagueNotificationService leagueNotificationService,
            SessionManager sessionManager
    ) {
        this.db = db;
        this.leagueService = leagueService;
        this.leagueNotificationService = leagueNotificationService;
        this.sessionManager = sessionManager;
    }

    @Override
    public CompletableFuture<Void> recordClassicMatch(
            String matchId,
            UserProfile player1,
            long player1StarDelta,
            UserProfile player2,
            long player2StarDelta,
            long finishedAtMillis
    ) {
        if (matchId == null || matchId.trim().isEmpty()) {
            return failedFuture(
                    new IllegalArgumentException("matchId je obavezan.")
            );
        }

        RankingCycle weeklyCycle = RankingCycleUtils.currentCycle(
                RankingCycleType.WEEKLY,
                finishedAtMillis
        );
        RankingCycle monthlyCycle = RankingCycleUtils.currentCycle(
                RankingCycleType.MONTHLY,
                finishedAtMillis
        );

        DocumentReference processedMatchRef = db
                .collection(COLLECTION_PROCESSED_MATCHES)
                .document(matchId);

        DocumentReference weeklyCycleRef = db
                .collection(COLLECTION_CYCLES)
                .document(weeklyCycle.getCycleId());

        DocumentReference monthlyCycleRef = db
                .collection(COLLECTION_CYCLES)
                .document(monthlyCycle.getCycleId());

        Task<Void> task = db.runTransaction(transaction -> {
            DocumentSnapshot processedSnapshot =
                    transaction.get(processedMatchRef);

            if (processedSnapshot.exists()) {
                return null;
            }

            // Sva čitanja moraju da budu pre prvog upisa u transakciji.
            DocumentSnapshot weeklySnapshot =
                    transaction.get(weeklyCycleRef);
            DocumentSnapshot monthlySnapshot =
                    transaction.get(monthlyCycleRef);

            if (!weeklySnapshot.exists()) {
                transaction.set(
                        weeklyCycleRef,
                        cycleToMap(weeklyCycle)
                );
            }

            if (!monthlySnapshot.exists()) {
                transaction.set(
                        monthlyCycleRef,
                        cycleToMap(monthlyCycle)
                );
            }

            upsertEntry(
                    transaction,
                    weeklyCycleRef,
                    player1,
                    player1StarDelta,
                    finishedAtMillis
            );
            upsertEntry(
                    transaction,
                    weeklyCycleRef,
                    player2,
                    player2StarDelta,
                    finishedAtMillis
            );
            upsertEntry(
                    transaction,
                    monthlyCycleRef,
                    player1,
                    player1StarDelta,
                    finishedAtMillis
            );
            upsertEntry(
                    transaction,
                    monthlyCycleRef,
                    player2,
                    player2StarDelta,
                    finishedAtMillis
            );

            Map<String, Object> processedData = new HashMap<>();
            processedData.put("matchId", matchId);
            processedData.put("player1Id", player1.getUserId());
            processedData.put("player2Id", player2.getUserId());
            processedData.put("processedAtMillis", finishedAtMillis);
            transaction.set(processedMatchRef, processedData);

            return null;
        });

        return toFuture(task);
    }

    @Override
    public CompletableFuture<List<RankingEntry>> getLeaderboard(
            String cycleId
    ) {
        CompletableFuture<List<RankingEntry>> future =
                new CompletableFuture<>();

        db.collection(COLLECTION_CYCLES)
                .document(cycleId)
                .collection(SUBCOLLECTION_ENTRIES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<RankingEntry> entries = new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshot) {
                        RankingEntry entry =
                                document.toObject(RankingEntry.class);

                        if (entry.getUserId() == null) {
                            entry.setUserId(document.getId());
                        }

                        // Dokument postoji samo kada je odigrana barem jedna
                        // partija, ali zadržavamo i eksplicitnu proveru.
                        if (entry.getGamesPlayed() > 0) {
                            entries.add(entry);
                        }
                    }

                    entries.sort((first, second) -> {
                        int starComparison = Long.compare(
                                second.getStarsEarned(),
                                first.getStarsEarned()
                        );

                        if (starComparison != 0) {
                            return starComparison;
                        }

                        return safe(first.getUsername())
                                .compareToIgnoreCase(
                                        safe(second.getUsername())
                                );
                    });

                    future.complete(entries);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<List<RankingCycle>>
    getUndistributedExpiredCycles(long nowMillis) {
        CompletableFuture<List<RankingCycle>> future =
                new CompletableFuture<>();

        db.collection(COLLECTION_CYCLES)
                .whereEqualTo("rewardsDistributed", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<RankingCycle> cycles = new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshot) {
                        RankingCycle cycle =
                                document.toObject(RankingCycle.class);

                        if (cycle.getCycleId() == null) {
                            cycle.setCycleId(document.getId());
                        }

                        if (cycle.getEndMillis() <= nowMillis) {
                            cycles.add(cycle);
                        }
                    }

                    cycles.sort(
                            Comparator.comparingLong(
                                    RankingCycle::getEndMillis
                            )
                    );

                    future.complete(cycles);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> finalizeExpiredCycles(long nowMillis) {
        return getUndistributedExpiredCycles(nowMillis)
                .thenCompose(cycles -> {
                    Log.i(TAG, "finalizeExpiredCycles: found " + cycles.size() + " expired undistributed cycle(s)");
                    for (RankingCycle c : cycles) {
                        Log.i(TAG, "  -> " + c.getCycleId() + " (type=" + c.getType() + ", rewardsDistributed=" + c.isRewardsDistributed() + ")");
                    }

                    CompletableFuture<Void> chain =
                            CompletableFuture.completedFuture(null);

                    for (RankingCycle cycle : cycles) {
                        chain = chain.thenCompose(ignored ->
                                getLeaderboard(cycle.getCycleId())
                                        .thenCompose(entries -> {
                                            Log.i(TAG, "  leaderboard for " + cycle.getCycleId() + ": " + entries.size() + " entries");
                                            return distributeCycleRewards(cycle, entries);
                                        })
                        );
                    }

                    return chain;
                });
    }

    @Override
    public CompletableFuture<RankingReward>
    getFirstUnseenRewardForUser(String userId) {
        CompletableFuture<RankingReward> future =
                new CompletableFuture<>();

        db.collection(COLLECTION_REWARDS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<RankingReward> unseen = new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshot) {
                        RankingReward reward =
                                document.toObject(RankingReward.class);

                        if (reward.getRewardId() == null) {
                            reward.setRewardId(document.getId());
                        }

                        if (!reward.isSeen()) {
                            unseen.add(reward);
                        }
                    }

                    unseen.sort(
                            Comparator.comparingLong(
                                    RankingReward::getCreatedAtMillis
                            )
                    );

                    future.complete(
                            unseen.isEmpty() ? null : unseen.get(0)
                    );
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> markRewardSeen(String rewardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_REWARDS)
                .document(rewardId)
                .update("seen", true)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private CompletableFuture<Void> distributeCycleRewards(
            RankingCycle cycle,
            List<RankingEntry> sortedEntries
    ) {
        DocumentReference cycleRef = db
                .collection(COLLECTION_CYCLES)
                .document(cycle.getCycleId());

        long rewardedAtMillis = System.currentTimeMillis();

        Task<Void> task = db.runTransaction(transaction -> {
            DocumentSnapshot cycleSnapshot = transaction.get(cycleRef);

            if (!cycleSnapshot.exists()) {
                return null;
            }

            Boolean alreadyDistributed =
                    cycleSnapshot.getBoolean("rewardsDistributed");

            if (Boolean.TRUE.equals(alreadyDistributed)) {
                return null;
            }

            int rewardedPlayers = Math.min(10, sortedEntries.size());

            for (int index = 0; index < rewardedPlayers; index++) {
                RankingEntry entry = sortedEntries.get(index);
                int placement = index + 1;
                int tokenReward =
                        RankingRewardPolicy.tokensForPlacement(
                                cycle.getCycleType(),
                                placement
                        );

                if (tokenReward <= 0) {
                    continue;
                }

                String rewardId =
                        cycle.getCycleId() + "_" + entry.getUserId();
                String notificationId =
                        "ranking_reward_" + rewardId;

                DocumentReference profileRef = db
                        .collection(COLLECTION_PROFILES)
                        .document(entry.getUserId());

                DocumentReference rewardRef = db
                        .collection(COLLECTION_REWARDS)
                        .document(rewardId);

                DocumentReference notificationRef = db
                        .collection(COLLECTION_NOTIFICATIONS)
                        .document(notificationId);

                transaction.update(
                        profileRef,
                        "numTokens",
                        FieldValue.increment(tokenReward)
                );

                transaction.set(
                        rewardRef,
                        rewardToMap(
                                rewardId,
                                entry.getUserId(),
                                cycle,
                                placement,
                                tokenReward,
                                rewardedAtMillis
                        )
                );

                transaction.set(
                        notificationRef,
                        notificationToMap(
                                notificationId,
                                entry.getUserId(),
                                cycle,
                                placement,
                                tokenReward,
                                rewardedAtMillis
                        )
                );
            }

            Map<String, Object> cycleUpdate = new HashMap<>();
            cycleUpdate.put("rewardsDistributed", true);
            cycleUpdate.put("rewardedAtMillis", rewardedAtMillis);
            transaction.update(cycleRef, cycleUpdate);

            return null;
        });

        CompletableFuture<Void> base = toFuture(task);
        if (cycle.getCycleType() == RankingCycleType.MONTHLY) {
            return base.thenCompose(ignored ->
                    applyMonthlyNonPlacementPenalties(cycle, sortedEntries));
        }
        return base;
    }

    private CompletableFuture<Void> applyMonthlyNonPlacementPenalties(
            RankingCycle cycle,
            List<RankingEntry> sortedEntries
    ) {
        List<RankingEntry> nonPlaced = sortedEntries.size() > 3
                ? sortedEntries.subList(3, sortedEntries.size())
                : new ArrayList<>();

        Log.i(TAG, "applyMonthlyNonPlacementPenalties: " + sortedEntries.size()
                + " total entries, " + nonPlaced.size() + " will be penalized");

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (int i = 0; i < nonPlaced.size(); i++) {
            RankingEntry entry = nonPlaced.get(i);
            int rank = i + 4; // positions 4, 5, 6, ...
            chain = chain.thenCompose(ignored ->
                    applyPenaltyToUser(cycle.getCycleId(), entry, rank));
        }
        return chain;
    }

    private CompletableFuture<Void> applyPenaltyToUser(
            String cycleId,
            RankingEntry entry,
            int rank
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_PROFILES)
                .document(entry.getUserId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        future.complete(null);
                        return;
                    }

                    UserProfile profile = snapshot.toObject(UserProfile.class);
                    if (profile == null) {
                        future.complete(null);
                        return;
                    }

                    long starsBefore = profile.getNumStars();
                    String leagueBefore = profile.getLeague();
                    leagueService.applyNonPlacementPenalty(profile);
                    long starsAfter = profile.getNumStars();
                    String leagueAfter = profile.getLeague();
                    long lost = starsBefore - starsAfter;
                    boolean leagueDropped = !leagueBefore.equals(leagueAfter);

                    Log.i(TAG, entry.getUsername()
                            + ", rank " + rank
                            + ", " + starsBefore + " -> " + starsAfter
                            + ", " + leagueAfter);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("numStars", starsAfter);
                    updates.put("league", leagueAfter);

                    if (leagueDropped) {
                        League newLeague = League.fromStars(starsAfter);
                        String userId = entry.getUserId();
                        if (userId.equals(sessionManager.getCurrentUserId())) {
                            // Current user: LeagueNotificationService handles in-app
                            // banner vs system notification automatically.
                            leagueNotificationService.notifyChange(userId, newLeague, false);
                        } else {
                            // Other user on a different device: persist to Firestore so
                            // they see the notification in their inbox when they open the app.
                            String notifId = "league_demotion_" + cycleId + "_" + userId;
                            db.collection(COLLECTION_NOTIFICATIONS)
                                    .document(notifId)
                                    .set(leagueDemotionNotificationToMap(
                                            notifId, userId, newLeague, System.currentTimeMillis()));
                        }
                    }

                    db.collection(COLLECTION_PROFILES)
                            .document(entry.getUserId())
                            .update(updates)
                            .addOnSuccessListener(unused -> future.complete(null))
                            .addOnFailureListener(future::completeExceptionally);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private void upsertEntry(
            Transaction transaction,
            DocumentReference cycleRef,
            UserProfile profile,
            long starDelta,
            long updatedAtMillis
    ) {
        DocumentReference entryRef = cycleRef
                .collection(SUBCOLLECTION_ENTRIES)
                .document(profile.getUserId());

        Map<String, Object> entryData = new HashMap<>();
        entryData.put("userId", profile.getUserId());
        entryData.put("username", profile.getUsername());
        entryData.put("league", profile.getLeague());
        entryData.put(
                "starsEarned",
                FieldValue.increment(starDelta)
        );
        entryData.put(
                "gamesPlayed",
                FieldValue.increment(1L)
        );
        entryData.put("updatedAtMillis", updatedAtMillis);

        transaction.set(
                entryRef,
                entryData,
                SetOptions.merge()
        );
    }

    private Map<String, Object> cycleToMap(RankingCycle cycle) {
        Map<String, Object> data = new HashMap<>();
        data.put("cycleId", cycle.getCycleId());
        data.put("type", cycle.getType());
        data.put("startMillis", cycle.getStartMillis());
        data.put("endMillis", cycle.getEndMillis());
        data.put("rewardsDistributed", false);
        data.put("rewardedAtMillis", 0L);
        return data;
    }

    private Map<String, Object> rewardToMap(
            String rewardId,
            String userId,
            RankingCycle cycle,
            int placement,
            int tokenReward,
            long createdAtMillis
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("rewardId", rewardId);
        data.put("userId", userId);
        data.put("cycleId", cycle.getCycleId());
        data.put("cycleType", cycle.getCycleType().name());
        data.put("placement", placement);
        data.put("tokenReward", tokenReward);
        data.put("createdAtMillis", createdAtMillis);
        data.put("seen", false);
        return data;
    }

    private Map<String, Object> notificationToMap(
            String notificationId,
            String userId,
            RankingCycle cycle,
            int placement,
            int tokenReward,
            long timestampMillis
    ) {
        String cycleLabel =
                cycle.getCycleType() == RankingCycleType.WEEKLY
                        ? "nedeljnoj"
                        : "mesečnoj";

        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        data.put("userId", userId);
        data.put("type", "REWARD");
        data.put("title", "Nagrada sa rang liste");
        data.put(
                "message",
                "Osvojili ste " + placement + ". mesto na "
                        + cycleLabel + " rang listi i dobili "
                        + tokenReward + " tokena."
        );
        data.put("sender", "Sistem");
        data.put("timestampMillis", timestampMillis);
        data.put("read", false);
        data.put("hasOpenAction", true);
        data.put("hasDecisionAction", false);
        data.put("target", "REWARD");
        data.put("actionStatus", "NONE");
        return data;
    }

    private Map<String, Object> leagueDemotionNotificationToMap(
            String notificationId,
            String userId,
            League newLeague,
            long timestampMillis
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        data.put("userId", userId);
        data.put("type", "LEAGUE");
        data.put("title", "Pad u ligu");
        data.put("message", "Nažalost, pali ste u ligu: " + newLeague.getDisplayName() + ".");
        data.put("sender", "Sistem");
        data.put("timestampMillis", timestampMillis);
        data.put("read", false);
        data.put("hasOpenAction", true);
        data.put("hasDecisionAction", false);
        data.put("target", "LEAGUE");
        data.put("actionStatus", "NONE");
        return data;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static <T> CompletableFuture<T> toFuture(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        task.addOnSuccessListener(future::complete);
        task.addOnFailureListener(future::completeExceptionally);
        return future;
    }

    private static <T> CompletableFuture<T> failedFuture(
            Throwable throwable
    ) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }
}
