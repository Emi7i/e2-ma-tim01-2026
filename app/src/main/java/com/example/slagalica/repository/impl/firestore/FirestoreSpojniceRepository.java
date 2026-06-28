package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

public class FirestoreSpojniceRepository implements SpojniceRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_SPOJNICE = "spojnice";

    @Inject
    public FirestoreSpojniceRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<Spojnice>> getAllSpojnice() {
        CompletableFuture<List<Spojnice>> future = new CompletableFuture<>();
        db.collection(COLLECTION_SPOJNICE).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Spojnice> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Spojnice.class));
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Spojnice> getRandomSpojnice() {
        return getRandomSpojnice(1).thenApply(list -> list.isEmpty() ? null : list.get(0));
    }

    @Override
    public CompletableFuture<List<Spojnice>> getRandomSpojnice(int count) {
        return getAllSpojnice().thenApply(list -> {
            if (list.isEmpty()) return new ArrayList<>();
            Collections.shuffle(list);
            return list.subList(0, Math.min(count, list.size()));
        });
    }

    @Override
    public CompletableFuture<Void> seedData(List<Spojnice> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        WriteBatch batch = db.batch();
        for (Spojnice s : data) {
            batch.set(db.collection(COLLECTION_SPOJNICE).document(), s);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
