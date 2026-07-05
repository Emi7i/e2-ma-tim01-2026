package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.social.MatchRequest;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.concurrent.CompletableFuture;

public interface MatchRequestRepository {
    CompletableFuture<String> createRequest(MatchRequest request);
    CompletableFuture<Void> updateStatus(String requestId, String status);
    /** Atomically accepts the request only if its status is still PENDING. Returns true if accepted. */
    CompletableFuture<Boolean> acceptIfPending(String requestId, String matchId);
    ListenerRegistration listenForIncomingPending(String userId, RequestListener listener);
    ListenerRegistration listenForRequest(String requestId, RequestListener listener);

    interface RequestListener {
        void onRequest(MatchRequest request);
    }
}
