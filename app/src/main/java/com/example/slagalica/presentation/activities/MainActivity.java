package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject UserProfileRepository userProfileRepository;
    @Inject UserStatisticsRepository userStatisticsRepository;
    @Inject SpojniceRepository spojniceRepository;
    @Inject KoZnaZnaRepository koZnaZnaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testFullFirebaseSchema();

        if (isLoggedIn()) {
            startActivity(new Intent(this, AppActivity.class));
        } else {
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }

    private boolean isLoggedIn(){
        return false;
    }

    private void testFullFirebaseSchema() {
        String testUserId = "test_user_123";

        // 1. Test Profile
        UserProfile testProfile = new UserProfile(
                testUserId, "FirebaseTester", "test@example.com", "avatar_url", 100, 10, "Gold", "Global", "qr_code_data"
        );

        userProfileRepository.saveProfile(testProfile)
                .thenAccept(aVoid -> Log.d("FirebaseTest", "SUCCESS: Profile created!"))
                .exceptionally(e -> { Log.e("FirebaseTest", "FAIL: Profile", e); return null; });

        // 2. Test Statistics
        UserStatistics testStats = new UserStatistics(
                testUserId, 85.5, 90.0, 75.0, 80.0, 95.0, 88.0, 82.0, 50, 30
        );

        userStatisticsRepository.saveStatistics(testStats)
                .thenAccept(aVoid -> Log.d("FirebaseTest", "SUCCESS: Statistics created!"))
                .exceptionally(e -> { Log.e("FirebaseTest", "FAIL: Statistics", e); return null; });

        // Note: Writing to 'spojnice' and 'koZnaZna' usually happens via admin/seeding,
        // but for testing the connection we can use the Firestore instance directly if needed.
        // For now, these two show the connection works for the user-specific data.
    }
}
