package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

public class FirestoreKoZnaZnaRepository implements KoZnaZnaRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_KO_ZNA_ZNA = "koZnaZna";

    @Inject
    public FirestoreKoZnaZnaRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<KoZnaZna>> getAllKoZnaZna() {
        CompletableFuture<List<KoZnaZna>> future = new CompletableFuture<>();
        db.collection(COLLECTION_KO_ZNA_ZNA).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<KoZnaZna> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(KoZnaZna.class));
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<KoZnaZna> getRandomKoZnaZna() {
        return getRandomQuestions(1).thenApply(list -> list.isEmpty() ? null : list.get(0));
    }

    @Override
    public CompletableFuture<List<KoZnaZna>> getRandomQuestions(int count) {
        return getAllKoZnaZna().thenApply(list -> {
            if (list.isEmpty()) return new ArrayList<>();
            java.util.Collections.shuffle(list);
            return list.subList(0, Math.min(count, list.size()));
        });
    }

    @Override
    public CompletableFuture<Void> seedData(List<KoZnaZna> questions) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        for (KoZnaZna q : questions) {
            batch.set(db.collection(COLLECTION_KO_ZNA_ZNA).document(), q);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
