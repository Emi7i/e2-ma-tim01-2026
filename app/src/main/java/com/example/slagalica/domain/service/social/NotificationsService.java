package com.example.slagalica.domain.service.social;

import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationsService {

    public NotificationsService() {
    }

    public void toggleRead(NotificationItem item) {
        item.setRead(!item.isRead());
    }

    public void markAsRead(NotificationItem item) {
        item.setRead(true);
    }

    public void acceptInvitation(NotificationItem item) {
        item.setRead(true);
        item.setActionStatus(NotificationActionStatus.ACCEPTED);
    }

    public void declineInvitation(NotificationItem item) {
        item.setRead(true);
        item.setActionStatus(NotificationActionStatus.DECLINED);
    }

    public String getTypeLabel(NotificationType type) {
        switch (type) {
            case CHAT:
                return "Tip: Čet";
            case RANKING:
                return "Tip: Rang lista";
            case REWARD:
                return "Tip: Nagrada";
            case GAME_INVITE:
                return "Tip: Poziv u igru";
            case LEAGUE:
                return "Tip: Liga";
            default:
                return "Tip: Ostalo";
        }
    }

    public String getStatusLabel(NotificationItem item) {
        return item.isRead() ? "Status: Pročitano" : "Status: Nepročitano";
    }

    public String getActionStatusLabel(NotificationItem item) {
        switch (item.getActionStatus()) {
            case ACCEPTED:
                return "Reakcija: Prihvaćeno";
            case DECLINED:
                return "Reakcija: Odbijeno";
            case PENDING:
                return "Reakcija: Na čekanju";
            default:
                return "Reakcija: Nema";
        }
    }

    public String formatTimestamp(long timestampMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }
}