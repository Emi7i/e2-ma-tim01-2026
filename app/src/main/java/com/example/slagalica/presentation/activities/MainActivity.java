package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLoggedIn()) {
            startActivity(new Intent(this, AppActivity.class));
        } else {
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }

    private boolean isLoggedIn(){
        return false; // will probably check phone storage to check if user is logged in or something
    }
}