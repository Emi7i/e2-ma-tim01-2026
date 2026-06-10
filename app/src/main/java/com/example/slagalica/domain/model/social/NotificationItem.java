package com.example.slagalica.domain.model.social;

public class NotificationItem {

    private final NotificationType type;
    private final String title;
    private final String message;
    private final String sender;
    private final String timestamp;
    private boolean read;
    private final boolean hasOpenAction;
    private final boolean hasDecisionAction;

    public NotificationItem(NotificationType type,
                            String title,
                            String message,
                            String sender,
                            String timestamp,
                            boolean read,
                            boolean hasOpenAction,
                            boolean hasDecisionAction) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.read = read;
        this.hasOpenAction = hasOpenAction;
        this.hasDecisionAction = hasDecisionAction;
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

    public String getTimestamp() {
        return timestamp;
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
}
