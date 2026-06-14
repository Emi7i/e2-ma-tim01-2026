package com.example.slagalica.presentation.fragments.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slagalica.databinding.FragmentAuthResetPasswordBinding;
import com.example.slagalica.domain.model.auth.ResetPasswordDTO;
import com.example.slagalica.domain.service.auth.AuthService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordFragment extends Fragment {

    @Inject
    AuthService authService;

    private FragmentAuthResetPasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.resetPasswordButton.setOnClickListener(v -> attemptResetPassword());
    }

    private void attemptResetPassword() {
        String oldPassword = binding.oldPasswordInput.getText().toString().trim();
        String newPassword = binding.newPassword1Input.getText().toString().trim();
        String repeatedNewPassword = binding.newPassword2Input.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(repeatedNewPassword)) {
            Toast.makeText(getContext(), "Sva polja obavezna!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(repeatedNewPassword)) {
            Toast.makeText(getContext(), "Nove šifre nisu iste.", Toast.LENGTH_SHORT).show();
            return;
        }

        ResetPasswordDTO dto = new ResetPasswordDTO(oldPassword, newPassword, repeatedNewPassword);

        binding.resetPasswordButton.setEnabled(false);

        authService.resetPassword(dto)
                .thenAccept(unused -> requireActivity().runOnUiThread(() -> {
                    binding.resetPasswordButton.setEnabled(true);
                    Toast.makeText(getContext(), "Uspešno resetovana šifra!", Toast.LENGTH_SHORT).show();
                    binding.oldPasswordInput.setText("");
                    binding.newPassword1Input.setText("");
                    binding.newPassword2Input.setText("");
                }))
                .exceptionally(ex -> {
                    requireActivity().runOnUiThread(() -> {
                        binding.resetPasswordButton.setEnabled(true);
                        Toast.makeText(getContext(), "Neuspešno. Je l valja stara šifra?", Toast.LENGTH_SHORT).show();
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