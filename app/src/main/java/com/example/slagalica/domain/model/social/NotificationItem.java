package com.example.slagalica.domain.model.social;

public class NotificationItem {

    private final String id;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String sender;
    private final long timestampMillis;
    private boolean read;
    private final boolean hasOpenAction;
    private final boolean hasDecisionAction;

    private final NotificationTarget target;
    private NotificationActionStatus actionStatus;

    public NotificationItem(String id,
                            NotificationType type,
                            String title,
                            String message,
                            String sender,
                            long timestampMillis,
                            boolean read,
                            boolean hasOpenAction,
                            boolean hasDecisionAction,
                            NotificationTarget target,
                            NotificationActionStatus actionStatus) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.sender = sender;
        this.timestampMillis = timestampMillis;
        this.read = read;
        this.hasOpenAction = hasOpenAction;
        this.hasDecisionAction = hasDecisionAction;
        this.target = target;
        this.actionStatus = actionStatus;
    }

    public String getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean hasOpenAction() {
        return hasOpenAction;
    }

    public boolean hasDecisionAction() {
        return hasDecisionAction;
    }

    public NotificationTarget getTarget() {
        return target;
    }

    public NotificationActionStatus getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(NotificationActionStatus actionStatus) {
        this.actionStatus = actionStatus;
    }
}