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
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class KoZnaZnaFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentGameKoZnaZnaBinding binding;

    private String question = "Koji je najveći broj?";
    private List<String> answers = Arrays.asList("1993", "1235", "3424", "sta");
    private int starNum1 = 55;
    private int starNum2 = 45;
    private String selectedAnswer;

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

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        setupStars();
        setupQuestionAndAnswers();
    }

    private void setupStars() {
        com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setVisibility(View.VISIBLE);
            gameHeader.setStars(starNum1, starNum2);
            gameHeader.setTimer("00:00");
        }

        android.widget.TextView toolbarTitle = requireActivity().findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Ko Zna Zna");
        }
    }

    private void setupQuestionAndAnswers() {
        binding.question.setText(question);

        List<android.widget.Button> answerButtons = Arrays.asList(
                binding.answer1,
                binding.answer2,
                binding.answer3,
                binding.answer4
        );

        for (int i = 0; i < answerButtons.size() && i < answers.size(); i++) {
            android.widget.Button button = answerButtons.get(i);
            button.setText(answers.get(i));
            button.setOnClickListener(v -> {
                selectedAnswer = ((android.widget.Button) v).getText().toString();
                Toast.makeText(requireContext(), "Selected: " + selectedAnswer, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
        binding = null;
    }
}
