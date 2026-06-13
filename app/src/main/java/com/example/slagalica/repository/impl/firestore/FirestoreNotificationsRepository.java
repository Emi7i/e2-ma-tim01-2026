package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.repository.impl.NotificationsRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreNotificationsRepository implements NotificationsRepository {

    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreNotificationsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<List<NotificationDocument>> getNotificationsForUser(String userId) {
        CompletableFuture<List<NotificationDocument>> future = new CompletableFuture<>();

        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationDocument> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(NotificationDocument.class));
                    }

                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> saveNotification(NotificationDocument notification) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        String notificationId = notification.getNotificationId();

        if (notificationId != null && !notificationId.trim().isEmpty()) {
            db.collection(COLLECTION_NOTIFICATIONS)
                    .document(notificationId)
                    .set(notification)
                    .addOnSuccessListener(aVoid -> future.complete(null))
                    .addOnFailureListener(future::completeExceptionally);
        } else {
            db.collection(COLLECTION_NOTIFICATIONS)
                    .add(notification)
                    .addOnSuccessListener(documentReference -> future.complete(null))
                    .addOnFailureListener(future::completeExceptionally);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> updateNotification(NotificationDocument notification) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        String notificationId = notification.getNotificationId();

        if (notificationId == null || notificationId.trim().isEmpty()) {
            future.completeExceptionally(
                    new IllegalArgumentException("Notification ID is required for update.")
            );
            return future;
        }

        db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}