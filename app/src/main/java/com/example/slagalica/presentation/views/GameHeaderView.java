package com.example.slagalica.presentation.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ViewGameHeaderBinding;

public class GameHeaderView extends LinearLayout {
    ViewGameHeaderBinding binding;

    public GameHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = ViewGameHeaderBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setTimer(String time) {
        binding.timer.setText(time);
    }

    public void setStars(int p1, int p2) {
        binding.player1Stars.setText(String.valueOf(p1));
        binding.player2Stars.setText(String.valueOf(p2));
    }
}
