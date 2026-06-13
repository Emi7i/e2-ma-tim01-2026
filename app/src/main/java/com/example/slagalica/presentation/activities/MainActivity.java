package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.domain.model.match.games.AsocijacijaColumnDocument;
import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;
import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.repository.impl.AsocijacijeContentRepository;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.NotificationsRepository;
import com.example.slagalica.repository.impl.SkockoContentRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class
MainActivity extends AppCompatActivity {

    @Inject UserProfileRepository userProfileRepository;
    @Inject UserStatisticsRepository userStatisticsRepository;
    @Inject SpojniceRepository spojniceRepository;
    @Inject KoZnaZnaRepository koZnaZnaRepository;
    @Inject AsocijacijeContentRepository asocijacijeContentRepository;
    @Inject SkockoContentRepository skockoContentRepository;
    @Inject NotificationsRepository notificationsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testFullFirebaseSchema();
       // seedAsocijacije();        samo prvi puuut sam pozvala
       // seedSkocko();
        seedNotifications();

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

    private void seedAsocijacije() {
        AsocijacijaDocument a1 = new AsocijacijaDocument(
                null,
                "BOCA",
                java.util.Arrays.asList(
                        new AsocijacijaColumnDocument("A", "BROD",
                                java.util.Arrays.asList("MORE", "POSADA", "SVEMIR", "MORNAR")),
                        new AsocijacijaColumnDocument("B", "KISEONIK",
                                java.util.Arrays.asList("VAZDUH", "O2", "GAS", "PLUCA")),
                        new AsocijacijaColumnDocument("C", "PORUKA",
                                java.util.Arrays.asList("POSLATI", "RAZMENA", "SMS", "PISMO")),
                        new AsocijacijaColumnDocument("D", "STAKLO",
                                java.util.Arrays.asList("PROZOR", "LUSTER", "RAZBITI", "CASA"))
                )
        );

        AsocijacijaDocument a2 = new AsocijacijaDocument(
                null,
                "KIVI",
                java.util.Arrays.asList(
                        new AsocijacijaColumnDocument("A", "PTICA",
                                java.util.Arrays.asList("PLAVA", "RAJSKA", "TRKACICA", "JAJA")),
                        new AsocijacijaColumnDocument("B", "VOCE",
                                java.util.Arrays.asList("SUSENO", "BOBICASTO", "JUZNO", "KRUSKA")),
                        new AsocijacijaColumnDocument("C", "SMEDJA",
                                java.util.Arrays.asList("KOSA", "OCI", "KAFENA", "MEDVED")),
                        new AsocijacijaColumnDocument("D", "NOVI ZELAND",
                                java.util.Arrays.asList("GOSPODAR PRSTENOVA", "OSTRVA", "MAORI", "AUSTRALIJA"))
                )
        );

        AsocijacijaDocument a3 = new AsocijacijaDocument(
                null,
                "BAJKE",
                java.util.Arrays.asList(
                        new AsocijacijaColumnDocument("A", "MEDVED",
                                java.util.Arrays.asList("VELIKI", "MALI", "POLARNI", "MED")),
                        new AsocijacijaColumnDocument("B", "ZABA",
                                java.util.Arrays.asList("BATAK", "BARA", "RODA", "LOKVANJ")),
                        new AsocijacijaColumnDocument("C", "KONJ",
                                java.util.Arrays.asList("GRIVA", "SARAC", "SNAGA", "SEDLO")),
                        new AsocijacijaColumnDocument("D", "PEPELJUGA",
                                java.util.Arrays.asList("MACEHA", "BAL", "VILA", "STIKLA"))
                )
        );

        AsocijacijaDocument a4 = new AsocijacijaDocument(
                null,
                "RUZA",
                java.util.Arrays.asList(
                        new AsocijacijaColumnDocument("A", "VETAR",
                                java.util.Arrays.asList("JUG", "OLUJA", "POVETARAC", "ZMAJ")),
                        new AsocijacijaColumnDocument("B", "CRVENA",
                                java.util.Arrays.asList("KRV", "BOJA", "CIGLA", "PAPRIKA")),
                        new AsocijacijaColumnDocument("C", "VID",
                                java.util.Arrays.asList("BODLJA", "ZVEZDA", "OSTAR", "OKO")),
                        new AsocijacijaColumnDocument("D", "MIRIS",
                                java.util.Arrays.asList("PARFEM", "OPOJAN", "NOS", "SVECA"))
                )
        );

        asocijacijeContentRepository.saveAsocijacija(a1)
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a2))
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a3))
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a4))
                .thenAccept(v -> android.util.Log.d("FirebaseSeed", "SUCCESS: Asocijacije seeded!"))
                .exceptionally(e -> {
                    android.util.Log.e("FirebaseSeed", "FAIL: Asocijacije seed", e);
                    return null;
                });
    }

    private void seedSkocko() {
        SkockoCombinationDocument s1 = new SkockoCombinationDocument(
                null,
                java.util.Arrays.asList("★", "♠", "♣", "♥")
        );

        SkockoCombinationDocument s2 = new SkockoCombinationDocument(
                null,
                java.util.Arrays.asList("♦", "💥", "♠", "★")
        );

        SkockoCombinationDocument s3 = new SkockoCombinationDocument(
                null,
                java.util.Arrays.asList("♣", "♥", "💥", "♦")
        );

        SkockoCombinationDocument s4 = new SkockoCombinationDocument(
                null,
                java.util.Arrays.asList("♠", "♦", "★", "♣")
        );

        skockoContentRepository.saveCombination(s1)
                .thenCompose(v -> skockoContentRepository.saveCombination(s2))
                .thenCompose(v -> skockoContentRepository.saveCombination(s3))
                .thenCompose(v -> skockoContentRepository.saveCombination(s4))
                .thenAccept(v -> android.util.Log.d("FirebaseSeed", "SUCCESS: Skocko seeded!"))
                .exceptionally(e -> {
                    android.util.Log.e("FirebaseSeed", "FAIL: Skocko seed", e);
                    return null;
                });
    }

    private void seedNotifications() {
        NotificationDocument n1 = new NotificationDocument(
                null,
                "test_user_123",
                "GAME_INVITE",
                "Poziv u igru",
                "Petar vas je pozvao u partiju.",
                "Petar",
                System.currentTimeMillis(),
                false,
                true,
                true,
                "GAME_INVITE",
                "PENDING"
        );

        NotificationDocument n2 = new NotificationDocument(
                null,
                "test_user_123",
                "REWARD",
                "Nagrada",
                "Osvojili ste 10 tokena za plasman na rang listi.",
                "Sistem",
                System.currentTimeMillis(),
                false,
                true,
                false,
                "REWARD",
                "NONE"
        );

        notificationsRepository.saveNotification(n1)
                .thenCompose(v -> notificationsRepository.saveNotification(n2))
                .thenAccept(v -> android.util.Log.d("FirebaseSeed", "SUCCESS: Notifications seeded!"))
                .exceptionally(e -> {
                    android.util.Log.e("FirebaseSeed", "FAIL: Notifications seed", e);
                    return null;
                });
    }
}
