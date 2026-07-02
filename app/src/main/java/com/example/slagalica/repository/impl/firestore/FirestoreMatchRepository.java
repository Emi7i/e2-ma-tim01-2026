package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.MatchSessionData;
import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.repository.impl.MatchRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreMatchRepository implements MatchRepository {

    private static final String COLLECTION = "matchSessions";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreMatchRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<String> create(MatchSessionData data) {
        CompletableFuture<String> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(docRef -> future.complete(docRef.getId()))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> update(String matchId, MatchSessionData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(matchId)
                .set(data)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<MatchSessionData> get(String matchId) {
        CompletableFuture<MatchSessionData> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(matchId)
                .get()
                .addOnSuccessListener(snapshot -> future.complete(snapshot.toObject(MatchSessionData.class)))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> delete(String matchId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(matchId)
                .delete()
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public void observe(String matchId, OnSessionUpdateListener<MatchSessionData> listener) {
        db.collection(COLLECTION)
                .document(matchId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    MatchSessionData data = snapshot.toObject(MatchSessionData.class);
                    if (data != null) listener.onSessionUpdated(data);
                });
    }
}
