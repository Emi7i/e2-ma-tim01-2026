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

import android.graphics.Bitmap;
import android.util.TypedValue;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.util.QrCodeGenerator;
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

                updateLeagueVisuals(profile);
                updateQrCode(profile);

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

    private void updateLeagueVisuals(com.example.slagalica.domain.model.profile.UserProfile profile) {
        String league = profile.getLeague().toLowerCase();
        
        // 1. Set League Border
        int borderResId = getResources().getIdentifier("league_border_" + league, "drawable", requireContext().getPackageName());
        if (borderResId != 0) {
            binding.leagueBorder.setImageResource(borderResId);
        } else {
            // Default border or shape if image not found
            binding.leagueBorder.setImageResource(R.drawable.circular_profile_background);
        }

        // 2. Set League Badge/Icon
        int badgeResId = getResources().getIdentifier("league_badge_" + league, "drawable", requireContext().getPackageName());
        if (badgeResId != 0) {
            binding.leagueIcon.setImageResource(badgeResId);
        }

        // 3. Set Static Icons
        binding.starsIcon.setImageResource(R.drawable.icon_stars);
        binding.rankIcon.setImageResource(R.drawable.icon_rank);
        binding.regionIcon.setImageResource(R.drawable.icon_region);
    }

    private void updateQrCode(com.example.slagalica.domain.model.profile.UserProfile profile) {
        String content = profile.getQrCode() != null ? profile.getQrCode() : profile.getUserId();
        if (content == null || content.isEmpty()) return;
        int sizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
        Bitmap qr = QrCodeGenerator.generate(content, sizePx);
        if (qr != null) {
            binding.qrCodeImage.setImageBitmap(qr);
        }
    }

    private void setupClickListeners() {
        binding.avatarContainer.setOnClickListener(v -> {
            showAvatarSelectionDialog();
        });

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

    private void showAvatarSelectionDialog() {
        String[] avatarUrls = {
            "https://media1.tenor.com/m/kqLCp6Ow_dQAAAAd/bug-cat-capoo-blue.gif",
            "https://media1.tenor.com/m/_Af1fysbSW4AAAAd/bugcat-bugcat-shine.gif",
            "https://media1.tenor.com/m/8V5_7dB1jsYAAAAd/capoo-waiting.gif",
            "https://media1.tenor.com/m/xPy26qAy0xYAAAAd/capoo-bugcat.gif",
            "https://media1.tenor.com/m/rBVT1zDLGJwAAAAd/bugcat-capoo-happy.gif"
        };

        String[] avatarNames = {
            "Bugcat 1",
            "Bugcat 2",
            "Bugcat 3",
            "Bugcat 4",
            "Bugcat 5"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Izaberi avatar");
        builder.setItems(avatarNames, (dialog, which) -> {
            viewModel.updateAvatar(avatarUrls[which]);
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
