package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.progression.RegionStatsDocument;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreRegionStatsRepository implements RegionStatsRepository {

    private static final String COLLECTION = "region_stats";
    private final FirebaseFirestore db;

    @Inject
    public FirestoreRegionStatsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<RegionStatsDocument>> getAllRegionStats() {
        CompletableFuture<List<RegionStatsDocument>> future = new CompletableFuture<>();
        db.collection(COLLECTION).get()
                .addOnSuccessListener(snapshot -> {
                    List<RegionStatsDocument> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RegionStatsDocument rd = doc.toObject(RegionStatsDocument.class);
                        if (rd != null) list.add(rd);
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> incrementField(String regionKey, String field, long delta) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Map<String, Object> update = new HashMap<>();
        update.put(field, FieldValue.increment(delta));
        db.collection(COLLECTION).document(regionKey)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(v -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
