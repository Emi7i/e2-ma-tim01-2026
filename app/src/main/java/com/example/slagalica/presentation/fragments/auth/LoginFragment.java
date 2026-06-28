package com.example.slagalica.presentation.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentAuthLoginBinding;
import com.example.slagalica.domain.model.auth.LoginDTO;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.service.auth.AuthService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject
    AuthService authService;

    @Inject
    SessionManager sessionManager;

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
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.loginRegisterLink.setOnClickListener(v -> {
            FragmentTransition.to(new RegisterFragment(), requireActivity(), false, R.id.main);
        });
    }

    private void attemptLogin() {
        String identifier = binding.identifierInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();

        if (TextUtils.isEmpty(identifier) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Sva polja su obavezna.", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginDTO dto = new LoginDTO(identifier, password);

        binding.loginButton.setEnabled(false);

        authService.loginUser(dto)
                .thenAccept(authResult -> requireActivity().runOnUiThread(() -> {
                    binding.loginButton.setEnabled(true);

                    if (authResult.getUser() != null && !authResult.getUser().isEmailVerified()) {
                        Toast.makeText(getContext(), "Potvrdite email prije logovanja!!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    sessionManager.loadCurrentProfile();
                    Intent intent = new Intent(requireActivity(), AppActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // finishing AuthActivity so the user can't go back there with return
                }))
                .exceptionally(ex -> {
                    requireActivity().runOnUiThread(() -> {
                        binding.loginButton.setEnabled(true);
                        Toast.makeText(getContext(), "Neuspešno logovanje. Proverite podatke.", Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // To prevent a memory leak, needs to be done in every fragment?
        binding = null;
    }
}