package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.repository.impl.SkockoContentRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreSkockoContentRepository implements SkockoContentRepository {

    private static final String COLLECTION_SKOCKO = "skocko";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreSkockoContentRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<SkockoCombinationDocument>> getAllCombinations() {
        CompletableFuture<List<SkockoCombinationDocument>> future = new CompletableFuture<>();

        db.collection(COLLECTION_SKOCKO)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SkockoCombinationDocument> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(SkockoCombinationDocument.class));
                    }

                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> saveCombination(SkockoCombinationDocument document) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_SKOCKO)
                .add(document)
                .addOnSuccessListener(documentReference -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}