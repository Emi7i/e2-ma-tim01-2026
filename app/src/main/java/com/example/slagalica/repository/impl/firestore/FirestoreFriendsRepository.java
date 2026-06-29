package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.repository.impl.FriendsRepository;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreFriendsRepository implements FriendsRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION = "friendships";
    private static final String FIELD_FRIEND_IDS = "friendIds";

    @Inject
    public FirestoreFriendsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<String>> getFriendIds(String userId) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        db.collection(COLLECTION).document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> ids = (List<String>) doc.get(FIELD_FRIEND_IDS);
                        future.complete(ids != null ? ids : new ArrayList<>());
                    } else {
                        future.complete(new ArrayList<>());
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> addFriend(String userId, String friendId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION).document(userId)
                .update(FIELD_FRIEND_IDS, FieldValue.arrayUnion(friendId))
                .addOnSuccessListener(v -> future.complete(null))
                .addOnFailureListener(e -> {
                    Map<String, Object> data = new HashMap<>();
                    List<String> ids = new ArrayList<>();
                    ids.add(friendId);
                    data.put(FIELD_FRIEND_IDS, ids);
                    db.collection(COLLECTION).document(userId).set(data)
                            .addOnSuccessListener(v -> future.complete(null))
                            .addOnFailureListener(future::completeExceptionally);
                });
        return future;
    }
}
