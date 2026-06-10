package com.example.slagalica.presentation.fragments.match;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.SkockoRepository;
import com.example.slagalica.repository.impl.stub.StubSkockoRepository;

import java.util.ArrayList;
import java.util.List;

public class SkockoFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private FragmentGameSkockoBinding binding;

    private SkockoRepository skockoRepository;
    private SkockoService skockoService;

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

        TextView toolbarTitle = requireActivity().findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Skočko");
        }

        skockoRepository = new StubSkockoRepository();
        SkockoTabla skockoTabla = skockoRepository.getSkockoTabla();
        skockoService = new SkockoService(skockoTabla);

        setupSymbolButtons();
        renderAll();
        updateGameState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (matchViewModel != null) {
            matchViewModel.setGameActive(false);
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
            skockoService.removeLastSymbol();
            renderAll();
        });

        binding.btnSkockoSubmit.setOnClickListener(v -> {
            SkockoService.SubmitResult result = skockoService.submitCurrentRow();

            if (!result.isSuccess()) {
                if (result.getMessage() != null) {
                    Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (result.getMessage() != null) {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
            }

            renderAll();
            updateGameState();
        });
    }

    private void handleAppendSymbol(String symbol) {
        if (!skockoService.canAppendMoreSymbols()) {
            Toast.makeText(requireContext(), "Red je već popunjen", Toast.LENGTH_SHORT).show();
            return;
        }

        skockoService.appendSymbol(symbol);
        renderAll();
    }

    private void renderAll() {
        renderUpperSection();
        renderLowerPlayerTwoAttempt();
        renderLowerFinalSolution();
    }

    private void renderUpperSection() {
        binding.skockoInputContainer.removeAllViews();
        binding.rightFeedbackRows.removeAllViews();

        SkockoTabla tabla = skockoService.getSkockoTabla();

        for (int i = 0; i < 6; i++) {
            SkockoPokusaj attempt = tabla.getAttempts().get(i);
            binding.skockoInputContainer.addView(createGuessRow(attempt, i));
            binding.rightFeedbackRows.addView(createFeedbackRow(attempt));
        }
    }

    private void renderLowerPlayerTwoAttempt() {
        binding.rowPlayerTwoAttempt.removeAllViews();
        binding.rowPlayerTwoAttempt.addView(
                createLowerGuessRow(skockoService.getSkockoTabla().getAttempts().get(6), 6)
        );
    }

    private void renderLowerFinalSolution() {
        binding.rowFinalSolution.removeAllViews();

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(rowParams);

        SkockoTabla tabla = skockoService.getSkockoTabla();

        for (int i = 0; i < 4; i++) {
            TextView cell = new TextView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(52));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            cell.setLayoutParams(params);
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            cell.setTypeface(null, Typeface.BOLD);

            if (tabla.isFinished()) {
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_active);
                String symbol = tabla.getSecretCombination().get(i);
                cell.setText(symbol);
                applySymbolColor(cell, symbol);
            } else {
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_empty);
                cell.setText("");
                cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }

            row.addView(cell);
        }

        binding.rowFinalSolution.addView(row);
    }

    private LinearLayout createGuessRow(SkockoPokusaj attempt, int rowIndex) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        row.setLayoutParams(rowParams);

        SkockoTabla tabla = skockoService.getSkockoTabla();

        boolean isActiveRow = rowIndex == tabla.getCurrentRow()
                && !attempt.isSubmitted()
                && !tabla.isFinished();

        for (int i = 0; i < 4; i++) {
            TextView guessView = new TextView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
            );
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            guessView.setLayoutParams(params);
            guessView.setGravity(Gravity.CENTER);
            guessView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            guessView.setTypeface(null, Typeface.BOLD);

            String symbol = attempt.getGuess().get(i).getSymbol();

            if (symbol == null || symbol.isEmpty()) {
                guessView.setText("");
                if (isActiveRow) {
                    guessView.setBackgroundResource(R.drawable.bg_skocko_guess_active);
                } else {
                    guessView.setBackgroundResource(R.drawable.bg_skocko_guess_empty);
                }
                guessView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            } else {
                guessView.setText(symbol);
                guessView.setBackgroundResource(R.drawable.game_field_background);
                applySymbolColor(guessView, symbol);
            }

            row.addView(guessView);
        }

        return row;
    }

    private LinearLayout createLowerGuessRow(SkockoPokusaj attempt, int rowIndex) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(rowParams);

        SkockoTabla tabla = skockoService.getSkockoTabla();

        boolean isActiveRow = rowIndex == tabla.getCurrentRow()
                && !attempt.isSubmitted()
                && !tabla.isFinished();

        for (int i = 0; i < 4; i++) {
            TextView guessView = new TextView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(52));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            guessView.setLayoutParams(params);
            guessView.setGravity(Gravity.CENTER);
            guessView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            guessView.setTypeface(null, Typeface.BOLD);

            String symbol = attempt.getGuess().get(i).getSymbol();

            if (symbol == null || symbol.isEmpty()) {
                guessView.setText("");
                if (isActiveRow) {
                    guessView.setBackgroundResource(R.drawable.bg_skocko_guess_active);
                } else {
                    guessView.setBackgroundResource(R.drawable.bg_skocko_guess_empty);
                }
                guessView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
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

    private void applySymbolColor(TextView textView, String symbol) {
        if ("♥".equals(symbol) || "♦".equals(symbol)) {
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
    }

    private void updateGameState() {
        binding.tvSkockoGameState.setText(skockoService.getGameStateText());
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