package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.social.ChatMessage;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ChatRepository {
    CompletableFuture<Void> sendMessage(ChatMessage message);

    /** Listens for the region's chat history in real time, ordered oldest to newest. */
    ListenerRegistration listenForMessages(String region, ChatMessagesListener listener);

    interface ChatMessagesListener {
        void onMessages(List<ChatMessage> allMessages, List<ChatMessage> newlyAddedMessages);
    }
}
