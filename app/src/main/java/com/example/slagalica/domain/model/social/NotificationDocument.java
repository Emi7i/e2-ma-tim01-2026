package com.example.slagalica.domain.model.social;

import com.google.firebase.firestore.DocumentId;

public class NotificationDocument {

    @DocumentId
    private String notificationId;

    private String userId;
    private String type;
    private String title;
    private String message;
    private String sender;
    private long timestampMillis;
    private boolean read;
    private boolean hasOpenAction;
    private boolean hasDecisionAction;
    private String target;
    private String actionStatus;

    public NotificationDocument() {
    }

    public NotificationDocument(String notificationId,
                                String userId,
                                String type,
                                String title,
                                String message,
                                String sender,
                                long timestampMillis,
                                boolean read,
                                boolean hasOpenAction,
                                boolean hasDecisionAction,
                                String target,
                                String actionStatus) {
        this.notificationId = notificationId;
        this.userId = userId;
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

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
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

    public boolean isHasOpenAction() {
        return hasOpenAction;
    }

    public boolean isHasDecisionAction() {
        return hasDecisionAction;
    }

    public String getTarget() {
        return target;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setHasOpenAction(boolean hasOpenAction) {
        this.hasOpenAction = hasOpenAction;
    }

    public void setHasDecisionAction(boolean hasDecisionAction) {
        this.hasDecisionAction = hasDecisionAction;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }
}