package com.example.slagalica.presentation.fragments.profile;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.GravityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentProfileBinding;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.viewmodels.ProfileViewModel;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.usernameText.setText(profile.getUsername());
                binding.emailText.setText(profile.getEmail());
                binding.tokensText.setText(String.format("%d tokena", profile.getNumTokens()));
                binding.starsText.setText(String.format("Zvezdice: %d", profile.getNumStars()));
                binding.leagueText.setText(String.format("Liga: %s", profile.getLeague()));
                binding.regionText.setText(String.format("Region: %s", profile.getRegion()));
                binding.rankText.setText(String.format("Rank: %d", profile.getRank()));

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    Glide.with(this)
                            .load(profile.getAvatar())
                            .placeholder(R.drawable.profile_icon)
                            .circleCrop()
                            .into(binding.profileAvatar);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator if we had one
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message
            }
        });
    }

    private void setupClickListeners() {
        binding.statisticsButton.setOnClickListener(v -> {
            // Switch to statistics fragment in the drawer
            FragmentTransition.to(new StatisticsFragment(), requireActivity(), true, R.id.rightDrawer);
        });

        binding.closeDrawerButton.setOnClickListener(v -> {
            // Close profile drawer
            androidx.drawerlayout.widget.DrawerLayout drawerLayout = requireActivity().findViewById(R.id.main);
            drawerLayout.closeDrawer(GravityCompat.END);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
