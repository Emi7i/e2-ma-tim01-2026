package com.example.slagalica.presentation.fragments.match;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameSpojniceBinding;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.presentation.viewmodels.SpojniceViewModel;
import com.example.slagalica.presentation.views.GameHeaderView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class SpojniceFragment extends Fragment {

    private SpojniceViewModel viewModel;
    private MatchViewModel matchViewModel;
    private FragmentGameSpojniceBinding binding;

    private List<Button> leftButtons;
    private List<Button> rightButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameSpojniceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SpojniceViewModel.class);
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        initializeButtons();
        setupHeader();
        observeViewModel();
        setupListeners();
    }

    private void initializeButtons() {
        leftButtons = Arrays.asList(
                binding.leftCard1, binding.leftCard2, binding.leftCard3,
                binding.leftCard4, binding.leftCard5
        );
        rightButtons = Arrays.asList(
                binding.rightCard1, binding.rightCard2, binding.rightCard3,
                binding.rightCard4, binding.rightCard5
        );
        
        for (Button btn : leftButtons) btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        for (Button btn : rightButtons) btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
    }

    private void setupHeader() {
        GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setVisibility(View.VISIBLE);
            updateHeaderScores(matchViewModel.getPlayer1Score().getValue(), matchViewModel.getPlayer2Score().getValue());
        }
        
        android.widget.TextView toolbarTitle = requireActivity().findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Spojnice");
        }
    }

    private void observeViewModel() {
        viewModel.getCurrentData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                binding.gameTitle.setText(data.getTitle());
            }
        });

        viewModel.getLeftColumn().observe(getViewLifecycleOwner(), terms -> {
            for (int i = 0; i < leftButtons.size(); i++) {
                if (i < terms.size()) {
                    leftButtons.get(i).setText(terms.get(i));
                    leftButtons.get(i).setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getRightColumn().observe(getViewLifecycleOwner(), terms -> {
            for (int i = 0; i < rightButtons.size(); i++) {
                if (i < terms.size()) {
                    rightButtons.get(i).setText(terms.get(i));
                    rightButtons.get(i).setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getCurrentLeftIndex().observe(getViewLifecycleOwner(), index -> {
            updateHighlights(index);
            updateMatchColors();
        });

        viewModel.getPlayer1Matches().observe(getViewLifecycleOwner(), matches -> updateMatchColors());
        viewModel.getPlayer2Matches().observe(getViewLifecycleOwner(), matches -> updateMatchColors());
        viewModel.getMissedMatches().observe(getViewLifecycleOwner(), matches -> updateMatchColors());

        viewModel.getP1ScoreDelta().observe(getViewLifecycleOwner(), delta -> {
            if (delta > 0) matchViewModel.updatePlayer1Score(delta);
        });

        viewModel.getP2ScoreDelta().observe(getViewLifecycleOwner(), delta -> {
            if (delta > 0) matchViewModel.updatePlayer2Score(delta);
        });

        matchViewModel.getPlayer1Score().observe(getViewLifecycleOwner(), p1 -> 
            updateHeaderScores(p1, matchViewModel.getPlayer2Score().getValue()));
        
        matchViewModel.getPlayer2Score().observe(getViewLifecycleOwner(), p2 -> 
            updateHeaderScores(matchViewModel.getPlayer1Score().getValue(), p2));

        viewModel.getTimeLeft().observe(getViewLifecycleOwner(), timeLeft -> {
            GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
            if (gameHeader != null) {
                gameHeader.setTimer(String.format("00:%02d", timeLeft));
            }
        });

        viewModel.isWaitingForNextPlayer().observe(getViewLifecycleOwner(), waiting -> {
            if (waiting) {
                showNextPlayerDialog();
            }
        });

        viewModel.isGameFinished().observe(getViewLifecycleOwner(), finished -> {
            if (finished) {
                Toast.makeText(requireContext(), "Spojnice završene!", Toast.LENGTH_LONG).show();
            }
        });
        
        viewModel.getCurrentPlayerTurn().observe(getViewLifecycleOwner(), turn -> {
            highlightScoreInHeader(turn);
            Integer index = viewModel.getCurrentLeftIndex().getValue();
            if (index != null) updateHighlights(index);
        });
    }

    private void updateHighlights(int activeIndex) {
        int focusColor = ContextCompat.getColor(requireContext(), R.color.blue_dark);

        for (int i = 0; i < leftButtons.size(); i++) {
            Button btn = leftButtons.get(i);
            if (i == activeIndex) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(focusColor));
            } else {
                if (!isLeftIndexMatched(i)) {
                    btn.setBackgroundTintList(null);
                    btn.setBackgroundResource(R.drawable.game_field_background);
                }
            }
        }
    }

    private boolean isLeftIndexMatched(int index) {
        Map<Integer, Integer> p1 = viewModel.getPlayer1Matches().getValue();
        Map<Integer, Integer> p2 = viewModel.getPlayer2Matches().getValue();
        Map<Integer, Integer> missed = viewModel.getMissedMatches().getValue();
        return (p1 != null && p1.containsKey(index)) || 
               (p2 != null && p2.containsKey(index)) ||
               (missed != null && missed.containsKey(index));
    }

    private void updateMatchColors() {
        Map<Integer, Integer> p1Matches = viewModel.getPlayer1Matches().getValue();
        Map<Integer, Integer> p2Matches = viewModel.getPlayer2Matches().getValue();
        Map<Integer, Integer> missed = viewModel.getMissedMatches().getValue();

        int blueColor = ContextCompat.getColor(requireContext(), R.color.blue_light);
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.field_border);
        int missedColor = ContextCompat.getColor(requireContext(), R.color.gray_light);

        // Reset all right buttons to default first
        for (Button btn : rightButtons) {
            btn.setBackgroundTintList(null);
            btn.setBackgroundResource(R.drawable.game_field_background);
        }

        if (missed != null) {
            for (Integer leftIdx : missed.keySet()) {
                leftButtons.get(leftIdx).setBackgroundTintList(android.content.res.ColorStateList.valueOf(missedColor));
                // Do not reveal correct pair pls
            }
        }

        if (p1Matches != null) {
            for (Map.Entry<Integer, Integer> match : p1Matches.entrySet()) {
                int leftIdx = match.getKey();
                int rightIdx = match.getValue();
                leftButtons.get(leftIdx).setBackgroundTintList(android.content.res.ColorStateList.valueOf(blueColor));
                rightButtons.get(rightIdx).setBackgroundTintList(android.content.res.ColorStateList.valueOf(blueColor));
            }
        }

        if (p2Matches != null) {
            for (Map.Entry<Integer, Integer> match : p2Matches.entrySet()) {
                int leftIdx = match.getKey();
                int rightIdx = match.getValue();
                leftButtons.get(leftIdx).setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                rightButtons.get(rightIdx).setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
            }
        }
    }

    private void highlightScoreInHeader(int turn) {
        GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            android.widget.TextView p1Stars = gameHeader.findViewById(R.id.player1Stars);
            android.widget.TextView p2Stars = gameHeader.findViewById(R.id.player2Stars);
            int blue = ContextCompat.getColor(requireContext(), R.color.blue_light);
            int purple = ContextCompat.getColor(requireContext(), R.color.field_border);
            int black = ContextCompat.getColor(requireContext(), R.color.black);

            if (p1Stars != null) p1Stars.setTextColor(turn == 1 ? blue : black);
            if (p2Stars != null) p2Stars.setTextColor(turn == 2 ? purple : black);
        }
    }

    private void updateHeaderScores(Integer p1, Integer p2) {
        GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
        if (gameHeader != null) {
            gameHeader.setStars(p1 != null ? p1 : 0, p2 != null ? p2 : 0);
        }
    }

    private void showNextPlayerDialog() {
        int currentPlayer = viewModel.getCurrentPlayerTurn().getValue();
        int nextPlayer = (currentPlayer == 1) ? 2 : 1;
        new AlertDialog.Builder(requireContext())
                .setTitle("Potez protivnika")
                .setMessage("Igrač " + nextPlayer + ", pokušaj da spojiš pojam koji je protivnik promašio!")
                .setPositiveButton("Spreman sam", (dialog, which) -> viewModel.startSecondPlayerTurn())
                .setCancelable(false)
                .show();
    }

    private void setupListeners() {
        for (int i = 0; i < rightButtons.size(); i++) {
            final int index = i;
            rightButtons.get(i).setOnClickListener(v -> viewModel.onRightTermSelected(index));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false);
        binding = null;
    }
}
