package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorakSessionData;
import com.example.slagalica.repository.impl.KorakPoKorakRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreKorakPoKorakRepository implements KorakPoKorakRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_SESSIONS = "korakPoKorakSessions";

    @Inject
    public FirestoreKorakPoKorakRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> updateSessionData(String matchId, KorakPoKorakSessionData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_SESSIONS).document(matchId)
                .set(data)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<KorakPoKorakSessionData> getSessionData(String matchId) {
        CompletableFuture<KorakPoKorakSessionData> future = new CompletableFuture<>();
        db.collection(COLLECTION_SESSIONS).document(matchId)
                .get()
                .addOnSuccessListener(snapshot -> future.complete(snapshot.toObject(KorakPoKorakSessionData.class)))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public void observeSessionData(String matchId, OnSessionUpdateListener<KorakPoKorakSessionData> listener) {
        db.collection(COLLECTION_SESSIONS).document(matchId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    KorakPoKorakSessionData data = snapshot.toObject(KorakPoKorakSessionData.class);
                    if (data != null) {
                        listener.onSessionUpdated(data);
                    }
                });
    }
}
