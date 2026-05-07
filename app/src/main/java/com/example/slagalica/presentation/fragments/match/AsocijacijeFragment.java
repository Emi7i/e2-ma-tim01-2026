package com.example.slagalica.presentation.fragments.match;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.AsocijacijeRepository;
import com.example.slagalica.repository.impl.stub.StubAsocijacijeRepository;

import java.util.ArrayList;
import java.util.List;

public class AsocijacijeFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private AsocijacijeRepository asocijacijeRepository;
    private Asocijacija asocijacija;

    private LinearLayout finalSolutionContainer;
    private EditText etFinalSolution;
    private Button btnFinalSubmit;

    private final List<LinearLayout> columnContainers = new ArrayList<>();

    public AsocijacijeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_asocijacije, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        matchViewModel.setGameActive(true);

        asocijacijeRepository = new StubAsocijacijeRepository();
        asocijacija = asocijacijeRepository.getAsocijacija();

        bindViews(view);
        renderColumns();
        setupFinalSolution();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (matchViewModel != null) {
            matchViewModel.setGameActive(false);
        }
    }

    private void bindViews(View view) {

        finalSolutionContainer = view.findViewById(R.id.finalSolutionContainer);
        etFinalSolution = view.findViewById(R.id.etAsocijacijeFinalSolution);
        btnFinalSubmit = view.findViewById(R.id.btnAsocijacijeFinalSubmit);

        columnContainers.clear();
        columnContainers.add(view.findViewById(R.id.asocijacijeColumnA));
        columnContainers.add(view.findViewById(R.id.asocijacijeColumnB));
        columnContainers.add(view.findViewById(R.id.asocijacijeColumnC));
        columnContainers.add(view.findViewById(R.id.asocijacijeColumnD));
    }

    private void renderColumns() {
        List<AsocijacijaKolona> columns = asocijacija.getColumns();

        for (int i = 0; i < columns.size() && i < columnContainers.size(); i++) {
            LinearLayout container = columnContainers.get(i);
            AsocijacijaKolona column = columns.get(i);

            container.removeAllViews();

            for (int j = 0; j < column.getFields().size(); j++) {
                AsocijacijaPolje field = column.getFields().get(j);
                TextView fieldView = createFieldView(column, field, j);
                container.addView(fieldView);
            }

            LinearLayout solutionRow = createSolutionInputRow(column);
            container.addView(solutionRow);
        }
    }

    private TextView createFieldView(AsocijacijaKolona column, AsocijacijaPolje field, int position) {
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
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        textView.setPadding(dp(4), dp(4), dp(4), dp(4));

        applyFieldState(textView, field, column.getLabel(), position);

        textView.setOnClickListener(v -> {
            if (!field.isOpened()) {
                field.setOpened(true);
                applyFieldState(textView, field, column.getLabel(), position);
            }
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

    private LinearLayout createSolutionInputRow(AsocijacijaKolona column) {
        LinearLayout wrapper = new LinearLayout(requireContext());

        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(dp(4), dp(6), dp(4), dp(4));
        wrapper.setLayoutParams(wrapperParams);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(4), dp(4), dp(4), dp(4));

        if (column.isSolved()) {
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
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
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

        if (column.isSolved()) {
            editText.setText(column.getSolution());
            editText.setEnabled(false);
            button.setEnabled(false);
        }

        button.setOnClickListener(v -> {
            String enteredText = editText.getText().toString().trim();

            if (enteredText.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Unesi rešenje kolone " + column.getLabel(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredText.equalsIgnoreCase(column.getSolution())) {
                column.setSolved(true);
                openAllFieldsInColumn(column);
                renderColumns();

                Toast.makeText(requireContext(),
                        "Tačno rešenje kolone " + column.getLabel(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Netačno rešenje kolone " + column.getLabel(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        wrapper.addView(editText);
        wrapper.addView(button);

        return wrapper;
    }

    private void setupFinalSolution() {
        applyFinalState();

        btnFinalSubmit.setOnClickListener(v -> {
            String enteredText = etFinalSolution.getText().toString().trim();

            if (enteredText.isEmpty()) {
                Toast.makeText(requireContext(), "Unesi konačno rešenje", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredText.equalsIgnoreCase(asocijacija.getFinalSolution())) {
                openWholeBoard();
                renderColumns();
                applyFinalState();

                etFinalSolution.setText(asocijacija.getFinalSolution());
                etFinalSolution.setEnabled(false);
                btnFinalSubmit.setEnabled(false);

                Toast.makeText(requireContext(), "Tačno konačno rešenje!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Netačno konačno rešenje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFinalState() {
        if (asocijacija.isFinalSolved()) {
            finalSolutionContainer.setBackgroundResource(R.drawable.bg_asocijacije_solution_open);
            etFinalSolution.setEnabled(false);
            btnFinalSubmit.setEnabled(false);
        } else {
            finalSolutionContainer.setBackgroundResource(R.drawable.bg_asocijacije_solution_closed);
            etFinalSolution.setEnabled(true);
            btnFinalSubmit.setEnabled(true);
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

    private void openAllFieldsInColumn(AsocijacijaKolona column) {
        for (AsocijacijaPolje field : column.getFields()) {
            field.setOpened(true);
        }
    }

    private void openWholeBoard() {
        for (AsocijacijaKolona column : asocijacija.getColumns()) {
            column.setSolved(true);
            for (AsocijacijaPolje field : column.getFields()) {
                field.setOpened(true);
            }
        }
        asocijacija.setFinalSolved(true);
    }
}