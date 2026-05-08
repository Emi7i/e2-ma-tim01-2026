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

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameMojBrojBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MojBrojFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentGameMojBrojBinding binding;
    List<String> tokens = new ArrayList<>();
    

    public MojBrojFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGameMojBrojBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
    }

    private void setupListeners(){
        EditText input = binding.myNumber;


        View.OnClickListener appendListener = v -> {
            tokens.add(((Button) v).getText().toString());
            updateDisplay(input, tokens);
        };

        binding.digit1.setOnClickListener(appendListener);
        binding.digit2.setOnClickListener(appendListener);
        binding.digit3.setOnClickListener(appendListener);
        binding.digit4.setOnClickListener(appendListener);
        binding.doubleDigit1.setOnClickListener(appendListener);
        binding.doubleDigit2.setOnClickListener(appendListener);
        binding.plus.setOnClickListener(appendListener);
        binding.minus.setOnClickListener(appendListener);
        binding.divide.setOnClickListener(appendListener);
        binding.product.setOnClickListener(appendListener);
        binding.leftParen.setOnClickListener(appendListener);
        binding.rightParen.setOnClickListener(appendListener);

        binding.backspaceButton.setOnClickListener(v -> {
            if (!tokens.isEmpty()) {
                tokens.remove(tokens.size() - 1);
                updateDisplay(input, tokens);
            }
        });
    }

    private void updateDisplay(EditText input, List<String> tokens) {
        input.setText(String.join(" ", tokens));
    }
}