package com.example.slagalica.presentation.fragments.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentHomeBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.match.games.MatchType;
import com.example.slagalica.presentation.fragments.match.KoZnaZnaFragment;
import com.example.slagalica.presentation.fragments.match.AsocijacijeFragment;
import com.example.slagalica.presentation.fragments.match.KorakPoKorakFragment;
import com.example.slagalica.presentation.fragments.match.MojBrojFragment;
import com.example.slagalica.presentation.fragments.match.SpojniceFragment;
import com.example.slagalica.presentation.fragments.match.SkockoFragment;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;
import com.example.slagalica.repository.impl.MatchmakingEntryRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentHomeBinding binding;

    @Inject
    MatchmakingEntryRepository matchmakingEntryRepository;
    @Inject
    SessionManager sessionManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        matchViewModel.getInsufficientTokens().observe(getViewLifecycleOwner(), poor -> {
            if (Boolean.TRUE.equals(poor)) {
                Toast.makeText(requireContext(), "Nemate dovoljno tokena!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.start.setOnClickListener(v -> {
            // TODO: match only with people in the matchmaking entry queue
            String player1Id = sessionManager.getCurrentUserId();
            String player2Id = "rAvFq0wuLHQvUwbD0FK0olNLocG3"; // skocko
            matchViewModel.startMatch(player1Id, player2Id, MatchType.CLASSIC);
        });
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        binding=null;
    }
}