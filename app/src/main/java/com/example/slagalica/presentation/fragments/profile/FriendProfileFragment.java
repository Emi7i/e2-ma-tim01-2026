package com.example.slagalica.presentation.fragments.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentFriendProfileBinding;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.social.MatchRequest;
import com.example.slagalica.presentation.viewmodels.MatchRequestViewModel;
import com.example.slagalica.repository.impl.UserProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private FragmentFriendProfileBinding binding;
    private MatchRequestViewModel matchRequestViewModel;

    private String friendUserId;
    private String friendUsername;

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

        friendUserId = getArguments() != null ? getArguments().getString(ARG_USER_ID) : null;
        matchRequestViewModel = new ViewModelProvider(requireActivity())
                .get(MatchRequestViewModel.class);

        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.matchRequestButton.setOnClickListener(v -> onMatchRequestButtonClick());

        observeMatchRequestState();

        if (friendUserId != null) {
            loadProfile(friendUserId);
        }
    }

    private void observeMatchRequestState() {
        matchRequestViewModel.getOutgoingRequest().observe(getViewLifecycleOwner(), request -> {
            boolean pending = request != null
                    && friendUserId != null
                    && request.getReceiverId().equals(friendUserId)
                    && MatchRequest.STATUS_PENDING.equals(request.getStatus());

            if (pending) {
                binding.matchRequestButton.setText(getString(R.string.match_request_cancel));
                binding.matchRequestButton.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red)));
            } else {
                binding.matchRequestButton.setText(getString(R.string.match_request_send));
                binding.matchRequestButton.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.field_background)));
            }
        });
    }

    private void onMatchRequestButtonClick() {
        MatchRequest current = matchRequestViewModel.getOutgoingRequest().getValue();
        boolean pendingToThis = current != null
                && friendUserId != null
                && current.getReceiverId().equals(friendUserId)
                && MatchRequest.STATUS_PENDING.equals(current.getStatus());

        if (pendingToThis) {
            matchRequestViewModel.cancelRequest();
        } else {
            matchRequestViewModel.sendRequest(
                    friendUserId,
                    friendUsername != null ? friendUsername : "");
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
        friendUsername = profile.getUsername();
        binding.matchRequestButton.setEnabled(true);

        binding.usernameText.setText(profile.getUsername());
        binding.starsText.setText(String.valueOf(profile.getNumStars()));
        binding.leagueText.setText(profile.getLeague());
        binding.rankText.setText("#" + profile.getRank());

        String league = profile.getLeague() != null ? profile.getLeague().toLowerCase() : "";
        int borderResId = getResources().getIdentifier("league_border_" + league, "drawable",
                requireContext().getPackageName());
        binding.leagueBorder.setImageResource(borderResId != 0
                ? borderResId : R.drawable.circular_profile_background);

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
