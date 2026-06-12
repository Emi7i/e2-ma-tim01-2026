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
                        buttons.get(i).setBackgroundResource(R.drawable.game_field_background);
                    } else {
                        buttons.get(i).setVisibility(View.GONE);
                    }
                }
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

        viewModel.isGameFinished().observe(getViewLifecycleOwner(), finished -> {
            if (finished) {
                Toast.makeText(requireContext(), "Igra je završena!", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator if needed
        });
    }

    private void updateHeader(int p1Score, int p2Score) {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setStars(p1Score, p2Score);
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
