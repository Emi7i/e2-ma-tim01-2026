package com.example.slagalica.presentation.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.slagalica.R;
import com.example.slagalica.presentation.fragments.match.AsocijacijeFragment;

public class AsocijacijeActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_TYPE = "extra_game_type";
    public static final String GAME_TYPE_ASOCIJACIJE = "asocijacije";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (savedInstanceState == null) {
            String gameType = getIntent().getStringExtra(EXTRA_GAME_TYPE);

            Fragment fragment;

            if (GAME_TYPE_ASOCIJACIJE.equals(gameType)) {
                fragment = new AsocijacijeFragment();
            } else {
                fragment = new AsocijacijeFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.gameFragmentContainer, fragment)
                    .commit();
        }
    }
}