package com.example.slagalica.presentation.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentFriendProfileBinding;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.UserProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private FragmentFriendProfileBinding binding;

    @Inject
    UserProfileRepository userProfileRepository;

    public static FriendProfileFragment newInstance(String userId) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        String userId = getArguments() != null ? getArguments().getString(ARG_USER_ID) : null;
        if (userId != null) {
            loadProfile(userId);
        }
    }

    private void loadProfile(String userId) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        userProfileRepository.getProfile(userId)
                .thenAccept(profile -> requireActivity().runOnUiThread(() -> {
                    binding.loadingIndicator.setVisibility(View.GONE);
                    if (profile != null) {
                        displayProfile(profile);
                    }
                }))
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            binding.loadingIndicator.setVisibility(View.GONE));
                    return null;
                });
    }

    private void displayProfile(UserProfile profile) {
        binding.usernameText.setText(profile.getUsername());
        binding.starsText.setText(String.valueOf(profile.getNumStars()));
        binding.leagueText.setText(profile.getLeague());
        binding.rankText.setText("#" + profile.getRank());

        String league = profile.getLeague() != null ? profile.getLeague().toLowerCase() : "";
        int borderResId = getResources().getIdentifier("league_border_" + league, "drawable",
                requireContext().getPackageName());
        if (borderResId != 0) {
            binding.leagueBorder.setImageResource(borderResId);
        } else {
            binding.leagueBorder.setImageResource(R.drawable.circular_profile_background);
        }

        if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
            Glide.with(this)
                    .load(profile.getAvatar())
                    .placeholder(R.drawable.profile_icon)
                    .circleCrop()
                    .into(binding.profileAvatar);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
