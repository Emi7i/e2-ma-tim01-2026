package com.example.slagalica.util;

import android.util.Log;

import com.example.slagalica.domain.model.match.games.AsocijacijaColumnDocument;
import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;
import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.domain.service.match.KoZnaZnaDemoFactory;
import com.example.slagalica.domain.service.match.SpojniceDemoFactory;
import com.example.slagalica.repository.impl.AsocijacijeContentRepository;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import com.example.slagalica.repository.impl.NotificationsRepository;
import com.example.slagalica.repository.impl.SkockoContentRepository;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseSeeder {

    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final KoZnaZnaRepository koZnaZnaRepository;
    private final SpojniceRepository spojniceRepository;
    private final AsocijacijeContentRepository asocijacijeContentRepository;
    private final SkockoContentRepository skockoContentRepository;
    private final NotificationsRepository notificationsRepository;

    @Inject
    public FirebaseSeeder(UserProfileRepository userProfileRepository, 
                          UserStatisticsRepository userStatisticsRepository,
                          KoZnaZnaRepository koZnaZnaRepository,
                          SpojniceRepository spojniceRepository,
                          AsocijacijeContentRepository asocijacijeContentRepository,
                          SkockoContentRepository skockoContentRepository,
                          NotificationsRepository notificationsRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.koZnaZnaRepository = koZnaZnaRepository;
        this.spojniceRepository = spojniceRepository;
        this.asocijacijeContentRepository = asocijacijeContentRepository;
        this.skockoContentRepository = skockoContentRepository;
        this.notificationsRepository = notificationsRepository;
    }

    public void seedTestData() {
        String testUserId = "test_user_123";

        // 1. Test Profile
        UserProfile testProfile = new UserProfile(
                testUserId, "Bugcat", "bug@cat.com",
                "https://media1.tenor.com/m/kqLCp6Ow_dQAAAAd/bug-cat-capoo-blue.gif",  // BUGCAT!!!!!!!! DO NOT TOUCH
                100L, 10L, "Gold", "Global", "qr_code_data", 42L
        );

        userProfileRepository.saveProfile(testProfile)
                .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Profile created!"))
                .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Profile", e); return null; });

        // 2. Test Statistics
        UserStatistics testStats = new UserStatistics(
                testUserId, 85.5, 90.0, 75.0, 80.0, 95.0, 88.0, 82.0, 50L, 30L,
                100L, 90L, 100L, 75L, 100L, 80L, 100L, 95L, 100L, 88L, 100L, 82L
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

        // 5. Asocijacije Data
        asocijacijeContentRepository.getAllAsocijacije().thenAccept(data -> {
            if (data.isEmpty()) {
                seedAsocijacije();
            }
        });

        // 6. Skocko Data
        skockoContentRepository.getAllCombinations().thenAccept(data -> {
            if (data.isEmpty()) {
                seedSkocko();
            }
        });

        // 7. Notifications
        seedNotifications();
    }

    private void seedAsocijacije() {
        AsocijacijaDocument a1 = new AsocijacijaDocument(
                null,
                "BOCA",
                Arrays.asList(
                        new AsocijacijaColumnDocument("A", "BROD",
                                Arrays.asList("MORE", "POSADA", "SVEMIR", "MORNAR")),
                        new AsocijacijaColumnDocument("B", "KISEONIK",
                                Arrays.asList("VAZDUH", "O2", "GAS", "PLUCA")),
                        new AsocijacijaColumnDocument("C", "PORUKA",
                                Arrays.asList("POSLATI", "RAZMENA", "SMS", "PISMO")),
                        new AsocijacijaColumnDocument("D", "STAKLO",
                                Arrays.asList("PROZOR", "LUSTER", "RAZBITI", "CASA"))
                )
        );

        AsocijacijaDocument a2 = new AsocijacijaDocument(
                null,
                "KIVI",
                Arrays.asList(
                        new AsocijacijaColumnDocument("A", "PTICA",
                                Arrays.asList("PLAVA", "RAJSKA", "TRKACICA", "JAJA")),
                        new AsocijacijaColumnDocument("B", "VOCE",
                                Arrays.asList("SUSENO", "BOBICASTO", "JUZNO", "KRUSKA")),
                        new AsocijacijaColumnDocument("C", "SMEDJA",
                                Arrays.asList("KOSA", "OCI", "KAFENA", "MEDVED")),
                        new AsocijacijaColumnDocument("D", "NOVI ZELAND",
                                Arrays.asList("GOSPODAR PRSTENOVA", "OSTRVA", "MAORI", "AUSTRALIJA"))
                )
        );

        AsocijacijaDocument a3 = new AsocijacijaDocument(
                null,
                "BAJKE",
                Arrays.asList(
                        new AsocijacijaColumnDocument("A", "MEDVED",
                                Arrays.asList("VELIKI", "MALI", "POLARNI", "MED")),
                        new AsocijacijaColumnDocument("B", "ZABA",
                                Arrays.asList("BATAK", "BARA", "RODA", "LOKVANJ")),
                        new AsocijacijaColumnDocument("C", "KONJ",
                                Arrays.asList("GRIVA", "SARAC", "SNAGA", "SEDLO")),
                        new AsocijacijaColumnDocument("D", "PEPELJUGA",
                                Arrays.asList("MACEHA", "BAL", "VILA", "STIKLA"))
                )
        );

        AsocijacijaDocument a4 = new AsocijacijaDocument(
                null,
                "RUZA",
                Arrays.asList(
                        new AsocijacijaColumnDocument("A", "VETAR",
                                Arrays.asList("JUG", "OLUJA", "POVETARAC", "ZMAJ")),
                        new AsocijacijaColumnDocument("B", "CRVENA",
                                Arrays.asList("KRV", "BOJA", "CIGLA", "PAPRIKA")),
                        new AsocijacijaColumnDocument("C", "VID",
                                Arrays.asList("BODLJA", "ZVEZDA", "OSTAR", "OKO")),
                        new AsocijacijaColumnDocument("D", "MIRIS",
                                Arrays.asList("PARFEM", "OPOJAN", "NOS", "SVECA"))
                )
        );

        asocijacijeContentRepository.saveAsocijacija(a1)
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a2))
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a3))
                .thenCompose(v -> asocijacijeContentRepository.saveAsocijacija(a4))
                .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: Asocijacije seeded!"))
                .exceptionally(e -> {
                    Log.e("FirebaseSeeder", "FAIL: Asocijacije seed", e);
                    return null;
                });
    }

    private void seedSkocko() {
        SkockoCombinationDocument s1 = new SkockoCombinationDocument(
                null,
                Arrays.asList("★", "♠", "♣", "♥")
        );

        SkockoCombinationDocument s2 = new SkockoCombinationDocument(
                null,
                Arrays.asList("♦", "💥", "♠", "★")
        );

        SkockoCombinationDocument s3 = new SkockoCombinationDocument(
                null,
                Arrays.asList("♣", "♥", "💥", "♦")
        );

        SkockoCombinationDocument s4 = new SkockoCombinationDocument(
                null,
                Arrays.asList("♠", "♦", "★", "♣")
        );

        skockoContentRepository.saveCombination(s1)
                .thenCompose(v -> skockoContentRepository.saveCombination(s2))
                .thenCompose(v -> skockoContentRepository.saveCombination(s3))
                .thenCompose(v -> skockoContentRepository.saveCombination(s4))
                .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: Skocko seeded!"))
                .exceptionally(e -> {
                    Log.e("FirebaseSeeder", "FAIL: Skocko seed", e);
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
                .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: Notifications seeded!"))
                .exceptionally(e -> {
                    Log.e("FirebaseSeeder", "FAIL: Notifications seed", e);
                    return null;
                });
    }
}
