package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.MatchmakingEntry;
import com.example.slagalica.repository.impl.MatchmakingEntryRepository;
import com.google.firebase.firestore.FirebaseFirestore;

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
    public CompletableFuture<Void> add(String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_QUEUE).document(userId)
                .set(new MatchmakingEntry(userId))
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
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
}