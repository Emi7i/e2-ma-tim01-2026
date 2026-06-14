package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBrojSessionData;
import com.example.slagalica.repository.impl.MojBrojRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreMojBrojRepository implements MojBrojRepository {

    private static final String COLLECTION = "mojBrojSessions";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreMojBrojRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> updateSessionData(long matchId, MojBrojSessionData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(String.valueOf(matchId))
                .set(data)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<MojBrojSessionData> getSessionData(long matchId) {
        CompletableFuture<MojBrojSessionData> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(String.valueOf(matchId))
                .get()
                .addOnSuccessListener(snapshot -> future.complete(snapshot.toObject(MojBrojSessionData.class)))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public void observeSessionData(long matchId, OnSessionUpdateListener<MojBrojSessionData> listener) {
        db.collection(COLLECTION)
                .document(String.valueOf(matchId))
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    MojBrojSessionData data = snapshot.toObject(MojBrojSessionData.class);
                    if (data != null) listener.onSessionUpdated(data);
                });
    }
}