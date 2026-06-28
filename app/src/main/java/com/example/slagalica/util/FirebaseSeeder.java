package com.example.slagalica.util;

import android.util.Log;

import com.example.slagalica.domain.model.match.games.AsocijacijaColumnDocument;
import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;
import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.match.games.korakpokorak.TermWithHints;
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
import com.example.slagalica.repository.impl.TermRepository;
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
    private final TermRepository termRepository;

    @Inject
    public FirebaseSeeder(UserProfileRepository userProfileRepository,
                          UserStatisticsRepository userStatisticsRepository,
                          KoZnaZnaRepository koZnaZnaRepository,
                          SpojniceRepository spojniceRepository,
                          AsocijacijeContentRepository asocijacijeContentRepository,
                          SkockoContentRepository skockoContentRepository,
                          NotificationsRepository notificationsRepository,
                          TermRepository termRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.koZnaZnaRepository = koZnaZnaRepository;
        this.spojniceRepository = spojniceRepository;
        this.asocijacijeContentRepository = asocijacijeContentRepository;
        this.skockoContentRepository = skockoContentRepository;
        this.notificationsRepository = notificationsRepository;
        this.termRepository = termRepository;
    }

    public void seedTestData() {
        String testUserId = "test_user_123";

        // 1. Test Profile
        userProfileRepository.getProfile(testUserId).thenAccept(profile -> {
            if (profile == null) {
                UserProfile testProfile = new UserProfile(
                        testUserId, "Bugcat", "bug@cat.com",
                        "https://media1.tenor.com/m/kqLCp6Ow_dQAAAAd/bug-cat-capoo-blue.gif",
                        100L, 10L, "Gold", "Global", "qr_code_data", 42L
                );
                userProfileRepository.saveProfile(testProfile)
                        .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Profile created!"))
                        .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Profile", e); return null; });
            }
        });

        // 2. Test Statistics
        userStatisticsRepository.getStatistics(testUserId).thenAccept(stats -> {
            if (stats == null) {
                UserStatistics testStats = UserStatistics.createNew(testUserId);
                testStats.setOverallStats(85.5);
                testStats.setKoZnaZna(90.0);
                testStats.setMojBroj(75.0);
                testStats.setKorakPoKorak(80.0);
                testStats.setAsocijacije(95.0);
                testStats.setSkocko(88.0);
                testStats.setSpojnice(82.0);
                testStats.setGamesPlayed(50L);
                testStats.setWonGames(30L);

                testStats.setKoZnaZnaTotal(100L);
                testStats.setKoZnaZnaCorrect(90L);
                testStats.setMojBrojTotal(100L);
                testStats.setMojBrojCorrect(75L);
                testStats.setKorakPoKorakTotal(100L);
                testStats.setKorakPoKorakCorrect(80L);
                testStats.setAsocijacijeTotal(100L);
                testStats.setAsocijacijeCorrect(95L);
                testStats.setSkockoTotal(100L);
                testStats.setSkockoCorrect(88L);
                testStats.setSpojniceTotal(100L);
                testStats.setSpojniceCorrect(82L);

                userStatisticsRepository.saveStatistics(testStats)
                        .thenAccept(aVoid -> Log.d("FirebaseSeeder", "SUCCESS: Statistics created!"))
                        .exceptionally(e -> { Log.e("FirebaseSeeder", "FAIL: Statistics", e); return null; });
            }
        });

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

        // 8. Terms for Korak po korak
        termRepository.getAllTerms().thenAccept(data -> {
            Log.d("FirebaseSeeder", "getAllTerms returned size: " + data.size());
            if(data.isEmpty()){
                seedTerms();
            }
        }).exceptionally(e -> {
            Log.e("FirebaseSeeder", "FAIL: getAllTerms", e);
            return null;
        });
    }

    // Korak po korak terms and hints
    public void seedTerms() {
        TermWithHints t1 = new TermWithHints("Sunce", Arrays.asList(
                "Ima veze sa danom i noći.",
                "Bez ovoga ne bi bilo života na Zemlji.",
                "Pojavljuje se ujutru, nestaje uveče.",
                "Daje svetlost i toplotu.",
                "Žuto je i okruglo na nebu.",
                "Ne sme se gledati direktno u ovo.",
                "Centar je Sunčevog sistema."
        ));

        TermWithHints t2 = new TermWithHints("Knjiga", Arrays.asList(
                "Ima veze sa pričama i znanjem.",
                "Može biti tanka ili debela.",
                "Često se čuva na polici.",
                "U nazivima predmeta u školi često se pominje ova reč.",
                "Sadrži stranice i naslov.",
                "Otvara se da bi se čitala.",
                "Ima korice i stranice."
        ));

        TermWithHints t3 = new TermWithHints("Kafa", Arrays.asList(
                "Ima veze sa jutrom i budnošću.",
                "Može biti crna ili sa mlekom.",
                "Sadrži kofein.",
                "Pravi se od zrna koja se prže.",
                "U imenima pića često se pominje ova reč, npr. espreso.",
                "Pije se topla, najčešće ujutru.",
                "Ima jak miris koji budi ljude."
        ));

        TermWithHints t4 = new TermWithHints("Kišobran", Arrays.asList(
                "Ima veze sa kišom i lošim vremenom.",
                "Može biti savijen ili otvoren.",
                "Štiti od nečega što pada s neba.",
                "Ima ručku i šipke ispod tkanine.",
                "Otvara se pritiskom na dugme.",
                "Najčešće se koristi kad pada kiša.",
                "Kad se otvori, liči na kupolu."
        ));

        TermWithHints t5 = new TermWithHints("Planina", Arrays.asList(
                "Ima veze sa prirodom i visinama.",
                "Može biti prekrivena snegom i ledom.",
                "Osvaja se penjanjem.",
                "Veća je od brda.",
                "Ima vrh, padine i podnožje.",
                "U imenima poznatih vrhova često se pominje ova reč, npr. Everest.",
                "Često ima stene i šume."
        ));

        TermWithHints t6 = new TermWithHints("Sat", Arrays.asList(
                "Ima veze sa vremenom i tačnošću.",
                "Može biti na zidu, ruci ili telefonu.",
                "Ima kazaljke ili prikazuje brojeve.",
                "Prati protok vremena.",
                "Otkucava svaku sekundu.",
                "U nazivu narukvice koja pokazuje vreme nalazi se ova reč.",
                "Zvoni ujutru da signalizira ustajanje."
        ));

        termRepository.saveTerm(t1)
                .thenCompose(v -> termRepository.saveTerm(t2))
                .thenCompose(v -> termRepository.saveTerm(t3))
                .thenCompose(v -> termRepository.saveTerm(t4))
                .thenCompose(v -> termRepository.saveTerm(t5))
                .thenCompose(v -> termRepository.saveTerm(t6))
                .thenAccept(v -> Log.d("FirebaseSeeder", "SUCCESS: Terms seeded!"))
                .exceptionally(e -> {
                    Log.e("FirebaseSeeder", "FAIL: Terms seed", e);
                    return null;
                });
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
