package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.util.FirebaseSeeder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

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
