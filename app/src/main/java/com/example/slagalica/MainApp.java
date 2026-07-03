package com.example.slagalica;

import android.app.Application;

import com.example.slagalica.presentation.notifications.AppNotificationHelper;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MainApp extends Application {
    @Override
    public void onCreate() { // hi
        super.onCreate();
        AppNotificationHelper.createNotificationChannels(this);
    }
}