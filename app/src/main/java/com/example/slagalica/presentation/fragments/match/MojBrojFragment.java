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
import com.example.slagalica.databinding.FragmentMojBrojBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MojBrojFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentMojBrojBinding binding;

    public MojBrojFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMojBrojBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
    }
}