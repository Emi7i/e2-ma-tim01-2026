package com.example.slagalica.presentation.fragments.social;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentFriendsBinding;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.fragments.profile.FriendProfileFragment;
import com.example.slagalica.presentation.viewmodels.FriendsViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private boolean isCameraActive = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(),
                            "Dozvola za kameru je potrebna za skeniranje QR koda",
                            Toast.LENGTH_SHORT).show();
                }
            });

    public FriendsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(FriendsViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppActivity) requireActivity()).setToolbarTitle("Prijatelji");

        setupClickListeners();
        observeViewModel();
        viewModel.loadFriends();
    }

    private void setupClickListeners() {
        binding.addButton.setOnClickListener(v -> {
            String username = binding.searchInput.getText() != null
                    ? binding.searchInput.getText().toString()
                    : "";
            viewModel.searchAndAddFriend(username);
        });

        binding.addByQrButton.setOnClickListener(v -> {
            if (isCameraActive) {
                stopCamera();
            } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void startCamera() {
        isCameraActive = true;
        binding.qrScannerContainer.setVisibility(View.VISIBLE);
        binding.addByQrButton.setText("Zatvori skener");

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
            } catch (Exception e) {
                stopCamera();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        BarcodeScanner scanner = BarcodeScanning.getClient(
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }
            InputImage inputImage = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty() && barcodes.get(0).getRawValue() != null) {
                            String content = barcodes.get(0).getRawValue();
                            imageProxy.close();
                            requireActivity().runOnUiThread(() -> {
                                stopCamera();
                                viewModel.addFriendByScannedContent(content);
                            });
                        } else {
                            imageProxy.close();
                        }
                    })
                    .addOnFailureListener(e -> imageProxy.close());
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(),
                CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
    }

    private void stopCamera() {
        isCameraActive = false;
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (binding != null) {
            binding.qrScannerContainer.setVisibility(View.GONE);
            binding.addByQrButton.setText(getString(R.string.friends_add_by_qr));
        }
    }

    private void observeViewModel() {
        viewModel.getFriends().observe(getViewLifecycleOwner(), this::renderFriends);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });
    }

    private void renderFriends(List<UserProfile> friends) {
        binding.friendsContainer.removeAllViews();

        if (friends == null || friends.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Još nema prijatelja. Dodajte ih pretragom!");
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            empty.setPadding(0, dp(16), 0, 0);
            binding.friendsContainer.addView(empty);
            return;
        }

        for (UserProfile friend : friends) {
            binding.friendsContainer.addView(createFriendCard(friend));
        }
    }

    private LinearLayout createFriendCard(UserProfile friend) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_notification_card);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(8);
        card.setLayoutParams(cardParams);

        ImageView avatar = new ImageView(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        avatarParams.rightMargin = dp(12);
        avatar.setLayoutParams(avatarParams);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
            Glide.with(this)
                    .load(friend.getAvatar())
                    .placeholder(R.drawable.profile_icon)
                    .circleCrop()
                    .into(avatar);
        } else {
            avatar.setImageResource(R.drawable.profile_icon);
        }
        card.addView(avatar);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView usernameView = new TextView(requireContext());
        usernameView.setText(friend.getUsername());
        usernameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        usernameView.setTypeface(null, Typeface.BOLD);
        usernameView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_dark));

        TextView detailView = new TextView(requireContext());
        detailView.setText("Liga: " + friend.getLeague() + "  •  " + friend.getNumStars() + " ⭐");
        detailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        detailView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        TextView rankView = new TextView(requireContext());
        rankView.setText("Rang: #" + friend.getRank());
        rankView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rankView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        info.addView(usernameView);
        info.addView(detailView);
        info.addView(rankView);
        card.addView(info);

        card.setOnClickListener(v -> openFriendProfile(friend.getUserId()));

        return card;
    }

    private void openFriendProfile(String userId) {
        FragmentTransition.to(
                FriendProfileFragment.newInstance(userId),
                requireActivity(),
                true,
                R.id.appContainer);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                requireContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCamera();
        cameraExecutor.shutdown();
        binding = null;
    }
}
