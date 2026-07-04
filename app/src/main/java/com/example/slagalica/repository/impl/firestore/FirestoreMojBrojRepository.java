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
    public CompletableFuture<Void> updateSessionData(String matchId, MojBrojSessionData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(matchId)
                .set(data)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<MojBrojSessionData> getSessionData(String matchId) {
        CompletableFuture<MojBrojSessionData> future = new CompletableFuture<>();
        db.collection(COLLECTION)
                .document(matchId)
                .get()
                .addOnSuccessListener(snapshot -> future.complete(snapshot.toObject(MojBrojSessionData.class)))
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
    public void observeSessionData(String matchId, OnSessionUpdateListener<MojBrojSessionData> listener) {
        db.collection(COLLECTION)
                .document(matchId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    MojBrojSessionData data = snapshot.toObject(MojBrojSessionData.class);
                    if (data != null) listener.onRemoteSessionUpdated(data);
                });
    }
}