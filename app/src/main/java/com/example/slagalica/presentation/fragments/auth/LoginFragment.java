package com.example.slagalica.presentation.fragments.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentAuthLoginBinding;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    FragmentAuthLoginBinding binding;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Listeners
        binding.loginButton.setOnClickListener(v -> {
            // TODO: add auth logic
            Intent intent = new Intent(requireActivity(), AppActivity.class);
            startActivity(intent);
            requireActivity().finish(); // finishing AuthActivity so the user can't go back there with return
        });
        binding.loginRegisterLink.setOnClickListener(v -> {
            FragmentTransition.to(new RegisterFragment(), requireActivity(), true, R.id.main);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // To prevent a memory leak, needs to be done in every fragment?
        binding = null;
    }
}