package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.repository.impl.UserStatisticsRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

public class FirestoreUserStatisticsRepository implements UserStatisticsRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_STATS = "statistics";

    @Inject
    public FirestoreUserStatisticsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<UserStatistics> getStatistics(String userId) {
        CompletableFuture<UserStatistics> future = new CompletableFuture<>();
        db.collection(COLLECTION_STATS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        future.complete(documentSnapshot.toObject(UserStatistics.class));
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveStatistics(UserStatistics statistics) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_STATS).document(statistics.getUserId()).set(statistics)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
