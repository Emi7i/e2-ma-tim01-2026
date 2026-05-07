package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.R;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenAsocijacije;
    private Button btnOpenSkocko;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenAsocijacije = findViewById(R.id.btnOpenAsocijacije);
        btnOpenSkocko = findViewById(R.id.btnOpenSkocko);

        btnOpenAsocijacije.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AsocijacijeActivity.class);
            startActivity(intent);
        });


        btnOpenSkocko.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SkockoActivity.class);
            startActivity(intent);
        });
    }
}