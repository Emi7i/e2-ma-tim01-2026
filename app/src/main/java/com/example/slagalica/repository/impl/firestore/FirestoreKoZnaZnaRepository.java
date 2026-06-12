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
        return getAllKoZnaZna().thenApply(list -> {
            if (list.isEmpty()) return null;
            return list.get(new Random().nextInt(list.size()));
        });
    }
}
