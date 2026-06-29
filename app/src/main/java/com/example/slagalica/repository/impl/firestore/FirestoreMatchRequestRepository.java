package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.social.MatchRequest;
import com.example.slagalica.repository.impl.MatchRequestRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreMatchRequestRepository implements MatchRequestRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION = "match_requests";

    @Inject
    public FirestoreMatchRequestRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<String> createRequest(MatchRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        db.collection(COLLECTION).add(request)
                .addOnSuccessListener(ref -> future.complete(ref.getId()))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> updateStatus(String requestId, String status) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION).document(requestId)
                .update("status", status)
                .addOnSuccessListener(v -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Boolean> acceptIfPending(String requestId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DocumentReference ref = db.collection(COLLECTION).document(requestId);
        db.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(ref);
            if (MatchRequest.STATUS_PENDING.equals(doc.getString("status"))) {
                transaction.update(ref, "status", MatchRequest.STATUS_ACCEPTED);
                return true;
            }
            return false;
        })
        .addOnSuccessListener(accepted -> future.complete((Boolean) accepted))
        .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public ListenerRegistration listenForIncomingPending(String userId, RequestListener listener) {
        return db.collection(COLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", MatchRequest.STATUS_PENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        listener.onRequest(
                                snapshot.getDocuments().get(0).toObject(MatchRequest.class));
                    } else {
                        listener.onRequest(null);
                    }
                });
    }

    @Override
    public ListenerRegistration listenForRequest(String requestId, RequestListener listener) {
        return db.collection(COLLECTION).document(requestId)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        listener.onRequest(snapshot.toObject(MatchRequest.class));
                    } else {
                        listener.onRequest(null);
                    }
                });
    }
}
