package com.example.slagalica.presentation.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentStatisticsBinding;
import com.example.slagalica.presentation.drawables.PieChartDrawable;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.viewmodels.ProfileViewModel;
import com.example.slagalica.presentation.viewmodels.StatisticsViewModel;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private ProfileViewModel profileViewModel;

    public StatisticsFragment() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        statisticsViewModel = new androidx.lifecycle.ViewModelProvider(this).get(StatisticsViewModel.class);
        profileViewModel = new androidx.lifecycle.ViewModelProvider(this).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        observeViewModels();
    }

    ///  Get all data from Firebase, otherwise set ot 0
    private void observeViewModels() {
        // Observe Profile for mini-header
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.username.setText(profile.getUsername());
                
                updateLeagueVisuals(profile);

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    Glide.with(this)
                            .load(profile.getAvatar())
                            .placeholder(R.drawable.profile_icon)
                            .circleCrop()
                            .into(binding.avatar);
                }
            }
        });

        // Observe Statistics
        statisticsViewModel.getUserStatistics().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                updateUI(stats);
            }
        });
    }

    private void updateLeagueVisuals(com.example.slagalica.domain.model.profile.UserProfile profile) {
        String league = profile.getLeague().toLowerCase();
        
        // Set League Border for mini-avatar
        int borderResId = getResources().getIdentifier("league_border_" + league, "drawable", requireContext().getPackageName());
        if (borderResId != 0) {
            binding.leagueBorder.setImageResource(borderResId);
        } else {
            binding.leagueBorder.setImageResource(R.drawable.circular_profile_background);
        }
    }

    private void updateUI(com.example.slagalica.domain.model.progression.UserStatistics stats) {
        binding.successRateText.setText(String.format("Uspesnost: %d%%", (int) stats.getOverallStats()));
        binding.totalGamesText.setText(String.format("Ukupno odigranih: %d partija", stats.getGamesPlayed()));
        
        // Update progress bars
        binding.progressKoZnaZna.setProgress((int) stats.getKoZnaZna());
        binding.progressMojBroj.setProgress((int) stats.getMojBroj());
        binding.progressKorakPoKorak.setProgress((int) stats.getKorakPoKorak());
        binding.progressAsocijacije.setProgress((int) stats.getAsocijacije());
        binding.progressSkocko.setProgress((int) stats.getSkocko());
        binding.progressSpojnice.setProgress((int) stats.getSpojnice());

        // Update Winrate Pie Chart
        float winRate = stats.getGamesPlayed() > 0 ? (float) stats.getWonGames() / stats.getGamesPlayed() : 0f;
        binding.pieChart.post(() -> {
            PieChartDrawable pieChartDrawable = new PieChartDrawable(requireContext());
            pieChartDrawable.setProgress(winRate);
            
            int width = binding.pieChart.getWidth();
            int height = binding.pieChart.getHeight();
            if (width > 0 && height > 0) {
                pieChartDrawable.setBounds(0, 0, width, height);
                binding.pieChart.setBackground(pieChartDrawable);
            }
            
            int percentage = (int) (winRate * 100);
            binding.winrateText.setText(percentage + "%");
        });
    }

    private void setupClickListeners() {
        binding.closeDrawerButton.setOnClickListener(v -> {
            // Switch back to profile when clicked on back
            FragmentTransition.to(new ProfileFragment(), requireActivity(), true, R.id.rightDrawer);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
