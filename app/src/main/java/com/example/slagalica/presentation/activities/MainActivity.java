package com.example.slagalica.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.R;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenAsocijacije;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenAsocijacije = findViewById(R.id.btnOpenAsocijacije);

        btnOpenAsocijacije.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AsocijacijeActivity.class);
            intent.putExtra(AsocijacijeActivity.EXTRA_GAME_TYPE, AsocijacijeActivity.GAME_TYPE_ASOCIJACIJE);
            startActivity(intent);
        });
    }
}