package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.social.NotificationItem;

import java.util.List;

public interface NotificationsRepository {
    List<NotificationItem> getNotifications();
    void addNotification(NotificationItem item);
    NotificationItem findById(String id);
}