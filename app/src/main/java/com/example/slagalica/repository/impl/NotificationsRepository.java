package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.social.NotificationDocument;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationsRepository {
    CompletableFuture<List<NotificationDocument>> getNotificationsForUser(String userId);
    CompletableFuture<Void> saveNotification(NotificationDocument notification);
    CompletableFuture<Void> updateNotification(NotificationDocument notification);
}