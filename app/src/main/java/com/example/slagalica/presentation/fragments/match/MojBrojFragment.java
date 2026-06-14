package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.slagalica.databinding.FragmentGameMojBrojBinding;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBroj;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.presentation.viewmodels.MojBrojViewModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MojBrojFragment extends Fragment {
    MatchViewModel matchViewModel;
    MojBrojViewModel gameViewModel;
    FragmentGameMojBrojBinding binding;
    List<String> tokens = new ArrayList<>();
    List<Button> operandButtons;
    List<Button> operatorButtons;
    private final Handler handler = new Handler(Looper.getMainLooper());
    Deque<Button> usedOperandStack = new ArrayDeque<>();

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

        operandButtons = List.of(
                binding.digit1, binding.digit2, binding.digit3, binding.digit4,
                binding.doubleDigit1, binding.doubleDigit2
        );

        operatorButtons = List.of(
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
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }

    private final Runnable spinGoalRunnable = new Runnable() {
        @Override
        public void run() {
            updateGoalWithRandomNumber();
            handler.postDelayed(this, 100);
        }
    };

    private final Runnable spinOperandsRunnable = new Runnable() {
        @Override
        public void run() {
            updateOperandsWithRandomNumbers();
            handler.postDelayed(this, 100);
        }
    };

    private void observeViewModel() {
        matchViewModel.getCurrentGame().observe(getViewLifecycleOwner(), igame -> {
            if (igame instanceof MojBroj) {
                gameViewModel.start((MojBroj) igame);
            }
        });

        gameViewModel.getIsGoalSpinning().observe(getViewLifecycleOwner(), isSpinning -> {
            if (isSpinning) {
                resetRoundUi();
                handler.post(spinGoalRunnable);
            } else {
                handler.removeCallbacks(spinGoalRunnable);
                setGoalNumber();
            }
        });

        gameViewModel.getAreOperandsSpinning().observe(getViewLifecycleOwner(), isSpinning -> {
            if (isSpinning) {
                binding.stopSpinning.setVisibility(View.VISIBLE);
                handler.post(spinOperandsRunnable);
            } else {
                handler.removeCallbacks(spinOperandsRunnable);
                setOperands();
                binding.stopSpinning.setVisibility(View.INVISIBLE);
                binding.confirmButton.setVisibility(View.VISIBLE);
                binding.backspaceButton.setEnabled(true);
                for (Button btn : operandButtons) btn.setEnabled(true);
                for (Button btn : operatorButtons) btn.setEnabled(true);
                binding.myAnswer.setEnabled(true);
            }
        });

        gameViewModel.getRoundOver().observe(getViewLifecycleOwner(), over -> {
            if (over) showRoundResults();
        });

        gameViewModel.getTimeLeft().observe(getViewLifecycleOwner(), timeLeft -> {
            com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(com.example.slagalica.R.id.gameHeader);
            if (gameHeader != null && timeLeft != null) {
                gameHeader.setTimer(String.format("00:%02d", timeLeft));
            }
        });

        gameViewModel.getGameOver().observe(getViewLifecycleOwner(), over -> {
            if (over) {
                // TODO: notify Match / navigate to next game
            }
        });
    }

    private void resetRoundUi() {
        tokens.clear();
        usedOperandStack.clear();
        binding.myAnswer.setText("");
        binding.opponentLayout.setVisibility(View.INVISIBLE);
        binding.confirmButton.setVisibility(View.INVISIBLE);
        binding.stopSpinning.setVisibility(View.VISIBLE);
        binding.backspaceButton.setEnabled(false);
        binding.myAnswer.setEnabled(false);
        for (Button btn : operandButtons) {
            btn.setEnabled(false);
            btn.setText("");
        }
        for (Button btn : operatorButtons) btn.setEnabled(false);
        binding.goalNumber.setText("");
        binding.myNumber.setText("");
        binding.opponentNumber.setText("");
        binding.opponentAnswer.setText("");
    }

    private void showRoundResults() {
        binding.opponentNumber.setText(String.valueOf(gameViewModel.getOpponentNumber()));
        binding.myNumber.setText(String.valueOf(gameViewModel.getMyNumber()));
        binding.opponentAnswer.setText(gameViewModel.getOpponentAnswer());
        binding.myAnswer.setEnabled(false);
        binding.backspaceButton.setEnabled(false);
        binding.opponentLayout.setVisibility(View.VISIBLE);
        binding.confirmButton.setVisibility(View.INVISIBLE);
        binding.stopSpinning.setVisibility(View.INVISIBLE);
        for (Button btn : operandButtons) btn.setEnabled(false);
        for (Button btn : operatorButtons) btn.setEnabled(false);

        // brief pause to let the player see results, then advance
        handler.postDelayed(() -> {
            if (gameViewModel.getGame().hasEnded()) {
                // game over handled via getGameOver()
            } else {
                gameViewModel.nextRound();
            }
        }, 3000);
    }

    private void setupListeners() {
        EditText input = binding.myAnswer;

        View.OnClickListener operandListener = v -> {
            if (Boolean.FALSE.equals(gameViewModel.getAreOperandsSpinning().getValue())) {
                Button btn = (Button) v;
                if (!usedOperandStack.contains(btn)) {
                    tokens.add(btn.getText().toString());
                    usedOperandStack.push(btn);
                    updateDisplay(input, tokens);
                    btn.setEnabled(false);
                }
            }
        };

        View.OnClickListener operatorListener = v -> {
            if (Boolean.FALSE.equals(gameViewModel.getAreOperandsSpinning().getValue())) {
                tokens.add(((Button) v).getText().toString());
                updateDisplay(input, tokens);
            }
        };

        for (Button btn : operandButtons) btn.setOnClickListener(operandListener);
        for (Button btn : operatorButtons) btn.setOnClickListener(operatorListener);

        binding.backspaceButton.setOnClickListener(v -> {
            if (!tokens.isEmpty()) {
                String removed = tokens.remove(tokens.size() - 1);
                if (!usedOperandStack.isEmpty()) {
                    String topValue = usedOperandStack.peek().getText().toString();
                    if (topValue.equals(removed)) {
                        usedOperandStack.pop().setEnabled(true);
                    }
                }
                updateDisplay(input, tokens);
            }
        });

        binding.confirmButton.setOnClickListener(v -> gameViewModel.checkAnswer(tokens));

        binding.stopSpinning.setOnClickListener(v -> stopSpinning());
    }

    private void stopSpinning(){
        if(Boolean.TRUE.equals(gameViewModel.getIsGoalSpinning().getValue())){
            gameViewModel.stopGoalSpinning();
        }
        else{
            gameViewModel.stopOperandsSpinning();
        }
    }

    private void updateDisplay(EditText input, List<String> tokens) {
        input.setText(String.join(" ", tokens));
    }

    private void updateGoalWithRandomNumber(){
        gameViewModel.generateGoalNumber();
        setGoalNumber();
    }

    private void updateOperandsWithRandomNumbers(){
        gameViewModel.generateOperands();
        setOperands();
    }

    private void setGoalNumber(){
        binding.goalNumber.setText(String.valueOf(gameViewModel.getGoalNumber()));
//        gameViewModel.stopGoalSpinning();
    }

    private void setOperands() {
        List<Integer> single = gameViewModel.getSingleDigits();
        List<Integer> doubleDigits = gameViewModel.getDoubleDigits();
        if (single.size() < 4 || doubleDigits.size() < 2) return;
        binding.digit1.setText(String.valueOf(single.get(0)));
        binding.digit2.setText(String.valueOf(single.get(1)));
        binding.digit3.setText(String.valueOf(single.get(2)));
        binding.digit4.setText(String.valueOf(single.get(3)));
        binding.doubleDigit1.setText(String.valueOf(doubleDigits.get(0)));
        binding.doubleDigit2.setText(String.valueOf(doubleDigits.get(1)));
    }
}