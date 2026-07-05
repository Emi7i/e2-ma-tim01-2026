package com.example.slagalica.domain.service.progression;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.progression.League;
import com.example.slagalica.domain.model.progression.LeagueChangeEvent;
import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationTarget;
import com.example.slagalica.domain.model.social.NotificationType;
import com.example.slagalica.domain.service.social.NotificationsMapper;
import com.example.slagalica.presentation.notifications.AppNotificationHelper;
import com.example.slagalica.repository.impl.NotificationsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

// Spec 2.g: notify the player the moment they enter a new league or drop a
// league. In-app banner if the app is foregrounded (SessionManager.isOnline()),
// otherwise a system notification — also persisted, so it still shows up in
// the Notifications screen afterwards.
@Singleton
public class LeagueNotificationService {

    private final Context appContext;
    private final SessionManager sessionManager;
    private final NotificationsRepository notificationsRepository;
    private final NotificationsMapper notificationsMapper = new NotificationsMapper();
    private final MutableLiveData<LeagueChangeEvent> event = new MutableLiveData<>();

    @Inject
    public LeagueNotificationService(@ApplicationContext Context appContext,
                                      SessionManager sessionManager,
                                      NotificationsRepository notificationsRepository) {
        this.appContext = appContext;
        this.sessionManager = sessionManager;
        this.notificationsRepository = notificationsRepository;
    }

    public LiveData<LeagueChangeEvent> getEvent() {
        return event;
    }

    public void notifyChange(String userId, League newLeague, boolean promoted) {
        String title = promoted ? "Napredovanje u ligu!" : "Pad u ligu";
        String message = promoted
                ? "Čestitamo! Napredovali ste u ligu: " + newLeague.getDisplayName() + "."
                : "Nažalost, pali ste u ligu: " + newLeague.getDisplayName() + ".";

        if (sessionManager.isOnline()) {
            event.postValue(new LeagueChangeEvent(newLeague, promoted, title, message));
            return;
        }

        NotificationItem item = new NotificationItem(
                "league_" + System.currentTimeMillis(),
                NotificationType.LEAGUE,
                title,
                message,
                "Sistem",
                System.currentTimeMillis(),
                false,
                true,
                false,
                NotificationTarget.LEAGUE,
                NotificationActionStatus.NONE
        );

        notificationsRepository.saveNotification(notificationsMapper.toDocument(item, userId));
        AppNotificationHelper.showSystemNotification(appContext, item);
    }
}
