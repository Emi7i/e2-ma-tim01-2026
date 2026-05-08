package com.example.slagalica.presentation.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentStatisticsBinding;
import com.example.slagalica.presentation.drawables.PieChartDrawable;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;

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
        setupProgressBars();
        setupClickListeners();
    }

    private void setupPieChart() {
        binding.pieChart.post(() -> {
            float progressValue = 0.65f;
            PieChartDrawable pieChartDrawable = new PieChartDrawable(requireContext());
            pieChartDrawable.setProgress(progressValue);
            
            // Set bounds to match the view size
            int width = binding.pieChart.getWidth();
            int height = binding.pieChart.getHeight();
            pieChartDrawable.setBounds(0, 0, width, height);
            
            binding.pieChart.setBackground(pieChartDrawable);
            
            // Update winrate text
            int percentage = (int) (progressValue * 100);
            binding.winrateText.setText(percentage + "%");
        });
    }

    private void setupProgressBars() {
        // Set progress values for each game
        binding.progressKoZnaZna.setProgress(75);
        binding.progressMojBroj.setProgress(60);
        binding.progressKorakPoKorak.setProgress(85);
        binding.progressAsocijacije.setProgress(100);
        binding.progressSkocko.setProgress(70);
        binding.progressSpojnice.setProgress(65);
    }

    private void setupClickListeners() {
        binding.closeDrawerButton.setOnClickListener(v -> {
            // Switch back to profile fragment in the drawer
            FragmentTransition.to(new ProfileFragment(), requireActivity(), true, R.id.rightDrawer);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
