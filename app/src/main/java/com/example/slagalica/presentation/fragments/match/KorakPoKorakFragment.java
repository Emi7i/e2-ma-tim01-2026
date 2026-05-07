package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameKorakPoKorakBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

// Annotation needed for Hilt to work
@AndroidEntryPoint
public class KorakPoKorakFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentGameKorakPoKorakBinding binding;

    public KorakPoKorakFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGameKorakPoKorakBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class); // generic view model creation
        matchViewModel.setGameActive(true); // activates timer header
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false); // hide timer
        binding = null;
    }
}