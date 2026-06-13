package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameKoZnaZnaBinding;
import com.example.slagalica.presentation.viewmodels.KoZnaZnaViewModel;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class KoZnaZnaFragment extends Fragment {
    private KoZnaZnaViewModel viewModel;
    private MatchViewModel matchViewModel;
    private FragmentGameKoZnaZnaBinding binding;

    public KoZnaZnaFragment() { }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameKoZnaZnaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KoZnaZnaViewModel.class);
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        setupToolbar();
        setupHeader();
        observeViewModel();
        setupListeners();
    }

    private void setupHeader() {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setVisibility(View.VISIBLE);
            Integer p1 = matchViewModel.getPlayer1Score().getValue();
            Integer p2 = matchViewModel.getPlayer2Score().getValue();
            gameHeader.setStars(p1 != null ? p1 : 0, p2 != null ? p2 : 0);
        }
    }

    private void setupToolbar() {
        android.widget.TextView toolbarTitle = requireActivity().findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Ko Zna Zna");
        }
    }

    private void observeViewModel() {
        viewModel.getCurrentQuestion().observe(getViewLifecycleOwner(), question -> {
            if (question != null) {
                binding.question.setText(question.getQuestion());
            }
        });

        viewModel.getCurrentAnswers().observe(getViewLifecycleOwner(), answers -> {
            if (answers != null) {
                List<android.widget.Button> buttons = Arrays.asList(
                        binding.answer1, binding.answer2, binding.answer3, binding.answer4
                );
                for (int i = 0; i < buttons.size(); i++) {
                    if (i < answers.size()) {
                        buttons.get(i).setText(answers.get(i));
                        buttons.get(i).setVisibility(View.VISIBLE);
                        buttons.get(i).setEnabled(true);
                        resetButtonBackground(buttons.get(i));
                    } else {
                        buttons.get(i).setVisibility(View.GONE);
                    }
                }
                resetScoreColors();
            }
        });

        viewModel.getPlayer1Delta().observe(getViewLifecycleOwner(), delta -> {
            if (delta != 0) matchViewModel.updatePlayer1Score(delta);
        });

        viewModel.getPlayer2Delta().observe(getViewLifecycleOwner(), delta -> {
            if (delta != 0) matchViewModel.updatePlayer2Score(delta);
        });

        matchViewModel.getPlayer1Score().observe(getViewLifecycleOwner(), p1Score -> {
            updateHeader(p1Score, matchViewModel.getPlayer2Score().getValue() != null ? matchViewModel.getPlayer2Score().getValue() : 0);
        });

        matchViewModel.getPlayer2Score().observe(getViewLifecycleOwner(), p2Score -> {
            updateHeader(matchViewModel.getPlayer1Score().getValue() != null ? matchViewModel.getPlayer1Score().getValue() : 0, p2Score);
        });

        viewModel.getTimeLeft().observe(getViewLifecycleOwner(), timeLeft -> {
            com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
            if (gameHeader != null) {
                gameHeader.setTimer(String.format("00:%02d", timeLeft));
            }
        });

        viewModel.getCanAnswer().observe(getViewLifecycleOwner(), canAnswer -> {
            List<android.widget.Button> buttons = Arrays.asList(
                    binding.answer1, binding.answer2, binding.answer3, binding.answer4
            );
            for (android.widget.Button button : buttons) {
                button.setEnabled(canAnswer);
            }
        });

        viewModel.isWaitingForNextPlayer().observe(getViewLifecycleOwner(), waiting -> {
            if (waiting) {
                showNextPlayerDialog();
            }
        });

        viewModel.isRevealingAnswer().observe(getViewLifecycleOwner(), revealing -> {
            if (revealing) {
                highlightAnswers();
            }
        });

        viewModel.getCurrentPlayerTurn().observe(getViewLifecycleOwner(), turn -> {
            // Optional: Show which player's turn it is
        });

        viewModel.isGameFinished().observe(getViewLifecycleOwner(), finished -> {
            if (finished) {
                Toast.makeText(requireContext(), "Igra je završena!", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator if needed
        });
    }

    private void showNextPlayerDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Igrač 2, tvoj potez")
                .setMessage("IGRAJ")
                .setPositiveButton("Ma moze", (dialog, which) -> viewModel.startNextPlayerTurn())
                .setCancelable(false)
                .show();
    }

    private void updateHeader(int p1Score, int p2Score) {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setStars(p1Score, p2Score);
        }
    }

    private void resetButtonBackground(android.widget.Button button) {
        if (button instanceof com.google.android.material.button.MaterialButton) {
            ((com.google.android.material.button.MaterialButton) button).setBackgroundTintList(null);
        }
        button.setBackgroundResource(R.drawable.game_field_background);
        button.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black));
    }

    private void resetScoreColors() {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            int defaultColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white);
            gameHeader.setStarColors(defaultColor, defaultColor);
        }
    }

    private void highlightAnswers() {
        com.example.slagalica.domain.model.match.games.KoZnaZna current = viewModel.getCurrentQuestion().getValue();
        if (current == null) return;

        List<android.widget.Button> buttons = Arrays.asList(
                binding.answer1, binding.answer2, binding.answer3, binding.answer4
        );

        String correct = current.getCorrectAnswer();
        String p1Answer = viewModel.getPlayer1Answer().getValue();
        String p2Answer = viewModel.getPlayer2Answer().getValue();

        int greenColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.green);
        int blueColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.blue_light);
        int purpleColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.field_border);

        for (android.widget.Button button : buttons) {
            String text = button.getText().toString().trim();
            android.content.res.ColorStateList tint = null;

            // Priority: Correct answer always green
            if (text.equalsIgnoreCase(correct.trim())) {
                tint = android.content.res.ColorStateList.valueOf(greenColor);
            }
            // Then show player choices if they were wrong
            else if (p1Answer != null && text.equalsIgnoreCase(p1Answer.trim())) {
                tint = android.content.res.ColorStateList.valueOf(blueColor);
            }
            else if (p2Answer != null && text.equalsIgnoreCase(p2Answer.trim())) {
                tint = android.content.res.ColorStateList.valueOf(purpleColor);
            }

            if (tint != null) {
                if (button instanceof com.google.android.material.button.MaterialButton) {
                    ((com.google.android.material.button.MaterialButton) button).setBackgroundTintList(tint);
                } else {
                    button.setBackgroundTintList(tint);
                }
            }
        }

        highlightScoresInHeader();
    }

    private void highlightScoresInHeader() {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            int blue = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.blue_light);
            int purple = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.field_border);
            gameHeader.setStarColors(blue, purple);
        }
    }

    private void setupListeners() {
        List<android.widget.Button> buttons = Arrays.asList(
                binding.answer1, binding.answer2, binding.answer3, binding.answer4
        );

        for (android.widget.Button button : buttons) {
            button.setOnClickListener(v -> {
                viewModel.submitAnswer(button.getText().toString());
            });
        }

        binding.nextButton.setOnClickListener(v -> viewModel.nextQuestion());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
        binding = null;
    }
}
