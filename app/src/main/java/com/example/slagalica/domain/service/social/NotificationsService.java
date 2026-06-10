package com.example.slagalica.domain.service.social;

import com.example.slagalica.domain.model.social.NotificationFilter;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationType;

import java.util.ArrayList;
import java.util.List;

public class NotificationsService {

    private final List<NotificationItem> allNotifications;

    public NotificationsService(List<NotificationItem> allNotifications) {
        this.allNotifications = allNotifications;
    }

    public List<NotificationItem> getAllNotifications() {
        return allNotifications;
    }

    public List<NotificationItem> getFilteredNotifications(NotificationFilter filter) {
        List<NotificationItem> filtered = new ArrayList<>();

        for (NotificationItem item : allNotifications) {
            if (filter == NotificationFilter.ALL) {
                filtered.add(item);
            } else if (filter == NotificationFilter.READ && item.isRead()) {
                filtered.add(item);
            } else if (filter == NotificationFilter.UNREAD && !item.isRead()) {
                filtered.add(item);
            }
        }

        return filtered;
    }

    public void toggleRead(NotificationItem item) {
        item.setRead(!item.isRead());
    }

    public void markAsRead(NotificationItem item) {
        item.setRead(true);
    }

    public String getTypeLabel(NotificationType type) {
        switch (type) {
            case REWARD:
                return "Tip: Nagrada";
            case GAME_INVITE:
                return "Tip: Poziv u igru";
            case CHAT:
                return "Tip: Čet";
            case LEAGUE:
                return "Tip: Liga";
            case RANKING:
                return "Tip: Rang lista";
            default:
                return "Tip: Ostalo";
        }
    }

    public String getStatusLabel(NotificationItem item) {
        return item.isRead() ? "Status: Pročitano" : "Status: Nepročitano";
    }

    public String getAvailableActionsLabel(NotificationItem item) {
        if (item.hasDecisionAction()) {
            return "Prihvati / Odbij";
        }
        return "Otvori";
    }
}