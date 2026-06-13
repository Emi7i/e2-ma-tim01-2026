package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;

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
import com.example.slagalica.util.FirebaseSeeder;

import java.util.Arrays;
import com.example.slagalica.util.FirebaseSeeder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject UserProfileRepository userProfileRepository;
    @Inject UserStatisticsRepository userStatisticsRepository;
    @Inject SpojniceRepository spojniceRepository;
    @Inject KoZnaZnaRepository koZnaZnaRepository;
    @Inject AsocijacijeContentRepository asocijacijeContentRepository;
    @Inject SkockoContentRepository skockoContentRepository;
    @Inject NotificationsRepository notificationsRepository;
    @Inject FirebaseSeeder firebaseSeeder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseSeeder.seedTestData();

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
}
