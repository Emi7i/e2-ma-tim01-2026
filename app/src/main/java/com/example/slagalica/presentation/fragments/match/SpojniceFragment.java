package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameSpojniceBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpojniceFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentGameSpojniceBinding binding;

    private int starNum1 = 55;
    private int starNum2 = 45;
    private String selectedLeftCard;
    private String selectedRightCard;
    private android.widget.Button selectedLeftButton;
    private android.widget.Button selectedRightButton;

    // Matching pairs: left card -> right card
    private final java.util.Map<String, String> matchingPairs = new java.util.HashMap<>();

    public SpojniceFragment() { }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameSpojniceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        setupStars();
        setupGameBoard();
    }

    private void setupStars() {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setVisibility(View.VISIBLE);
            gameHeader.setStars(starNum1, starNum2);
            gameHeader.setTimer("18:56");
        }

        android.widget.TextView toolbarTitle = requireActivity().findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Spojnice");
        }
    }

    private void setupGameBoard() {
        matchingPairs.put("Ma-i-a", "Hee");
        matchingPairs.put("Ma-i-aa", "Hoo");
        matchingPairs.put("Ma-i-aaa", "Ha");
        matchingPairs.put("Ma-i-aaaa", "Ha ha");

        // Add to lists
        List<String> leftCardTexts = new java.util.ArrayList<>(matchingPairs.keySet());
        List<String> rightCardTexts = new java.util.ArrayList<>(matchingPairs.values());
        
        // Shuffle shuffle
        Collections.shuffle(leftCardTexts);
        Collections.shuffle(rightCardTexts);

        List<android.widget.Button> leftCards = Arrays.asList(
                binding.leftCard1,
                binding.leftCard2,
                binding.leftCard3,
                binding.leftCard4,
                binding.leftCard5,
                binding.leftCard6,
                binding.leftCard7
        );

        List<android.widget.Button> rightCards = Arrays.asList(
                binding.rightCard1,
                binding.rightCard2,
                binding.rightCard3,
                binding.rightCard4,
                binding.rightCard5,
                binding.rightCard6,
                binding.rightCard7
        );

        // Set left card texts
        for (int i = 0; i < leftCards.size() && i < leftCardTexts.size(); i++) {
            android.widget.Button button = leftCards.get(i);
            button.setText(leftCardTexts.get(i));
        }

        // Set right card texts
        for (int i = 0; i < rightCards.size() && i < rightCardTexts.size(); i++) {
            android.widget.Button button = rightCards.get(i);
            button.setText(rightCardTexts.get(i));
        }

        for (android.widget.Button button : leftCards) {
            button.setOnClickListener(v -> {
                selectedLeftCard = ((android.widget.Button) v).getText().toString();
                selectedLeftButton = (android.widget.Button) v;
                checkMatch();
            });
        }

        for (android.widget.Button button : rightCards) {
            button.setOnClickListener(v -> {
                selectedRightCard = ((android.widget.Button) v).getText().toString();
                selectedRightButton = (android.widget.Button) v;
                checkMatch();
            });
        }
    }

    private void checkMatch() {
        if (selectedLeftButton == null || selectedRightButton == null) return;

        String correctMatch = matchingPairs.get(selectedLeftCard);
        if (correctMatch != null && correctMatch.equals(selectedRightCard)) {
            setButtonTint(selectedLeftButton, android.R.color.holo_purple);
            setButtonTint(selectedRightButton, android.R.color.holo_purple);
            selectedLeftButton.setEnabled(false);
            selectedRightButton.setEnabled(false);
        } else {
            setButtonTint(selectedLeftButton, android.R.color.darker_gray);
            selectedLeftButton.setEnabled(false);
        }

        selectedLeftButton = null;
        selectedRightButton = null;
        selectedLeftCard = null;
        selectedRightCard = null;
    }

    private void setButtonTint(android.widget.Button button, int colorRes) {
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(requireContext(), colorRes)
                )
        );
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
        binding = null;
    }
}
