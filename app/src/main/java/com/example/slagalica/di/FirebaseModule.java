package com.example.slagalica.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    // Set this to your dev machine's IP when testing on a real device on the
    // same network, or "10.0.2.2" when testing on the Android Studio emulator.
    private static final String EMULATOR_HOST = "10.0.2.2";
    private static final int EMULATOR_PORT = 8080;
    private static final boolean USE_FIRESTORE_EMULATOR = false; // flip to false to go back to production

    @Provides
    @Singleton
    public FirebaseFirestore provideFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (USE_FIRESTORE_EMULATOR) {
            db.useEmulator(EMULATOR_HOST, EMULATOR_PORT);
        }
        return db;
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (USE_FIRESTORE_EMULATOR) {
            auth.useEmulator(EMULATOR_HOST, 9099); // 9099 is the Auth emulator's default port
        }
        return auth;
    }
}