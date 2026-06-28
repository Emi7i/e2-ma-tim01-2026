package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

public class FirestoreUserProfileRepository implements UserProfileRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_USERS = "profiles";

    @Inject
    public FirestoreUserProfileRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<UserProfile> getProfile(String userId) {
        CompletableFuture<UserProfile> future = new CompletableFuture<>();
        db.collection(COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        future.complete(documentSnapshot.toObject(UserProfile.class));
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveProfile(UserProfile profile) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection(COLLECTION_USERS).document(profile.getUserId()).set(profile)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<UserProfile> findByUsername(String username) {
        CompletableFuture<UserProfile> future = new CompletableFuture<>();
        db.collection(COLLECTION_USERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        future.complete(querySnapshot.getDocuments().get(0).toObject(UserProfile.class));
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    @Override
    public CompletableFuture<List<UserProfile>> getAllProfiles() {
        CompletableFuture<List<UserProfile>> future = new CompletableFuture<>();
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        future.complete(querySnapshot.toObjects(UserProfile.class)))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}