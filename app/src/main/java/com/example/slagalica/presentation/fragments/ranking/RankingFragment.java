package com.example.slagalica.presentation.fragments.ranking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slagalica.databinding.FragmentRankingBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.ranking.RankingCycleType;
import com.example.slagalica.domain.service.ranking.RankingCycleUtils;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.adapters.RankingAdapter;
import com.example.slagalica.presentation.viewmodels.RankingViewModel;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RankingFragment extends Fragment {

    private static final long REFRESH_INTERVAL_MILLIS =
            2L * 60L * 1000L;

    private FragmentRankingBinding binding;
    private RankingViewModel viewModel;
    private RankingAdapter adapter;

    private RankingCycleType selectedType =
            RankingCycleType.WEEKLY;

    private final Handler refreshHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) {
                return;
            }

            viewModel.loadLeaderboard(selectedType);
            refreshHandler.postDelayed(
                    this,
                    REFRESH_INTERVAL_MILLIS
            );
        }
    };

    @Inject
    SessionManager sessionManager;

    public RankingFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRankingBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ((AppActivity) requireActivity())
                .setToolbarTitle("Rang lista");

        viewModel = new ViewModelProvider(requireActivity())
                .get(RankingViewModel.class);

        adapter = new RankingAdapter(
                sessionManager.getCurrentUserId()
        );

        binding.rankingRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.rankingRecycler.setAdapter(adapter);

        setupTabs();
        observeViewModel();

        binding.refreshButton.setOnClickListener(v ->
                viewModel.loadLeaderboard(selectedType)
        );

        viewModel.loadLeaderboard(selectedType);
        viewModel.finalizeExpiredCyclesAndLoadReward(
                sessionManager.getCurrentUserId()
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(
                refreshRunnable,
                REFRESH_INTERVAL_MILLIS
        );
    }

    @Override
    public void onStop() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        refreshHandler.removeCallbacks(refreshRunnable);
        binding.rankingRecycler.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }

    private void setupTabs() {
        binding.rankingTabs.addTab(
                binding.rankingTabs
                        .newTab()
                        .setText("Nedeljna")
        );

        binding.rankingTabs.addTab(
                binding.rankingTabs
                        .newTab()
                        .setText("Mesečna")
        );

        binding.rankingTabs.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(
                            TabLayout.Tab tab
                    ) {
                        selectedType =
                                tab.getPosition() == 0
                                        ? RankingCycleType.WEEKLY
                                        : RankingCycleType.MONTHLY;

                        viewModel.loadLeaderboard(selectedType);
                    }

                    @Override
                    public void onTabUnselected(
                            TabLayout.Tab tab
                    ) {
                    }

                    @Override
                    public void onTabReselected(
                            TabLayout.Tab tab
                    ) {
                        viewModel.loadLeaderboard(selectedType);
                    }
                }
        );
    }

    private void observeViewModel() {
        viewModel.getEntries().observe(
                getViewLifecycleOwner(),
                entries -> {
                    adapter.submitList(entries);

                    boolean empty =
                            entries == null || entries.isEmpty();

                    binding.rankingEmptyText.setVisibility(
                            empty ? View.VISIBLE : View.GONE
                    );
                    binding.rankingRecycler.setVisibility(
                            empty ? View.GONE : View.VISIBLE
                    );
                }
        );

        viewModel.getDisplayedCycle().observe(
                getViewLifecycleOwner(),
                cycle -> {
                    if (cycle == null) {
                        return;
                    }

                    binding.cycleRangeText.setText(
                            "Ciklus: "
                                    + RankingCycleUtils
                                    .formatRange(cycle)
                    );
                }
        );

        viewModel.getLoading().observe(
                getViewLifecycleOwner(),
                loading -> binding.rankingProgress.setVisibility(
                        Boolean.TRUE.equals(loading)
                                ? View.VISIBLE
                                : View.GONE
                )
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (message != null
                            && !message.trim().isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
