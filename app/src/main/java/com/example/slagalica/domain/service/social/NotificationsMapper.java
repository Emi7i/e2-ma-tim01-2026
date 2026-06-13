package com.example.slagalica.domain.service.social;

import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationTarget;
import com.example.slagalica.domain.model.social.NotificationType;

public class NotificationsMapper {

    public NotificationItem toRuntime(NotificationDocument doc) {
        return new NotificationItem(
                doc.getNotificationId(),
                NotificationType.valueOf(doc.getType()),
                doc.getTitle(),
                doc.getMessage(),
                doc.getSender(),
                doc.getTimestampMillis(),
                doc.isRead(),
                doc.isHasOpenAction(),
                doc.isHasDecisionAction(),
                NotificationTarget.valueOf(doc.getTarget()),
                NotificationActionStatus.valueOf(doc.getActionStatus())
        );
    }

    public NotificationDocument toDocument(NotificationItem item, String userId) {
        return new NotificationDocument(
                item.getId(),
                userId,
                item.getType().name(),
                item.getTitle(),
                item.getMessage(),
                item.getSender(),
                item.getTimestampMillis(),
                item.isRead(),
                item.hasOpenAction(),
                item.hasDecisionAction(),
                item.getTarget().name(),
                item.getActionStatus().name()
        );
    }
}