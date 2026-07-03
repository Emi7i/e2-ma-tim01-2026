package com.example.slagalica.di;

import com.example.slagalica.repository.impl.FriendsRepository;
import com.example.slagalica.repository.impl.MatchRequestRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreFriendsRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreMatchRequestRepository;
import com.example.slagalica.repository.impl.AsocijacijeContentRepository;
import com.example.slagalica.repository.impl.RegionStatsRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreRegionStatsRepository;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.KorakPoKorakRepository;
import com.example.slagalica.repository.impl.MatchRepository;
import com.example.slagalica.repository.impl.MatchmakingEntryRepository;
import com.example.slagalica.repository.impl.MojBrojRepository;
import com.example.slagalica.repository.impl.NotificationsRepository;
import com.example.slagalica.repository.impl.SkockoContentRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.SpojniceSessionRepository;
import com.example.slagalica.repository.impl.TermRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreAsocijacijeContentRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreKoZnaZnaRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreKorakPoKorakRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreMatchRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreMatchmakingEntryRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreMojBrojRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreNotificationsRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreSkockoContentRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreSpojniceRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreSpojniceSessionRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreTermRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreUserProfileRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreUserStatisticsRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract UserProfileRepository bindUserProfileRepository(FirestoreUserProfileRepository impl);

    @Binds
    @Singleton
    public abstract UserStatisticsRepository bindUserStatisticsRepository(FirestoreUserStatisticsRepository impl);

    @Binds
    @Singleton
    public abstract SpojniceRepository bindSpojniceRepository(FirestoreSpojniceRepository impl);

    @Binds
    @Singleton
    public abstract KoZnaZnaRepository bindKoZnaZnaRepository(FirestoreKoZnaZnaRepository impl);

    @Binds
    @Singleton
    public abstract TermRepository bindTermRepository(FirestoreTermRepository impl);

    @Binds
    @Singleton
    public abstract KorakPoKorakRepository bindKorakPoKorakRepository(FirestoreKorakPoKorakRepository impl);

    @Binds
    @Singleton
    public abstract AsocijacijeContentRepository bindAsocijacijeContentRepository(
            FirestoreAsocijacijeContentRepository impl
    );

    @Binds
    @Singleton
    public abstract SkockoContentRepository bindSkockoContentRepository(
            FirestoreSkockoContentRepository impl
    );

    @Binds
    @Singleton
    public abstract NotificationsRepository bindNotificationsRepository(
            FirestoreNotificationsRepository impl
    );

    @Binds
    @Singleton
    public abstract MojBrojRepository bindMojBrojRepository(
            FirestoreMojBrojRepository impl
    );

    @Binds
    @Singleton
    public abstract FriendsRepository bindFriendsRepository(
            FirestoreFriendsRepository impl
    );

    @Binds
    @Singleton
    public abstract MatchRequestRepository bindMatchRequestRepository(
            FirestoreMatchRequestRepository impl
    );

    @Binds
    @Singleton
    public abstract RegionStatsRepository bindRegionStatsRepository(
            FirestoreRegionStatsRepository impl
    );

    @Binds
    @Singleton
    public abstract MatchRepository bindMatchRepository(
            FirestoreMatchRepository impl
    );

    @Binds
    @Singleton
    public abstract SpojniceSessionRepository bindSpojniceSessionRepository(
            FirestoreSpojniceSessionRepository impl
    );

    @Binds
    @Singleton
    public abstract MatchmakingEntryRepository bindMatchmakingEntryRepository(
            FirestoreMatchmakingEntryRepository impl
    );
}
