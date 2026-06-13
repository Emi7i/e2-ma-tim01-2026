package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.korakpokorak.TermWithHints;
import com.example.slagalica.repository.impl.TermRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreTermRepository implements TermRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_TERMS = "korakPoKorakTerms";

    @Inject
    public FirestoreTermRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public CompletableFuture<List<TermWithHints>> getAllTerms() {
        CompletableFuture<List<TermWithHints>> future = new CompletableFuture<>();
        db.collection(COLLECTION_TERMS).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TermWithHints> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(TermWithHints.class));
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<TermWithHints> getRandomTermWithHints() {
        return getAllTerms().thenApply(list -> {
            if (list.isEmpty()) return null;
            return list.get(new Random().nextInt(list.size()));
        });
    }

    @Override
    public CompletableFuture<Void> saveTerm(TermWithHints term) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_TERMS).add(term)
                .addOnSuccessListener(docRef -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}