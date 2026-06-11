package com.example.slagalica.presentation.fragments.match;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameSkockoBinding;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;
import com.example.slagalica.domain.service.match.SkockoService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.SkockoRepository;
import com.example.slagalica.repository.impl.stub.StubSkockoRepository;

public class SkockoFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private FragmentGameSkockoBinding binding;

    private SkockoRepository skockoRepository;
    private SkockoService skockoService;
    private CountDownTimer roundTimer;

    public SkockoFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameSkockoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        ((AppActivity) requireActivity()).setToolbarTitle("Skočko");

        skockoRepository = new StubSkockoRepository();
        skockoService = new SkockoService(skockoRepository.getRounds());

        setupSymbolButtons();
        renderWholeScreen();
        startRoundTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (matchViewModel != null) {
            matchViewModel.setGameActive(false);
        }
        if (roundTimer != null) {
            roundTimer.cancel();
        }
        binding = null;
    }

    private void setupSymbolButtons() {
        binding.symStar.setOnClickListener(v -> handleAppendSymbol("★"));
        binding.symSpade.setOnClickListener(v -> handleAppendSymbol("♠"));
        binding.symClub.setOnClickListener(v -> handleAppendSymbol("♣"));
        binding.symHeart.setOnClickListener(v -> handleAppendSymbol("♥"));
        binding.symDiamond.setOnClickListener(v -> handleAppendSymbol("♦"));
        binding.symExplosion.setOnClickListener(v -> handleAppendSymbol("💥"));

        binding.btnSkockoDelete.setOnClickListener(v -> {
            if (skockoService.getCurrentRound().getGameState().isRoundFinished()) {
                return;
            }
            skockoService.removeLastSymbol();
            renderWholeScreen();
        });

        binding.btnSkockoSubmit.setOnClickListener(v -> {
            SkockoTabla round = skockoService.getCurrentRound();

            if (round.getGameState().isRoundFinished()) {
                if (skockoService.canAdvanceRound()) {
                    if (roundTimer != null) {
                        roundTimer.cancel();
                    }

                    SkockoService.ActionResult result = skockoService.advanceToNextRound();
                    Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    renderWholeScreen();
                    startRoundTimer();
                } else {
                    Toast.makeText(requireContext(), "Igra je završena", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            SkockoService.ActionResult result = skockoService.submitCurrentRow();
            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

            renderWholeScreen();

            if (result.isBonusActivated()) {
                if (roundTimer != null) {
                    roundTimer.cancel();
                }
                startRoundTimer();
            } else if (result.isRoundFinished()) {
                if (roundTimer != null) {
                    roundTimer.cancel();
                }
            }
        });
    }

    private void handleAppendSymbol(String symbol) {
        if (!skockoService.canEditCurrentAttempt()) {
            return;
        }

        skockoService.appendSymbol(symbol);
        renderWholeScreen();
    }

    private void renderWholeScreen() {
        renderInfo();
        renderRegularAttempts();
        renderBonusAttempt();
        renderFinalSolution();
        renderActionButtons();
    }

    private void renderInfo() {
        binding.tvSkockoGameState.setText(
                skockoService.getRoundInfoText()
                        + "\n"
                        + skockoService.getScoreText()
                        + "\n"
                        + skockoService.getStatusText()
        );
    }

    private void renderRegularAttempts() {
        binding.skockoInputContainer.removeAllViews();
        binding.rightFeedbackRows.removeAllViews();

        SkockoTabla round = skockoService.getCurrentRound();

        for (int i = 0; i < 6; i++) {
            SkockoPokusaj attempt = round.getAttempts().get(i);
            binding.skockoInputContainer.addView(createGuessRow(attempt, i, round));
            binding.rightFeedbackRows.addView(createFeedbackRow(attempt));
        }
    }

    private void renderBonusAttempt() {
        binding.rowPlayerTwoAttempt.removeAllViews();

        SkockoTabla round = skockoService.getCurrentRound();
        binding.rowPlayerTwoAttempt.addView(
                createLowerGuessRow(round.getBonusAttempt(), round.isBonusAttemptActive(), round)
        );
    }

    private void renderFinalSolution() {
        binding.rowFinalSolution.removeAllViews();

        SkockoTabla round = skockoService.getCurrentRound();
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        for (int i = 0; i < 4; i++) {
            TextView cell = createSymbolCell(dp(44), dp(52));

            if (round.getGameState().isRoundFinished()) {
                String symbol = round.getSecretCombination().get(i);
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_active);
                cell.setText(symbol);
                applySymbolColor(cell, symbol);
            } else {
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_empty);
                cell.setText("");
            }

            row.addView(cell);
        }

        binding.rowFinalSolution.addView(row);
    }

    private void renderActionButtons() {
        SkockoTabla round = skockoService.getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            binding.btnSkockoDelete.setEnabled(false);

            if (skockoService.canAdvanceRound()) {
                binding.btnSkockoSubmit.setEnabled(true);
                binding.btnSkockoSubmit.setText("Sledeća runda");
            } else {
                binding.btnSkockoSubmit.setEnabled(false);
                binding.btnSkockoSubmit.setText("Kraj");
            }
        } else {
            binding.btnSkockoDelete.setEnabled(true);
            binding.btnSkockoSubmit.setEnabled(true);
            binding.btnSkockoSubmit.setText("OK");
        }
    }

    private void startRoundTimer() {
        SkockoTabla round = skockoService.getCurrentRound();

        if (roundTimer != null) {
            roundTimer.cancel();
        }

        roundTimer = new CountDownTimer(round.getGameState().getRemainingSeconds() * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                round.getGameState().setRemainingSeconds((int) (millisUntilFinished / 1000));
                renderInfo();
            }

            @Override
            public void onFinish() {
                round.getGameState().setRemainingSeconds(0);
                SkockoService.ActionResult result = skockoService.onTimeExpired();
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                renderWholeScreen();

                if (result.isBonusActivated()) {
                    startRoundTimer();
                }
            }
        }.start();
    }

    private LinearLayout createGuessRow(SkockoPokusaj attempt, int rowIndex, SkockoTabla round) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        row.setLayoutParams(rowParams);

        boolean isActiveRow = !round.isBonusAttemptActive()
                && !round.getGameState().isRoundFinished()
                && rowIndex == round.getCurrentRow()
                && !attempt.isSubmitted();

        for (int i = 0; i < 4; i++) {
            TextView guessView = createSymbolCell(0, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
            );
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            guessView.setLayoutParams(params);

            String symbol = attempt.getGuess().get(i).getSymbol();

            if (symbol == null || symbol.isEmpty()) {
                guessView.setText("");
                guessView.setBackgroundResource(
                        isActiveRow ? R.drawable.bg_skocko_guess_active : R.drawable.bg_skocko_guess_empty
                );
            } else {
                guessView.setText(symbol);
                guessView.setBackgroundResource(R.drawable.game_field_background);
                applySymbolColor(guessView, symbol);
            }

            row.addView(guessView);
        }

        return row;
    }

    private LinearLayout createLowerGuessRow(SkockoPokusaj attempt, boolean isActiveBonus, SkockoTabla round) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        for (int i = 0; i < 4; i++) {
            TextView guessView = createSymbolCell(dp(44), dp(52));

            String symbol = attempt.getGuess().get(i).getSymbol();

            if (symbol == null || symbol.isEmpty()) {
                guessView.setText("");
                guessView.setBackgroundResource(
                        isActiveBonus && !round.getGameState().isRoundFinished()
                                ? R.drawable.bg_skocko_guess_active
                                : R.drawable.bg_skocko_guess_empty
                );
            } else {
                guessView.setText(symbol);
                guessView.setBackgroundResource(R.drawable.game_field_background);
                applySymbolColor(guessView, symbol);
            }

            row.addView(guessView);
        }

        return row;
    }

    private LinearLayout createFeedbackRow(SkockoPokusaj attempt) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1,
                1f
        );
        row.setLayoutParams(rowParams);

        for (int i = 0; i < 4; i++) {
            View dot = new View(requireContext());

            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(0, dp(16), 1f);
            dotParams.setMargins(dp(4), dp(2), dp(4), dp(2));
            dot.setLayoutParams(dotParams);

            String state = attempt.getFeedback().get(i);

            if ("EXACT".equals(state)) {
                dot.setBackgroundResource(R.drawable.bg_skocko_feedback_exact);
            } else if ("PARTIAL".equals(state)) {
                dot.setBackgroundResource(R.drawable.bg_skocko_feedback_partial);
            } else {
                dot.setBackgroundResource(R.drawable.bg_skocko_feedback_empty);
            }

            row.addView(dot);
        }

        return row;
    }

    private TextView createSymbolCell(int width, int height) {
        TextView cell = new TextView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        cell.setLayoutParams(params);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        return cell;
    }

    private void applySymbolColor(TextView textView, String symbol) {
        if ("♥".equals(symbol) || "♦".equals(symbol)) {
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
    }


    private int dp(int value) {
        Resources resources = requireContext().getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                resources.getDisplayMetrics()
        );
    }
}