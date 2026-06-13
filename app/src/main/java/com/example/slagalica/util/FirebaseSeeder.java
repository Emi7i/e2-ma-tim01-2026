package com.example.slagalica.util;

import android.util.Log;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseSeeder {

    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    @Inject
    public FirebaseSeeder(UserProfileRepository userProfileRepository, 
                          UserStatisticsRepository userStatisticsRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userStatisticsRepository = userStatisticsRepository;
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
                testUserId, 85.5, 90.0, 75.0, 80.0, 95.0, 88.0, 82.0, 50, 30
        );

        userStatisticsRepository.saveStatistics(testStats)
                .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Statistics created!"))
                .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Statistics", e); return null; });
    }
}
