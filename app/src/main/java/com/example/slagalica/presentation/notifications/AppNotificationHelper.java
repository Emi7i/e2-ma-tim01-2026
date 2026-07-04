package com.example.slagalica.presentation.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.slagalica.R;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationType;
import com.example.slagalica.presentation.activities.AppActivity;

public class AppNotificationHelper {

    public static final String CHANNEL_CHAT = "chat_channel";
    public static final String CHANNEL_RANKING = "ranking_channel";
    public static final String CHANNEL_REWARDS = "rewards_channel";
    public static final String CHANNEL_GENERAL = "general_channel";

    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_NOTIFICATION_ACTION = "extra_notification_action";

    public static final String ACTION_ACCEPT = "action_accept";
    public static final String ACTION_DECLINE = "action_decline";

    public static final String EXTRA_NOTIFICATION_TARGET = "extra_notification_target";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);

        NotificationChannel chatChannel = new NotificationChannel(
                CHANNEL_CHAT,
                "Čet",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationChannel rankingChannel = new NotificationChannel(
                CHANNEL_RANKING,
                "Rangiranje",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationChannel rewardsChannel = new NotificationChannel(
                CHANNEL_REWARDS,
                "Nagrade",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "Ostalo",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        manager.createNotificationChannel(chatChannel);
        manager.createNotificationChannel(rankingChannel);
        manager.createNotificationChannel(rewardsChannel);
        manager.createNotificationChannel(generalChannel);
    }

    public static void showSystemNotification(Context context, NotificationItem item) {
        String channelId = mapTypeToChannel(item.getType());

        Intent openIntent = new Intent(context, AppActivity.class);
        openIntent.putExtra(EXTRA_NOTIFICATION_ID, item.getId());
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                item.getId().hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(item.getTitle())
                .setContentText(item.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(item.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        if (item.hasDecisionAction()) {
            Intent acceptIntent = new Intent(context, NotificationActionReceiver.class);
            acceptIntent.putExtra(EXTRA_NOTIFICATION_ID, item.getId());
            acceptIntent.putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_ACCEPT);

            PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (item.getId() + "_accept").hashCode(),
                    acceptIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent declineIntent = new Intent(context, NotificationActionReceiver.class);
            declineIntent.putExtra(EXTRA_NOTIFICATION_ID, item.getId());
            openIntent.putExtra(EXTRA_NOTIFICATION_TARGET, item.getTarget().name());
            declineIntent.putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_DECLINE);

            PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (item.getId() + "_decline").hashCode(),
                    declineIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            builder.addAction(0, "Prihvati", acceptPendingIntent);
            builder.addAction(0, "Odbij", declinePendingIntent);
        }

        NotificationManagerCompat.from(context).notify(item.getId().hashCode(), builder.build());
    }

    private static String mapTypeToChannel(NotificationType type) {
        switch (type) {
            case CHAT:
                return CHANNEL_CHAT;
            case RANKING:
                return CHANNEL_RANKING;
            case REWARD:
                return CHANNEL_REWARDS;
            default:
                return CHANNEL_GENERAL;
        }
    }
}