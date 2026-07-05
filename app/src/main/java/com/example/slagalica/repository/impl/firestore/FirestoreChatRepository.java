package com.example.slagalica.repository.impl.firestore;

import com.example.slagalica.domain.model.social.ChatMessage;
import com.example.slagalica.repository.impl.ChatRepository;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirestoreChatRepository implements ChatRepository {

    private static final String COLLECTION_CHAT_MESSAGES = "chat_messages";

    private final FirebaseFirestore db;

    @Inject
    public FirestoreChatRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> sendMessage(ChatMessage message) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_CHAT_MESSAGES)
                .add(message)
                .addOnSuccessListener(ref -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    @Override
    public ListenerRegistration listenForMessages(String region, ChatMessagesListener listener) {
        // Sorted client-side (rather than via .orderBy()) so this doesn't depend on a
        // composite Firestore index being created for region == + timestamp order.
        return db.collection(COLLECTION_CHAT_MESSAGES)
                .whereEqualTo("region", region)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot == null) return;

                    List<ChatMessage> all = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        all.add(doc.toObject(ChatMessage.class));
                    }
                    all.sort((a, b) -> Long.compare(a.getTimestampMillis(), b.getTimestampMillis()));

                    List<ChatMessage> added = new ArrayList<>();
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            added.add(change.getDocument().toObject(ChatMessage.class));
                        }
                    }

                    listener.onMessages(all, added);
                });
    }
}
