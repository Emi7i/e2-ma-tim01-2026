package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private String question = "Pitanje";
    private List<String> answers = Arrays.asList("1993", "1235", "3424", "sta");

    public KoZnaZnaFragment() {
        // Required empty public constructor
    }

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

        setupQuestionAndAnswers();
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
            answerButtons.get(i).setText(answers.get(i));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
        binding = null;
    }
}
