package com.example.slagalica.presentation.fragments.match;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.R;
import com.example.slagalica.domain.model.match.games.SkockoPolje;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.SkockoRepository;
import com.example.slagalica.repository.impl.stub.StubSkockoRepository;

import java.util.ArrayList;
import java.util.List;

public class SkockoFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private SkockoRepository skockoRepository;
    private SkockoTabla skockoTabla;

    private TextView tvGameState;

    private LinearLayout skockoInputContainer;
    private LinearLayout rowPlayerTwoAttempt;
    private LinearLayout rowFinalSolution;
    private LinearLayout rightFeedbackRows;

    private TextView symStar;
    private TextView symSpade;
    private TextView symClub;
    private TextView symHeart;
    private TextView symDiamond;
    private TextView symQuestion;

    private Button btnDelete;
    private Button btnSubmit;

    public SkockoFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_skocko, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        skockoRepository = new StubSkockoRepository();
        skockoTabla = skockoRepository.getSkockoTabla();

        bindViews(view);
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
    }

    private void bindViews(View view) {
        tvGameState = view.findViewById(R.id.tvSkockoGameState);

        skockoInputContainer = view.findViewById(R.id.skockoInputContainer);
        rowPlayerTwoAttempt = view.findViewById(R.id.rowPlayerTwoAttempt);
        rowFinalSolution = view.findViewById(R.id.rowFinalSolution);
        rightFeedbackRows = view.findViewById(R.id.rightFeedbackRows);

        symStar = view.findViewById(R.id.symStar);
        symSpade = view.findViewById(R.id.symSpade);
        symClub = view.findViewById(R.id.symClub);
        symHeart = view.findViewById(R.id.symHeart);
        symDiamond = view.findViewById(R.id.symDiamond);
        symQuestion = view.findViewById(R.id.symExplosion);

        btnDelete = view.findViewById(R.id.btnSkockoDelete);
        btnSubmit = view.findViewById(R.id.btnSkockoSubmit);
    }

    private void setupSymbolButtons() {
        symStar.setOnClickListener(v -> appendSymbol("★"));
        symSpade.setOnClickListener(v -> appendSymbol("♠"));
        symClub.setOnClickListener(v -> appendSymbol("♣"));
        symHeart.setOnClickListener(v -> appendSymbol("♥"));
        symDiamond.setOnClickListener(v -> appendSymbol("♦"));
        symQuestion.setOnClickListener(v -> appendSymbol("💥"));

        btnDelete.setOnClickListener(v -> removeLastSymbol());
        btnSubmit.setOnClickListener(v -> submitCurrentRow());
    }

    private void appendSymbol(String symbol) {
        if (skockoTabla.isFinished()) return;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());
        if (currentAttempt.isSubmitted()) return;

        for (SkockoPolje polje : currentAttempt.getGuess()) {
            if (polje.isEmpty()) {
                polje.setSymbol(symbol);
                renderAll();
                return;
            }
        }

        Toast.makeText(requireContext(), "Red je već popunjen", Toast.LENGTH_SHORT).show();
    }

    private void removeLastSymbol() {
        if (skockoTabla.isFinished()) return;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());
        if (currentAttempt.isSubmitted()) return;

        for (int i = currentAttempt.getGuess().size() - 1; i >= 0; i--) {
            SkockoPolje polje = currentAttempt.getGuess().get(i);
            if (!polje.isEmpty()) {
                polje.setSymbol("");
                renderAll();
                return;
            }
        }
    }

    private void submitCurrentRow() {
        if (skockoTabla.isFinished()) return;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());

        for (SkockoPolje polje : currentAttempt.getGuess()) {
            if (polje.isEmpty()) {
                Toast.makeText(requireContext(), "Popuni sva 4 polja", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        evaluateAttempt(currentAttempt);
        currentAttempt.setSubmitted(true);

        boolean solved = isExactAttempt(currentAttempt);

        if (solved) {
            skockoTabla.setSolved(true);
            skockoTabla.setFinished(true);
            if (skockoTabla.getCurrentPlayer() == 1) {
                skockoTabla.setWinnerName(skockoTabla.getPlayerOneName());
            } else {
                skockoTabla.setWinnerName(skockoTabla.getPlayerTwoName());
            }
        } else {
            if (skockoTabla.getCurrentPlayer() == 1) {
                if (skockoTabla.getCurrentRow() == 5) {
                    skockoTabla.setCurrentPlayer(2);
                    skockoTabla.setCurrentRow(6);
                    Toast.makeText(requireContext(), "Sada igra Igrač 2", Toast.LENGTH_SHORT).show();
                } else {
                    skockoTabla.setCurrentRow(skockoTabla.getCurrentRow() + 1);
                }
            } else {
                skockoTabla.setFinished(true);
            }
        }

        renderAll();
        updateGameState();
    }

    private boolean isExactAttempt(SkockoPokusaj attempt) {
        for (int i = 0; i < 4; i++) {
            if (!attempt.getGuess().get(i).getSymbol().equals(skockoTabla.getSecretCombination().get(i))) {
                return false;
            }
        }
        return true;
    }

    private void evaluateAttempt(SkockoPokusaj attempt) {
        List<String> secret = new ArrayList<>(skockoTabla.getSecretCombination());
        List<String> guess = new ArrayList<>();

        for (SkockoPolje polje : attempt.getGuess()) {
            guess.add(polje.getSymbol());
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (guess.get(i).equals(secret.get(i))) {
                result.add("EXACT");
                guess.set(i, null);
                secret.set(i, null);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (guess.get(i) != null) {
                int index = secret.indexOf(guess.get(i));
                if (index != -1) {
                    result.add("PARTIAL");
                    secret.set(index, null);
                }
            }
        }

        while (result.size() < 4) {
            result.add("EMPTY");
        }

        for (int i = 0; i < 4; i++) {
            attempt.getFeedback().set(i, result.get(i));
        }
    }

    private void renderAll() {
        renderUpperSection();
        renderLowerPlayerTwoAttempt();
        renderLowerFinalSolution();
    }

    private void renderUpperSection() {
        skockoInputContainer.removeAllViews();
        rightFeedbackRows.removeAllViews();

        for (int i = 0; i < 6; i++) {
            SkockoPokusaj attempt = skockoTabla.getAttempts().get(i);
            skockoInputContainer.addView(createGuessRow(attempt, i));
            rightFeedbackRows.addView(createFeedbackRow(attempt));
        }
    }

    private void renderLowerPlayerTwoAttempt() {
        rowPlayerTwoAttempt.removeAllViews();
        rowPlayerTwoAttempt.addView(createLowerGuessRow(skockoTabla.getAttempts().get(6), 6));
    }

    private void renderLowerFinalSolution() {
        rowFinalSolution.removeAllViews();

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(rowParams);

        for (int i = 0; i < 4; i++) {
            TextView cell = new TextView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(52));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            cell.setLayoutParams(params);
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            cell.setTypeface(null, Typeface.BOLD);
            if (skockoTabla.isFinished()) {
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_active);
            } else {
                cell.setBackgroundResource(R.drawable.bg_skocko_guess_empty);
            }

            if (skockoTabla.isFinished()) {
                String symbol = skockoTabla.getSecretCombination().get(i);
                cell.setText(symbol);
                applySymbolColor(cell, symbol);
            } else {
                cell.setText("");
                cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }

            row.addView(cell);
        }

        rowFinalSolution.addView(row);
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

        boolean isActiveRow = rowIndex == skockoTabla.getCurrentRow()
                && !attempt.isSubmitted()
                && !skockoTabla.isFinished();

        for (int i = 0; i < 4; i++) {
            TextView guessView = new TextView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
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

        boolean isActiveRow = rowIndex == skockoTabla.getCurrentRow()
                && !attempt.isSubmitted()
                && !skockoTabla.isFinished();

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
        if (skockoTabla.isSolved()) {
            tvGameState.setText("Pobednik: " + skockoTabla.getWinnerName());
        } else if (skockoTabla.isFinished()) {
            tvGameState.setText("Niko nije pogodio | Kombinacija prikazana");
        } else if (skockoTabla.getCurrentPlayer() == 1) {
            tvGameState.setText("Na potezu: " + skockoTabla.getPlayerOneName()
                    + " | pokušaj " + (skockoTabla.getCurrentRow() + 1) + " / 6");
        } else {
            tvGameState.setText("Na potezu: " + skockoTabla.getPlayerTwoName() + " | pokušaj 1 / 1");
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