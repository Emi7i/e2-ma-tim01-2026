package com.example.slagalica.presentation.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slagalica.databinding.FragmentStatisticsBinding;
import com.example.slagalica.presentation.drawables.PieChartDrawable;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;

    public StatisticsFragment() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPieChart();
        setupClickListeners();
    }

    private void setupPieChart() {
        PieChartDrawable pieChartDrawable = new PieChartDrawable(requireContext());
        pieChartDrawable.setProgress(0.55f); // 55% winrate
        binding.pieChart.setBackground(pieChartDrawable);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
