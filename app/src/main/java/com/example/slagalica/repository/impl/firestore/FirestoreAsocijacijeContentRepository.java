package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;
import com.example.slagalica.repository.impl.AsocijacijeContentRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreAsocijacijeContentRepository implements AsocijacijeContentRepository {

    private static final String COLLECTION_ASOCIJACIJE = "asocijacije";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreAsocijacijeContentRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<AsocijacijaDocument>> getAllAsocijacije() {
        CompletableFuture<List<AsocijacijaDocument>> future = new CompletableFuture<>();

        db.collection(COLLECTION_ASOCIJACIJE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AsocijacijaDocument> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(AsocijacijaDocument.class));
                    }

                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> saveAsocijacija(AsocijacijaDocument asocijacijaDocument) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_ASOCIJACIJE)
                .add(asocijacijaDocument)
                .addOnSuccessListener(documentReference -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}