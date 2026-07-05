package com.example.slagalica.presentation.fragments.social;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.example.slagalica.domain.model.social.MatchRequest;
import com.example.slagalica.presentation.viewmodels.FriendsViewModel;
import com.example.slagalica.presentation.viewmodels.MatchRequestViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;
    private MatchRequestViewModel matchRequestViewModel;
    private List<UserProfile> currentFriends = new ArrayList<>();
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
        matchRequestViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity())
                .get(MatchRequestViewModel.class);
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
            binding.searchInput.setText("");
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
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            currentFriends = friends != null ? friends : new ArrayList<>();
            renderFriends(currentFriends);
        });

        matchRequestViewModel.getOutgoingRequest().observe(getViewLifecycleOwner(),
                request -> renderFriends(currentFriends));

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
            binding.friendsContainer.addView(buildEmptyState());
            return;
        }

        for (UserProfile friend : friends) {
            binding.friendsContainer.addView(createFriendCard(friend));
        }
    }

    private LinearLayout buildEmptyState() {
        LinearLayout wrap = new LinearLayout(requireContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(android.view.Gravity.CENTER);
        wrap.setPadding(dp(24), dp(48), dp(24), dp(24));
        wrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.person_24dp_000000_fill0_wght400_grad0_opsz24);
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
        icon.setAlpha(0.35f);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(72), dp(72));
        iconParams.bottomMargin = dp(16);
        icon.setLayoutParams(iconParams);

        TextView title = new TextView(requireContext());
        title.setText("Nema prijatelja");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        title.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dp(6);
        title.setLayoutParams(titleParams);

        TextView sub = new TextView(requireContext());
        sub.setText("Pretražite korisnika ili skenirajte\nnjihov QR kod");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        sub.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        sub.setGravity(android.view.Gravity.CENTER);
        sub.setLineSpacing(0, 1.3f);

        wrap.addView(icon);
        wrap.addView(title);
        wrap.addView(sub);
        return wrap;
    }

    private LinearLayout createFriendCard(UserProfile friend) {
        // Root card
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_friend_card);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setClickable(true);
        card.setFocusable(true);
        card.setElevation(dp(1));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);

        // Avatar with circular background
        FrameLayout avatarWrap = new FrameLayout(requireContext());
        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        wrapParams.rightMargin = dp(14);
        avatarWrap.setLayoutParams(wrapParams);

        ImageView avatar = new ImageView(requireContext());
        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        avatar.setLayoutParams(avatarParams);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
            Glide.with(this)
                    .load(friend.getAvatar())
                    .placeholder(R.drawable.profile_icon)
                    .circleCrop()
                    .into(avatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile_icon)
                    .circleCrop()
                    .into(avatar);
        }
        avatarWrap.addView(avatar);
        card.addView(avatarWrap);

        // Info column
        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView usernameView = new TextView(requireContext());
        usernameView.setText(friend.getUsername());
        usernameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        usernameView.setTypeface(null, Typeface.BOLD);
        usernameView.setTextColor(Color.parseColor("#1A1A2E"));
        LinearLayout.LayoutParams unParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        unParams.bottomMargin = dp(3);
        usernameView.setLayoutParams(unParams);

        // Stars + league pill row
        LinearLayout statsRow = new LinearLayout(requireContext());
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(2);
        statsRow.setLayoutParams(rowParams);

        TextView starsView = new TextView(requireContext());
        starsView.setText(friend.getNumStars() + " ⭐");
        starsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        starsView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        TextView dot = new TextView(requireContext());
        dot.setText("  ·  ");
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dot.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        TextView leagueView = new TextView(requireContext());
        leagueView.setText(friend.getLeague());
        leagueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        leagueView.setTypeface(null, Typeface.BOLD);
        leagueView.setTextColor(ContextCompat.getColor(requireContext(), R.color.field_border));
        leagueView.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor("#1A673AB7")));
        leagueView.setBackground(buildLeaguePillBg());
        leagueView.setPadding(dp(7), dp(2), dp(7), dp(2));

        statsRow.addView(starsView);
        statsRow.addView(dot);
        statsRow.addView(leagueView);

        TextView rankView = new TextView(requireContext());
        rankView.setText("Rang: #" + friend.getRank());
        rankView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rankView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        info.addView(usernameView);
        info.addView(statsRow);
        info.addView(rankView);
        card.addView(info);

        // Match request button
        MatchRequest outgoing = matchRequestViewModel.getOutgoingRequest().getValue();
        boolean pendingToThis = outgoing != null
                && outgoing.getReceiverId().equals(friend.getUserId())
                && MatchRequest.STATUS_PENDING.equals(outgoing.getStatus());

        com.google.android.material.button.MaterialButton matchBtn =
                new com.google.android.material.button.MaterialButton(requireContext());
        matchBtn.setText(pendingToThis
                ? getString(R.string.match_request_cancel)
                : getString(R.string.match_request_send_short));
        matchBtn.setAllCaps(false);
        matchBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        matchBtn.setPadding(dp(14), dp(0), dp(14), dp(0));
        matchBtn.setCornerRadius(dp(8));
        matchBtn.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(),
                        pendingToThis ? R.color.red : R.color.field_border)));
        matchBtn.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.leftMargin = dp(8);
        matchBtn.setLayoutParams(btnParams);

        matchBtn.setOnClickListener(v -> {
            MatchRequest cur = matchRequestViewModel.getOutgoingRequest().getValue();
            boolean isPending = cur != null
                    && cur.getReceiverId().equals(friend.getUserId())
                    && MatchRequest.STATUS_PENDING.equals(cur.getStatus());
            if (isPending) {
                matchRequestViewModel.cancelRequest();
            } else if (cur == null) {
                matchRequestViewModel.sendRequest(friend.getUserId(), friend.getUsername());
            }
        });

        card.addView(matchBtn);
        card.setOnClickListener(v -> openFriendProfile(friend.getUserId()));

        return card;
    }

    private android.graphics.drawable.GradientDrawable buildLeaguePillBg() {
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(20));
        bg.setColor(Color.parseColor("#1A673AB7"));
        return bg;
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
