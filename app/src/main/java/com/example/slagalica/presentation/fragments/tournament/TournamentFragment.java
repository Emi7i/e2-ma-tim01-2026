package com.example.slagalica.presentation.fragments.tournament;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slagalica.databinding.FragmentTournamentBinding;
import com.example.slagalica.domain.model.tournament.TournamentMatch;
import com.example.slagalica.domain.model.tournament.TournamentMatchStatus;
import com.example.slagalica.domain.model.tournament.TournamentRound;
import com.example.slagalica.domain.model.tournament.TournamentSession;
import com.example.slagalica.domain.model.tournament.TournamentStatus;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.adapters.TournamentPlayerAdapter;
import com.example.slagalica.presentation.viewmodels.TournamentViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TournamentFragment extends Fragment {

    private FragmentTournamentBinding binding;
    private TournamentViewModel viewModel;
    private TournamentPlayerAdapter adapter;
    private boolean waitingInQueue = false;

    public TournamentFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentTournamentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ((AppActivity) requireActivity()).setToolbarTitle("Turnir");

        viewModel = new ViewModelProvider(this).get(TournamentViewModel.class);
        adapter = new TournamentPlayerAdapter();

        binding.playersRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.playersRecycler.setAdapter(adapter);

        setupListeners();
        observeViewModel();

        viewModel.loadActiveTournament();
    }

    private void setupListeners() {
        binding.joinTournamentButton.setOnClickListener(v -> viewModel.joinTournament());
        binding.demoTournamentButton.setOnClickListener(v -> viewModel.createDemoTournament());
        binding.cancelQueueButton.setOnClickListener(v -> viewModel.cancelQueue());
        binding.refreshButton.setOnClickListener(v -> viewModel.loadActiveTournament());

        binding.simulateWinButton.setOnClickListener(v -> {
            playResultAnimation(true);
            viewModel.simulateWinCurrentMatch();
        });
    }

    private void observeViewModel() {
        viewModel.getTournament().observe(getViewLifecycleOwner(), this::renderTournament);
        viewModel.getNextMatch().observe(getViewLifecycleOwner(), this::renderNextMatch);

        viewModel.getWaitingInQueue().observe(getViewLifecycleOwner(), waiting -> {
            waitingInQueue = Boolean.TRUE.equals(waiting);

            if (waitingInQueue) {
                binding.statusText.setText("U redu ste za turnir. Čekaju se još igrači.");
                binding.cancelQueueButton.setVisibility(View.VISIBLE);
                binding.joinTournamentButton.setText("Čekaju se igrači...");
                binding.joinTournamentButton.setEnabled(false);
                binding.playersCard.setVisibility(View.GONE);
                binding.bracketCard.setVisibility(View.GONE);
            } else {
                binding.cancelQueueButton.setVisibility(View.GONE);
                binding.joinTournamentButton.setText("Započni turnir");
                binding.joinTournamentButton.setEnabled(true);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.joinTournamentButton.setEnabled(!isLoading);
            binding.demoTournamentButton.setEnabled(!isLoading);
            binding.simulateWinButton.setEnabled(!isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getInfo().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTournament(TournamentSession tournament) {
        if (tournament == null) {
            if (!waitingInQueue) {
                binding.statusText.setText("Nema aktivnog turnira.");
                binding.joinTournamentButton.setText("Započni turnir");
                binding.joinTournamentButton.setEnabled(true);
                binding.cancelQueueButton.setVisibility(View.GONE);
            }

            binding.bracketCard.setVisibility(View.GONE);
            binding.playersCard.setVisibility(View.GONE);
            binding.cancelQueueButton.setVisibility(View.VISIBLE);
            return;
        }

        waitingInQueue = false;
        binding.cancelQueueButton.setVisibility(View.GONE);
        binding.joinTournamentButton.setText("Turnir je u toku");
        binding.joinTournamentButton.setEnabled(false);

        TournamentStatus status = tournament.getStatusEnum();
        binding.statusText.setText(statusLabel(status));
        binding.playersCard.setVisibility(View.VISIBLE);
        binding.bracketCard.setVisibility(View.VISIBLE);

        adapter.submitList(tournament.getParticipants());

        StringBuilder bracket = new StringBuilder();
        for (TournamentMatch match : tournament.getMatches()) {
            bracket.append(matchLabel(match)).append("\n");
        }
        binding.bracketText.setText(bracket.toString().trim());

        if (status == TournamentStatus.FINISHED) {
            binding.simulateWinButton.setVisibility(View.GONE);
            binding.nextMatchText.setText("Turnir je završen. Pobednik: " + tournament.getWinnerId());
        }
    }

    private void renderNextMatch(TournamentMatch match) {
        if (match == null) {
            binding.nextMatchText.setText("Trenutno nema turnirske partije za vas.");
            binding.simulateWinButton.setVisibility(View.GONE);
            return;
        }

        binding.nextMatchText.setText(
                (match.getRoundEnum() == TournamentRound.SEMIFINAL ? "Polufinale" : "Finale")
                        + ": "
                        + safe(match.getPlayer1Username())
                        + " vs "
                        + safe(match.getPlayer2Username())
        );

        boolean playable = match.getStatusEnum() == TournamentMatchStatus.WAITING
                || match.getStatusEnum() == TournamentMatchStatus.IN_PROGRESS;
        binding.simulateWinButton.setVisibility(playable ? View.VISIBLE : View.GONE);
    }

    private String statusLabel(TournamentStatus status) {
        switch (status) {
            case WAITING_FOR_PLAYERS:
                return "Čekaju se 4 igrača.";
            case SEMIFINALS_READY:
                return "Turnir je spreman. Igraju se dva polufinala.";
            case SEMIFINALS_IN_PROGRESS:
                return "Polufinala su u toku.";
            case FINAL_READY:
                return "Finale je spremno.";
            case FINAL_IN_PROGRESS:
                return "Finale je u toku.";
            case FINISHED:
                return "Turnir je završen.";
            case CANCELLED:
                return "Turnir je otkazan.";
            default:
                return "Nepoznat status turnira.";
        }
    }

    private String matchLabel(TournamentMatch match) {
        String round = match.getRoundEnum() == TournamentRound.SEMIFINAL
                ? "Polufinale " + match.getMatchIndex()
                : "Finale";

        String result = "";
        if (match.getStatusEnum() == TournamentMatchStatus.FINISHED) {
            result = " → pobednik: " + winnerNameFor(match);
        }

        return round
                + ": "
                + safe(match.getPlayer1Username())
                + " vs "
                + safe(match.getPlayer2Username())
                + result;
    }

    private String winnerNameFor(TournamentMatch match) {
        String winnerId = match.getWinnerId();

        if (winnerId == null) {
            return "nepoznat";
        }

        if (winnerId.equals(match.getPlayer1Id())) {
            return safe(match.getPlayer1Username());
        }

        if (winnerId.equals(match.getPlayer2Id())) {
            return safe(match.getPlayer2Username());
        }

        return winnerId;
    }

    private void playResultAnimation(boolean win) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                binding.bracketCard,
                View.SCALE_X,
                1f,
                1.04f,
                1f
        );
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                binding.bracketCard,
                View.SCALE_Y,
                1f,
                1.04f,
                1f
        );
        ObjectAnimator alpha = ObjectAnimator.ofFloat(
                binding.bracketCard,
                View.ALPHA,
                1f,
                0.75f,
                1f
        );

        AnimatorSet set = new AnimatorSet();
        set.setDuration(550L);
        set.playTogether(scaleX, scaleY, alpha);
        set.start();
    }

    private String safe(String value) {
        return value == null ? "Igrač" : value;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
