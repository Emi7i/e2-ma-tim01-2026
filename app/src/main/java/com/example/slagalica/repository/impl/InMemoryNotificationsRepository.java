package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationTarget;
import com.example.slagalica.domain.model.social.NotificationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryNotificationsRepository implements NotificationsRepository {

    private static InMemoryNotificationsRepository instance;
    private final List<NotificationItem> notifications = new ArrayList<>();

    private InMemoryNotificationsRepository() {
        seedInitialData();
    }

    public static synchronized InMemoryNotificationsRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryNotificationsRepository();
        }
        return instance;
    }

    @Override
    public synchronized List<NotificationItem> getNotifications() {
        List<NotificationItem> copy = new ArrayList<>(notifications);
        Collections.reverse(copy);
        return copy;
    }

    @Override
    public synchronized void addNotification(NotificationItem item) {
        notifications.add(item);
    }

    @Override
    public synchronized NotificationItem findById(String id) {
        for (NotificationItem item : notifications) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private void seedInitialData() {
        long now = System.currentTimeMillis();

        notifications.add(new NotificationItem(
                "seed_reward_1",
                NotificationType.REWARD,
                "Nagrada",
                "Osvojili ste 5 tokena za plasman na rang listi.",
                "Sistem",
                now - 1000L * 60 * 60,
                false,
                true,
                false,
                NotificationTarget.REWARD,
                NotificationActionStatus.NONE
        ));

        notifications.add(new NotificationItem(
                "seed_invite_1",
                NotificationType.GAME_INVITE,
                "Poziv u igru",
                "Marko vas je pozvao u prijateljsku partiju.",
                "Marko",
                now - 1000L * 60 * 30,
                false,
                true,
                true,
                NotificationTarget.GAME_INVITE,
                NotificationActionStatus.PENDING
        ));

        notifications.add(new NotificationItem(
                "seed_chat_1",
                NotificationType.CHAT,
                "Nova poruka",
                "Ivana vam je poslala poruku u čet.",
                "Ivana",
                now - 1000L * 60 * 10,
                true,
                true,
                false,
                NotificationTarget.CHAT,
                NotificationActionStatus.NONE
        ));
    }
}