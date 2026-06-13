package com.example.slagalica.presentation.fragments.match;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameAsocijacijeBinding;
import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;
import com.example.slagalica.domain.service.match.AsocijacijeFactory;
import com.example.slagalica.domain.service.match.AsocijacijeService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.AsocijacijeContentRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AsocijacijeFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private FragmentGameAsocijacijeBinding binding;

    @Inject
    AsocijacijeContentRepository asocijacijeContentRepository;

    private AsocijacijeService asocijacijeService;

    private final List<LinearLayout> columnContainers = new ArrayList<>();
    private CountDownTimer roundTimer;

    public AsocijacijeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameAsocijacijeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        ((AppActivity) requireActivity()).setToolbarTitle("Asocijacije");

        bindViews();
        setupPassButton();
        setupFinalSolution();
        loadAsocijacijeFromFirestore();
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

    private void loadAsocijacijeFromFirestore() {
        asocijacijeContentRepository.getAllAsocijacije()
                .thenAccept(documents -> {
                    requireActivity().runOnUiThread(() -> {
                        AsocijacijeFactory factory = new AsocijacijeFactory();
                        List<Asocijacija> rounds = factory.createRounds(documents);

                        if (rounds == null || rounds.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Nema asocijacija u bazi",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        asocijacijeService = new AsocijacijeService(rounds);
                        renderWholeScreen();
                        startRoundTimer();
                    });
                })
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Greška pri učitavanju asocijacija iz baze",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }

    private void bindViews() {
        columnContainers.clear();
        columnContainers.add(binding.asocijacijeColumnA);
        columnContainers.add(binding.asocijacijeColumnB);
        columnContainers.add(binding.asocijacijeColumnC);
        columnContainers.add(binding.asocijacijeColumnD);
    }

    private void setupPassButton() {
        binding.btnAsocijacijePass.setOnClickListener(v -> {
            if (asocijacijeService == null) {
                return;
            }

            AsocijacijeService.ActionResult result = asocijacijeService.passTurn();

            if (!result.isSuccess()) {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

            if (asocijacijeService.canAdvanceRound() || !asocijacijeService.isMatchFinished()) {
                if (roundTimer != null) {
                    roundTimer.cancel();
                }

                if (!asocijacijeService.getCurrentRound().getGameState().isRoundFinished()) {
                    startRoundTimer();
                } else if (asocijacijeService.canAdvanceRound()) {
                    renderWholeScreen();
                    startRoundTimer();
                    return;
                }
            }

            renderWholeScreen();

            if (!asocijacijeService.getCurrentRound().getGameState().isRoundFinished()) {
                startRoundTimer();
            }
        });
    }

    private void setupFinalSolution() {
        binding.btnAsocijacijeFinalSubmit.setOnClickListener(v -> {
            if (asocijacijeService == null) {
                return;
            }

            AsocijacijeService.ActionResult result =
                    asocijacijeService.submitFinalSolution(binding.etAsocijacijeFinalSolution.getText().toString());

            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

            if (result.isSuccess()
                    && roundTimer != null
                    && asocijacijeService.getCurrentRound().getGameState().isRoundFinished()) {
                roundTimer.cancel();
            }

            renderWholeScreen();
        });
    }

    private void renderWholeScreen() {
        if (asocijacijeService == null) {
            return;
        }

        updateGameHeader();
        renderColumns();
        renderFinalState();
        renderPassButtonState();
    }

    private void updateGameHeader() {
        AppActivity activity = (AppActivity) requireActivity();
        Asocijacija round = asocijacijeService.getCurrentRound();

        activity.getBinding().gameHeader.setPlayerNames(
                round.getGameState().getPlayerOneName(),
                round.getGameState().getPlayerTwoName()
        );

        activity.getBinding().gameHeader.setActivePlayer(
                round.getGameState().getCurrentPlayer()
        );

        activity.getBinding().gameHeader.setScores(
                round.getGameState().getPlayerOneScore(),
                round.getGameState().getPlayerTwoScore()
        );

        activity.getBinding().gameHeader.setTimer(
                formatTime(round.getGameState().getRemainingSeconds())
        );
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void renderColumns() {
        Asocijacija round = asocijacijeService.getCurrentRound();
        List<AsocijacijaKolona> columns = round.getColumns();

        for (int i = 0; i < columns.size() && i < columnContainers.size(); i++) {
            LinearLayout container = columnContainers.get(i);
            AsocijacijaKolona column = columns.get(i);

            container.removeAllViews();

            for (int j = 0; j < column.getFields().size(); j++) {
                AsocijacijaPolje field = column.getFields().get(j);
                TextView fieldView = createFieldView(i, j, column, field);
                container.addView(fieldView);
            }

            LinearLayout solutionRow = createSolutionInputRow(i, round, column);
            container.addView(solutionRow);
        }
    }

    private TextView createFieldView(int columnIndex, int fieldIndex, AsocijacijaKolona column, AsocijacijaPolje field) {
        TextView textView = new TextView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        textView.setLayoutParams(params);

        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        textView.setPadding(dp(4), dp(4), dp(4), dp(4));

        applyFieldState(textView, field, column.getLabel(), fieldIndex);

        textView.setOnClickListener(v -> {
            AsocijacijeService.ActionResult result = asocijacijeService.openField(columnIndex, fieldIndex);
            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
            renderWholeScreen();
        });

        return textView;
    }

    private void applyFieldState(TextView textView, AsocijacijaPolje field, String label, int position) {
        if (field.isOpened()) {
            textView.setBackgroundResource(R.drawable.bg_asocijacije_field_open);
            textView.setText(field.getText());
        } else {
            textView.setBackgroundResource(R.drawable.game_field_background);
            textView.setText(label + (position + 1));
        }
    }

    private LinearLayout createSolutionInputRow(int columnIndex, Asocijacija round, AsocijacijaKolona column) {
        LinearLayout wrapper = new LinearLayout(requireContext());

        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(dp(4), dp(6), dp(4), dp(4));
        wrapper.setLayoutParams(wrapperParams);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(4), dp(4), dp(4), dp(4));

        boolean revealSolution = column.isSolved() || round.getGameState().isRoundFinished();

        if (revealSolution) {
            wrapper.setBackgroundResource(R.drawable.bg_asocijacije_solution_open);
        } else {
            wrapper.setBackgroundResource(R.drawable.bg_asocijacije_solution_closed);
        }

        EditText editText = new EditText(requireContext());
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(44)
        );
        editText.setLayoutParams(etParams);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setHint("Rešenje " + column.getLabel());
        editText.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setPadding(dp(8), 0, dp(8), 0);
        editText.setSingleLine(true);

        Button button = new Button(requireContext());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                dp(52),
                dp(32)
        );
        btnParams.gravity = Gravity.END;
        btnParams.topMargin = dp(4);
        button.setLayoutParams(btnParams);
        button.setText("OK");
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        if (revealSolution) {
            editText.setText(column.getSolution());
            editText.setEnabled(false);
            button.setEnabled(false);
        } else {
            boolean enableGuess = asocijacijeService.canCurrentPlayerGuess();
            editText.setEnabled(enableGuess);
            button.setEnabled(enableGuess);
        }

        button.setOnClickListener(v -> {
            AsocijacijeService.ActionResult result =
                    asocijacijeService.submitColumnSolution(columnIndex, editText.getText().toString());

            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

            if (result.isSuccess()
                    && roundTimer != null
                    && asocijacijeService.getCurrentRound().getGameState().isRoundFinished()) {
                roundTimer.cancel();
            }

            renderWholeScreen();
        });

        wrapper.addView(editText);
        wrapper.addView(button);

        return wrapper;
    }

    private void renderFinalState() {
        Asocijacija round = asocijacijeService.getCurrentRound();
        boolean reveal = round.isFinalSolved() || round.getGameState().isRoundFinished();

        if (reveal) {
            binding.finalSolutionContainer.setBackgroundResource(R.drawable.bg_asocijacije_solution_open);
            binding.etAsocijacijeFinalSolution.setText(round.getFinalSolution());
            binding.etAsocijacijeFinalSolution.setEnabled(false);
            binding.btnAsocijacijeFinalSubmit.setEnabled(false);
        } else {
            binding.finalSolutionContainer.setBackgroundResource(R.drawable.bg_asocijacije_solution_closed);
            binding.etAsocijacijeFinalSolution.setText("");
            boolean enableGuess = asocijacijeService.canCurrentPlayerGuess();
            binding.etAsocijacijeFinalSolution.setEnabled(enableGuess);
            binding.btnAsocijacijeFinalSubmit.setEnabled(enableGuess);
        }
    }

    private void renderPassButtonState() {
        Asocijacija round = asocijacijeService.getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            if (asocijacijeService.canAdvanceRound()) {
                binding.btnAsocijacijePass.setText("Sledeća runda");
                binding.btnAsocijacijePass.setEnabled(true);
            } else {
                binding.btnAsocijacijePass.setText("Kraj");
                binding.btnAsocijacijePass.setEnabled(false);
            }
        } else {
            binding.btnAsocijacijePass.setText("Predaj potez");
            binding.btnAsocijacijePass.setEnabled(asocijacijeService.canCurrentPlayerGuess());
        }
    }

    private void startRoundTimer() {
        if (asocijacijeService == null) {
            return;
        }

        Asocijacija round = asocijacijeService.getCurrentRound();

        if (roundTimer != null) {
            roundTimer.cancel();
        }

        roundTimer = new CountDownTimer(round.getGameState().getRemainingSeconds() * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                round.getGameState().setRemainingSeconds((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                round.getGameState().setRemainingSeconds(0);
                AsocijacijeService.ActionResult result = asocijacijeService.onTimeExpired();
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                renderWholeScreen();
            }
        }.start();
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