package com.example.slagalica.util;

import android.util.Log;

import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.domain.service.match.KoZnaZnaDemoFactory;
import com.example.slagalica.domain.service.match.SpojniceDemoFactory;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseSeeder {

    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final KoZnaZnaRepository koZnaZnaRepository;
    private final SpojniceRepository spojniceRepository;

    @Inject
    public FirebaseSeeder(UserProfileRepository userProfileRepository, 
                          UserStatisticsRepository userStatisticsRepository,
                          KoZnaZnaRepository koZnaZnaRepository,
                          SpojniceRepository spojniceRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.koZnaZnaRepository = koZnaZnaRepository;
        this.spojniceRepository = spojniceRepository;
    }

    public void seedTestData() {
        String testUserId = "test_user_123";

        // 1. Test Profile
        UserProfile testProfile = new UserProfile(
                testUserId, "Bugcat", "bug@cat.com",
                "https://media1.tenor.com/m/kqLCp6Ow_dQAAAAd/bug-cat-capoo-blue.gif",  // BUGCAT!!!!!!!! DO NOT TOUCH
                100, 10, "Gold", "Global", "qr_code_data", 42
        );

        userProfileRepository.saveProfile(testProfile)
                .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Profile created!"))
                .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Profile", e); return null; });

        // 2. Test Statistics
        UserStatistics testStats = new UserStatistics(
                testUserId, 85.5, 90.0, 75.0, 80.0, 95.0, 88.0, 82.0, 50, 30,
                100, 90, 100, 75, 100, 80, 100, 95, 100, 88, 100, 82
        );

        userStatisticsRepository.saveStatistics(testStats)
                .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Statistics created!"))
                .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Statistics", e); return null; });

        // 3. KoZnaZna Questions
        koZnaZnaRepository.getAllKoZnaZna().thenAccept(questions -> {
            if (questions.isEmpty()) {
                Log.d("FirebaseSeeder", "Seeding KoZnaZna data...");
                List<KoZnaZna> demoQuestions = new KoZnaZnaDemoFactory().createDemoQuestions();
                koZnaZnaRepository.seedData(demoQuestions)
                        .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: KoZnaZna seeded!"))
                        .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: KoZnaZna seeding", e); return null; });
            }
        });

        // 4. Spojnice Data
        spojniceRepository.getAllSpojnice().thenAccept(data -> {
            if (data.isEmpty()) {
                Log.d("FirebaseSeeder", "Seeding Spojnice data...");
                List<Spojnice> demoSpojnice = new SpojniceDemoFactory().createDemoSpojnice();
                spojniceRepository.seedData(demoSpojnice)
                        .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: Spojnice seeded!"))
                        .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Spojnice seeding", e); return null; });
            }
        });
    }
}
