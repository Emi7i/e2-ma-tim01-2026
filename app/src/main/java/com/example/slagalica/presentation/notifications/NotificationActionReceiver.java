package com.example.slagalica.presentation.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.service.social.NotificationsService;
import com.example.slagalica.repository.impl.InMemoryNotificationsRepository;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationId = intent.getStringExtra(AppNotificationHelper.EXTRA_NOTIFICATION_ID);
        String action = intent.getStringExtra(AppNotificationHelper.EXTRA_NOTIFICATION_ACTION);

        InMemoryNotificationsRepository repository = InMemoryNotificationsRepository.getInstance();
        NotificationsService service = new NotificationsService(repository);

        NotificationItem item = repository.findById(notificationId);
        if (item == null) {
            return;
        }

        if (AppNotificationHelper.ACTION_ACCEPT.equals(action)) {
            service.acceptInvitation(item);
            Toast.makeText(context, "Poziv prihvaćen", Toast.LENGTH_SHORT).show();
        } else if (AppNotificationHelper.ACTION_DECLINE.equals(action)) {
            service.declineInvitation(item);
            Toast.makeText(context, "Poziv odbijen", Toast.LENGTH_SHORT).show();
        }
    }
}