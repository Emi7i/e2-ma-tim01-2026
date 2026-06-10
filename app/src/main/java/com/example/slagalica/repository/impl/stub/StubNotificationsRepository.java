package com.example.slagalica.repository.impl.stub;

import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationType;
import com.example.slagalica.repository.impl.NotificationsRepository;

import java.util.ArrayList;
import java.util.List;

public class StubNotificationsRepository implements NotificationsRepository {

    @Override
    public List<NotificationItem> getNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();

        notifications.add(new NotificationItem(
                NotificationType.REWARD,
                "Nagrada",
                "Osvojili ste 5 tokena za plasman na rang listi.",
                "Sistem",
                "Danas u 11:05",
                false,
                true,
                false
        ));

        notifications.add(new NotificationItem(
                NotificationType.GAME_INVITE,
                "Poziv u igru",
                "Marko vas je pozvao u prijateljsku partiju.",
                "Marko",
                "Danas u 10:42",
                false,
                true,
                true
        ));

        notifications.add(new NotificationItem(
                NotificationType.CHAT,
                "Nova poruka",
                "Ivana vam je poslala poruku u čet.",
                "Ivana",
                "Juče u 22:18",
                true,
                true,
                false
        ));

        notifications.add(new NotificationItem(
                NotificationType.LEAGUE,
                "Nova liga",
                "Prešli ste u višu ligu. Čestitamo!",
                "Sistem",
                "Juče u 20:10",
                false,
                true,
                false
        ));

        notifications.add(new NotificationItem(
                NotificationType.RANKING,
                "Rang lista",
                "Završili ste ciklus na 3. mestu.",
                "Sistem",
                "24.04.2026. u 18:30",
                true,
                true,
                false
        ));

        return notifications;
    }
}