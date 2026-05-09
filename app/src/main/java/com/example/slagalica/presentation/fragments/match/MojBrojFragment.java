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
    List<Button> operandButtons;
    List<Button> operatorButtons;
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        gameViewModel.getIsCorrect().observe(getViewLifecycleOwner(), isCorrect -> {
            if (isCorrect) finishGame();
        });

        gameViewModel.getIsGoalSpinning().observe(getViewLifecycleOwner(), isSpinning -> {
            if (isSpinning) {
                handler.post(spinGoalRunnable);
            } else {
                handler.removeCallbacks(spinGoalRunnable);
            }
        });

        gameViewModel.getAreOperandsSpinning().observe(getViewLifecycleOwner(), isSpinning -> {
            if (isSpinning) {
                handler.post(spinOperandsRunnable);
            } else {
                binding.stopSpinning.setVisibility(View.INVISIBLE);
                binding.confirmButton.setVisibility(View.VISIBLE);
                binding.backspaceButton.setEnabled(true);
                handler.removeCallbacks(spinOperandsRunnable);
            }
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
        for (Button btn : operandButtons) btn.setEnabled(false);
        for (Button btn : operatorButtons) btn.setEnabled(false);
    }

    private void setupListeners() {
        EditText input = binding.myAnswer;

        View.OnClickListener operandListener = v -> {
            if (Boolean.FALSE.equals(gameViewModel.getAreOperandsSpinning().getValue())) {
                String value = ((Button) v).getText().toString();
                if (!tokens.contains(value)) {
                    tokens.add(value);
                    updateDisplay(input, tokens);
                    v.setEnabled(false); // visually show it's been used
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
                for (Button btn : operandButtons) {
                    if (btn.getText().toString().equals(removed)) {
                        btn.setEnabled(true);
                        break;
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
            // TODO: save goal to db...
            gameViewModel.stopGoalSpinning();
        }
        else{
            // TODO: save operands to db...
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

    private void setOperands(){
        binding.digit1.setText(String.valueOf(gameViewModel.getSingleDigits()[0]));
        binding.digit2.setText(String.valueOf(gameViewModel.getSingleDigits()[1]));
        binding.digit3.setText(String.valueOf(gameViewModel.getSingleDigits()[2]));
        binding.digit4.setText(String.valueOf(gameViewModel.getSingleDigits()[3]));
        binding.doubleDigit1.setText(String.valueOf(gameViewModel.getDoubleDigits()[0]));
        binding.doubleDigit2.setText(String.valueOf(gameViewModel.getDoubleDigits()[1]));

//        gameViewModel.stopDigitsSpinning();
    }
}