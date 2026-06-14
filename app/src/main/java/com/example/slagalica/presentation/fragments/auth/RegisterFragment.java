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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentAuthRegisterBinding;
import com.example.slagalica.domain.model.auth.RegistrationDTO;
import com.example.slagalica.domain.service.auth.AuthService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    @Inject
    AuthService authService;

    FragmentAuthRegisterBinding binding;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAuthRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.regions)); // TODO: get from elsewhere
        ((AutoCompleteTextView) binding.regionInput).setAdapter(regionAdapter);

        // Listeners
        binding.registerButton.setOnClickListener(v -> attemptRegister());
        binding.loginLink.setOnClickListener(v -> {
            FragmentTransition.to(new LoginFragment(), requireActivity(), true, R.id.main);
        });

    }

    private void attemptRegister() {
        String username = binding.usernameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String region = (binding.regionInput).getText().toString().trim();
        String password = binding.password1Input.getText().toString();
        String repeatedPassword = binding.password2Input.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(region)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatedPassword)) {
            Toast.makeText(getContext(), "Sva polja su obavezna.", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistrationDTO dto = new RegistrationDTO(email, username, region, password, repeatedPassword);

        binding.registerButton.setEnabled(false);

        authService.registerUser(dto)
                .thenAccept(unused -> requireActivity().runOnUiThread(() -> {
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(getContext(), "Registracija uspešna! Provjerite email za potvrdu.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(requireActivity(), AppActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }))
                .exceptionally(ex -> {
                    requireActivity().runOnUiThread(() -> {
                        binding.registerButton.setEnabled(true);
                        Toast.makeText(getContext(), "Registracija neuspešna: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}