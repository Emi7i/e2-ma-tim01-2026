package com.example.slagalica.presentation.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ViewGameHeaderBinding;

public class GameHeaderView extends LinearLayout {

    private final ViewGameHeaderBinding binding;

    public GameHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = ViewGameHeaderBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setTimer(String time) {
        binding.timer.setText(time != null && !time.trim().isEmpty() ? time : "00:00");
    }

    public void setPlayerNames(String playerOneName, String playerTwoName) {
        String safePlayerOne = (playerOneName == null || playerOneName.trim().isEmpty())
                ? "Igrač 1"
                : playerOneName;

        String safePlayerTwo = (playerTwoName == null || playerTwoName.trim().isEmpty())
                ? "Igrač 2"
                : playerTwoName;

        binding.player1Name.setText(safePlayerOne);
        binding.player2Name.setText(safePlayerTwo);
    }

    public void setStars(int playerOneStars, int playerTwoStars) {
        binding.player1Stars.setText(String.valueOf(playerOneStars));
        binding.player2Stars.setText(String.valueOf(playerTwoStars));
    }

    public void setStarColors(int color1, int color2) {
        binding.player1Stars.setTextColor(color1);
        binding.player2Stars.setTextColor(color2);
    }

    public void setActivePlayer(int currentPlayer) {
        if (currentPlayer == 1) {
            binding.player1Name.setBackgroundResource(R.drawable.bg_player_name_active);
            binding.player2Name.setBackgroundResource(R.drawable.game_field_background);
        } else if (currentPlayer == 2) {
            binding.player1Name.setBackgroundResource(R.drawable.game_field_background);
            binding.player2Name.setBackgroundResource(R.drawable.bg_player_name_active);
        } else {
            binding.player1Name.setBackgroundResource(R.drawable.game_field_background);
            binding.player2Name.setBackgroundResource(R.drawable.game_field_background);
        }
    }
}