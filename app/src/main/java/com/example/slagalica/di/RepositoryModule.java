package com.example.slagalica.di;

import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.KorakPoKorakRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.TermRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreKoZnaZnaRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreKorakPoKorakRepository;
import com.example.slagalica.repository.impl.firestore.FirestoreSpojniceRepository;
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
}
