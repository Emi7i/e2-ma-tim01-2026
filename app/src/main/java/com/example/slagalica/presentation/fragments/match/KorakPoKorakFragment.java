package com.example.slagalica.presentation.fragments.match;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentGameKorakPoKorakBinding;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;
import com.example.slagalica.presentation.viewmodels.KorakPoKorakViewModel;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

// Annotation needed for Hilt to work
@AndroidEntryPoint
public class KorakPoKorakFragment extends Fragment {
    MatchViewModel matchViewModel;
    KorakPoKorakViewModel gameViewModel;
    FragmentGameKorakPoKorakBinding binding;

    int[] points;
    String answer;

    public KorakPoKorakFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGameKorakPoKorakBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class); // generic view model creation
        matchViewModel.setGameActive(true); // activates timer header
        gameViewModel = new ViewModelProvider(this).get(KorakPoKorakViewModel.class);

        points = gameViewModel.getPoints();
        setupListeners();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false); // hide timer
        binding = null;
    }

    private void observeViewModel(){
        // If the match triggered this game to be started, start the timer and other stuff
        matchViewModel.getCurrentGameId().observe(getViewLifecycleOwner(), gameId -> {
            if (gameId == 5) {
                gameViewModel.start((KorakPoKorak) matchViewModel.getMatch().getCurrentGame());
            }
        });

        gameViewModel.getLatestHint().observe(getViewLifecycleOwner(), hint -> {
            if (hint != null) revealNextHint(hint);
        });

        gameViewModel.getRevealAllHints().observe(getViewLifecycleOwner(), reveal -> {
            if (reveal) revealAllHints();
            else {
                binding.answer.setEnabled(true);
                resetAllHints();
            }
        });

        gameViewModel.getStealWindowOpen().observe(getViewLifecycleOwner(), steal -> {
            if (steal) {
                Toast.makeText(requireContext(), "Protivnikov red", Toast.LENGTH_SHORT).show();
            }
        });

        gameViewModel.getTimeLeft().observe(getViewLifecycleOwner(), timeLeft -> {
            com.example.slagalica.presentation.views.GameHeaderView gameHeader = requireActivity().findViewById(R.id.gameHeader);
            if (gameHeader != null) {
                gameHeader.setTimer(String.format("00:%02d", timeLeft));
            }
        });

        gameViewModel.getGameOver().observe(getViewLifecycleOwner(), over -> {
            if (over) {
                // TODO: notify Match / navigate to next game
            }
        });
    }

    private void revealNextHint(String hint){
        int index = gameViewModel.getGame().getCurrentHint() - 1;
        switch (index) {
            case 0:
                binding.hint1.setText(hint);
                binding.points1.setText(String.valueOf(points[0]));
                break;
            case 1:
                binding.hint2.setText(hint);
                binding.points2.setText(String.valueOf(points[1]));
                break;
            case 2:
                binding.hint3.setText(hint);
                binding.points3.setText(String.valueOf(points[2]));
                break;
            case 3:
                binding.hint4.setText(hint);
                binding.points4.setText(String.valueOf(points[3]));
                break;
            case 4:
                binding.hint5.setText(hint);
                binding.points5.setText(String.valueOf(points[4]));
                break;
            case 5:
                binding.hint6.setText(hint);
                binding.points6.setText(String.valueOf(points[5]));
                break;
            case 6:
                binding.hint7.setText(hint);
                binding.points7.setText(String.valueOf(points[6]));
                break;
        }
    }

    private void revealAllHints(){
        List<String> hints = gameViewModel.getAllHints();
        binding.hint1.setText(hints.get(0));
        binding.points1.setText(String.valueOf(points[0]));
        binding.hint2.setText(hints.get(1));
        binding.points2.setText(String.valueOf(points[1]));
        binding.hint3.setText(hints.get(2));
        binding.points3.setText(String.valueOf(points[2]));
        binding.hint4.setText(hints.get(3));
        binding.points4.setText(String.valueOf(points[3]));
        binding.hint5.setText(hints.get(4));
        binding.points5.setText(String.valueOf(points[4]));
        binding.hint6.setText(hints.get(5));
        binding.points6.setText(String.valueOf(points[5]));
        binding.hint7.setText(hints.get(6));
        binding.points7.setText(String.valueOf(points[6]));

        revealAnswer(gameViewModel.getGame().getTerm());
    }

    private void resetAllHints(){
        binding.hint1.setText("");
        binding.points1.setText("");
        binding.hint2.setText("");
        binding.points2.setText("");
        binding.hint3.setText("");
        binding.points3.setText("");
        binding.hint4.setText("");
        binding.points4.setText("");
        binding.hint5.setText("");
        binding.points5.setText("");
        binding.hint6.setText("");
        binding.points6.setText("");
        binding.hint7.setText("");
        binding.points7.setText("");
    }

    private void revealAnswer(String answer){
        binding.answer.setEnabled(false);
        binding.answer.setText(answer);
    }

    // temp
    private void setupListeners(){
        binding.answer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String input = binding.answer.getText().toString().trim();
                boolean correct = gameViewModel.submitAnswer(input);
                if (!correct) {
                    ColorStateList originalTint = binding.answer.getBackgroundTintList();
                    binding.answer.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    binding.answer.postDelayed(() -> {
                        binding.answer.setText("");
                        binding.answer.setBackgroundTintList(originalTint);
                    }, 1000);
                }
                return true;
            }
            return false;
        });
    }
}