package com.example.slagalica.repository.impl.firestore;

import android.util.Log;

import com.example.slagalica.domain.model.match.MatchmakingEntry;
import com.example.slagalica.domain.model.match.games.common.OnMatchmakingUpdateListener;
import com.example.slagalica.repository.impl.MatchmakingEntryRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreMatchmakingEntryRepository implements MatchmakingEntryRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_QUEUE = "matchmakingQueue";

    @Inject
    public FirestoreMatchmakingEntryRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Boolean> exists(String userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        db.collection(COLLECTION_QUEUE).document(userId)
                .get()
                .addOnSuccessListener(snapshot -> future.complete(snapshot.exists()))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<String> add(String userId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        String matchId = java.util.UUID.randomUUID().toString();
        MatchmakingEntry entry = new MatchmakingEntry(userId);
        entry.setMatchId(matchId);
        db.collection(COLLECTION_QUEUE).document(userId)
                .set(entry)
                .addOnSuccessListener(unused -> future.complete(matchId))
                .addOnFailureListener(future::completeExceptionally);
        Log.d("Matchmaking", "Added new entry!");
        return future;
    }

    @Override
    public CompletableFuture<Void> delete(String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_QUEUE).document(userId)
                .delete()
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<MatchmakingEntry> getOldest(String excludeUserId) {
        CompletableFuture<MatchmakingEntry> future = new CompletableFuture<>();
        db.collection(COLLECTION_QUEUE)
                .orderBy("queuedAt", Query.Direction.ASCENDING)
                .limit(10) // small buffer in case the oldest entry is the excluded user
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    MatchmakingEntry result = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        MatchmakingEntry entry = doc.toObject(MatchmakingEntry.class);
                        if (entry != null && !entry.getUserId().equals(excludeUserId)) {
                            result = entry;
                            break;
                        }
                    }
                    future.complete(result);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> claim(String userId, String matchedWith) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_QUEUE).document(userId)
                .update("matchedWith", matchedWith)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public ListenerRegistration observeEntry(String userId, OnMatchmakingUpdateListener listener) {
        return db.collection(COLLECTION_QUEUE).document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        Log.d("Matchmaking", "Error adding snapshot listener");
                        return;
                    }
                    Log.d("Matchmaking", "Observing matchmaking entry...");
                    MatchmakingEntry entry = snapshot.toObject(MatchmakingEntry.class);
                    if (entry != null && entry.getMatchedWith() != null) {
                        listener.onMatchFound(entry);
                    }
                });
    }
}