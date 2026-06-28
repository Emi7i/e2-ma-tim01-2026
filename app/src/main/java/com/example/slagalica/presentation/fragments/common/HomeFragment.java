package com.example.slagalica.presentation.fragments.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentHomeBinding;
import com.example.slagalica.presentation.fragments.match.KoZnaZnaFragment;
import com.example.slagalica.presentation.fragments.match.AsocijacijeFragment;
import com.example.slagalica.presentation.fragments.match.KorakPoKorakFragment;
import com.example.slagalica.presentation.fragments.match.MojBrojFragment;
import com.example.slagalica.presentation.fragments.match.SpojniceFragment;
import com.example.slagalica.presentation.fragments.match.SkockoFragment;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentHomeBinding binding;

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

        binding.start.setOnClickListener(v -> {
            String player1Id = "NgIpszkS4LU3RTDtOd4qqQlZm7Q2"; // perica
            String player2Id = "rAvFq0wuLHQvUwbD0FK0olNLocG3"; // skocko
            matchViewModel.startMatch(player1Id, player2Id);
        });

        // Temporary access to all games from home
        binding.koZnaZna.setOnClickListener(v -> {
            FragmentTransition.to(new KoZnaZnaFragment(), requireActivity(), true, R.id.appContainer);
        });
        binding.spojnice.setOnClickListener(v -> {
            FragmentTransition.to(new SpojniceFragment(), requireActivity(), true, R.id.appContainer);
        });
        binding.korakPoKorak.setOnClickListener(v -> {
            matchViewModel.getMatch().startKorakPoKorak();
            FragmentTransition.to(new KorakPoKorakFragment(), requireActivity(), true, R.id.appContainer);
        });

        binding.mojBroj.setOnClickListener(v -> {
            matchViewModel.getMatch().startMojBroj();
            FragmentTransition.to(new MojBrojFragment(), requireActivity(), true, R.id.appContainer);
        });

        binding.asocijacije.setOnClickListener(v -> {
            FragmentTransition.to(new AsocijacijeFragment(), requireActivity(), true, R.id.appContainer);
        });

        binding.skocko.setOnClickListener(v -> {
            FragmentTransition.to(new SkockoFragment(), requireActivity(), true, R.id.appContainer);
        });
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        binding=null;
    }
}