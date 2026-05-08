package com.example.slagalica.presentation.fragments.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentHomeBinding;
import com.example.slagalica.presentation.fragments.match.KoZnaZnaFragment;
import com.example.slagalica.presentation.fragments.match.KorakPoKorakFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Temporary access to all games from home
        binding.koZnaZna.setOnClickListener(v -> {
            FragmentTransition.to(new KoZnaZnaFragment(), requireActivity(), true, R.id.appContainer);
        });
        binding.korakPoKorak.setOnClickListener(v -> {
            FragmentTransition.to(new KorakPoKorakFragment(), requireActivity(), true, R.id.appContainer);
        });
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        binding=null;
    }
}