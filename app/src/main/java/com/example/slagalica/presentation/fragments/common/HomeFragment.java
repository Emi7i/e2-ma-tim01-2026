package com.example.slagalica.presentation.fragments.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
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
import com.google.firebase.firestore.ListenerRegistration;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    MatchViewModel matchViewModel;
    FragmentHomeBinding binding;
    private ListenerRegistration matchmakingRegistration;

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
            binding.start.setText("Čekam...");
            binding.start.setEnabled(false);

            String currentUserId = sessionManager.getCurrentUserId();

            matchmakingEntryRepository.getOldest(currentUserId)
                    .thenCompose(opponentEntry -> {
                        if (opponentEntry != null) {
                            // Someone's already waiting — claim them and start the match now
                            String matchId = opponentEntry.getMatchId();
                            requireActivity().runOnUiThread(() ->
                                    matchViewModel.startMatch(currentUserId, opponentEntry.getUserId(), MatchType.CLASSIC, matchId,
                                            () -> {
                                                // ONLY claim/delete here — once the match doc is confirmed written
                                                matchmakingEntryRepository.claim(opponentEntry.getUserId(), currentUserId)
                                                        .thenAccept(unused ->  {});
                                            }));
                            return null;
//                            return matchmakingEntryRepository.claim(opponentEntry.getUserId(), currentUserId)
//                                    .thenAccept(unused -> {
//                                        matchmakingEntryRepository.delete(opponentEntry.getUserId());
//                                        requireActivity().runOnUiThread(() ->
//                                                matchViewModel.startMatch(currentUserId, opponentEntry.getUserId(), MatchType.CLASSIC, matchId,
//                                                        () -> {
//                                                            // Runs only once the match document is confirmed written
//                                                            matchmakingEntryRepository.claim(opponentEntry.getUserId(), currentUserId)
//                                                                    .thenAccept(unused2 -> matchmakingEntryRepository.delete(opponentEntry.getUserId()));
//                                                        }));
//                                    });
                        } else {
                            // Nobody waiting — join queue and listen for someone to claim us
                            return matchmakingEntryRepository.add(currentUserId)
                                    .thenAccept(unused -> requireActivity().runOnUiThread(() -> {
                                        matchmakingRegistration = matchmakingEntryRepository.observeEntry(currentUserId, entry ->
                                                requireActivity().runOnUiThread(() -> {
                                                    Log.d("Matchmaking", "Found it!");
                                                    matchmakingEntryRepository.delete(currentUserId);
                                                    if (matchmakingRegistration != null) {
                                                        matchmakingRegistration.remove();
                                                        matchmakingRegistration = null;
                                                    }
                                                    matchViewModel.startMatch(entry.getMatchedWith(), currentUserId, MatchType.CLASSIC, entry.getMatchId(), null);
                                                })
                                        );
                                    }));
                        }
                    })
                    .exceptionally(ex -> {
                        requireActivity().runOnUiThread(() -> {
                            binding.start.setText("Započni");
                            binding.start.setEnabled(true);
                            Toast.makeText(requireContext(), "Greška, pokušajte ponovo", Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });
        });
    }

    @Override
    public void onDestroyView() {
        if (matchmakingRegistration != null) {
            matchmakingRegistration.remove();
            matchmakingRegistration = null;
        }
        super.onDestroyView();
        binding=null;
    }
}