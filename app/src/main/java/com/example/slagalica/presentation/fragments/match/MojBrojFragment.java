package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.slagalica.databinding.FragmentGameMojBrojBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.presentation.viewmodels.MojBrojViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MojBrojFragment extends Fragment {
    MatchViewModel matchViewModel;
    MojBrojViewModel gameViewModel;
    FragmentGameMojBrojBinding binding;
    List<String> tokens = new ArrayList<>();
    List<Button> numpadButtons;

    public MojBrojFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameMojBrojBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        gameViewModel = new ViewModelProvider(this).get(MojBrojViewModel.class);
        matchViewModel.setGameActive(true);

        binding.goalNumber.setText(String.valueOf(gameViewModel.getGoalNumber()));

        numpadButtons = List.of(
                binding.digit1, binding.digit2, binding.digit3, binding.digit4,
                binding.doubleDigit1, binding.doubleDigit2,
                binding.plus, binding.minus, binding.divide, binding.product,
                binding.leftParen, binding.rightParen
        );

        setupListeners();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
    }

    private void observeViewModel() {
        gameViewModel.getIsCorrect().observe(getViewLifecycleOwner(), isCorrect -> {
            if (isCorrect) finishGame();
        });
    }

    private void finishGame() {
        binding.opponentNumber.setText(String.valueOf(gameViewModel.getOpponentNumber()));
        binding.myNumber.setText(String.valueOf(gameViewModel.getMyNumber()));
        binding.opponentAnswer.setText(String.valueOf(gameViewModel.getOpponentAnswer()));
        binding.myAnswer.setEnabled(false);
        binding.backspaceButton.setEnabled(false);
        binding.opponentLayout.setVisibility(View.VISIBLE);
        binding.confirmButton.setVisibility(View.INVISIBLE);
        for (Button btn : numpadButtons) btn.setEnabled(false);
    }

    private void setupListeners() {
        EditText input = binding.myAnswer;

        View.OnClickListener appendListener = v -> {
            tokens.add(((Button) v).getText().toString());
            updateDisplay(input, tokens);
        };

        for (Button btn : numpadButtons) btn.setOnClickListener(appendListener);

        binding.backspaceButton.setOnClickListener(v -> {
            if (!tokens.isEmpty()) {
                tokens.remove(tokens.size() - 1);
                updateDisplay(input, tokens);
            }
        });

        binding.confirmButton.setOnClickListener(v -> gameViewModel.checkAnswer(tokens));
    }

    private void updateDisplay(EditText input, List<String> tokens) {
        input.setText(String.join(" ", tokens));
    }
}