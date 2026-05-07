package com.example.slagalica.presentation.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.R;
import com.example.slagalica.presentation.fragments.match.SkockoFragment;

public class SkockoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skocko);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.skockoFragmentContainer, new SkockoFragment())
                    .commit();
        }
    }
}