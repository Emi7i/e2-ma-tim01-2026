package com.example.slagalica.presentation.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Otvorite notifikaciju u aplikaciji da biste reagovali.", Toast.LENGTH_SHORT).show();
    }
}