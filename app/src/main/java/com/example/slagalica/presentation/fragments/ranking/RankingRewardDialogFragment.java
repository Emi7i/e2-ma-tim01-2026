package com.example.slagalica.presentation.fragments.ranking;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.databinding.DialogRankingRewardBinding;
import com.example.slagalica.domain.model.ranking.RankingCycleType;
import com.example.slagalica.domain.model.ranking.RankingReward;
import com.example.slagalica.presentation.viewmodels.RankingViewModel;

public class RankingRewardDialogFragment extends DialogFragment {

    public static final String TAG = "ranking_reward_dialog";

    private static final String ARG_REWARD_ID = "reward_id";
    private static final String ARG_CYCLE_TYPE = "cycle_type";
    private static final String ARG_PLACEMENT = "placement";
    private static final String ARG_TOKENS = "tokens";

    private DialogRankingRewardBinding binding;
    private RankingViewModel viewModel;
    private ToneGenerator toneGenerator;
    private boolean markedSeen;

    public static RankingRewardDialogFragment newInstance(
            RankingReward reward
    ) {
        RankingRewardDialogFragment fragment =
                new RankingRewardDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(
                ARG_REWARD_ID,
                reward.getRewardId()
        );
        arguments.putString(
                ARG_CYCLE_TYPE,
                reward.getCycleType()
        );
        arguments.putInt(
                ARG_PLACEMENT,
                reward.getPlacement()
        );
        arguments.putInt(
                ARG_TOKENS,
                reward.getTokenReward()
        );

        fragment.setArguments(arguments);
        fragment.setCancelable(false);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(
            @Nullable Bundle savedInstanceState
    ) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = DialogRankingRewardBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RankingViewModel.class);

        Bundle arguments = requireArguments();

        String cycleTypeName =
                arguments.getString(
                        ARG_CYCLE_TYPE,
                        RankingCycleType.WEEKLY.name()
                );

        RankingCycleType cycleType =
                RankingCycleType.valueOf(cycleTypeName);

        int placement =
                arguments.getInt(ARG_PLACEMENT);
        int tokens =
                arguments.getInt(ARG_TOKENS);

        binding.rewardTitle.setText("Čestitamo!");
        binding.rewardPlacement.setText(
                placement + ". mesto"
        );
        binding.rewardCycle.setText(
                cycleType.getDisplayName() + " rang lista"
        );
        binding.rewardTokens.setText(
                "+" + tokens + " tokena"
        );

        binding.rewardCloseButton.setOnClickListener(v ->
                dismiss()
        );

        playRewardSound();
        startRewardAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );

        int width = (int) (
                getResources()
                        .getDisplayMetrics()
                        .widthPixels * 0.90f
        );

        dialog.getWindow().setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void onDestroyView() {
        markSeenOnce();

        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        binding = null;
        super.onDestroyView();
    }

    private void playRewardSound() {
        toneGenerator = new ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                90
        );

        toneGenerator.startTone(
                ToneGenerator.TONE_PROP_ACK,
                550
        );
    }

    private void startRewardAnimation() {
        binding.rewardIcon.setScaleX(0.45f);
        binding.rewardIcon.setScaleY(0.45f);
        binding.rewardIcon.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                binding.rewardIcon,
                View.SCALE_X,
                0.45f,
                1.20f,
                1f
        );

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                binding.rewardIcon,
                View.SCALE_Y,
                0.45f,
                1.20f,
                1f
        );

        ObjectAnimator alpha = ObjectAnimator.ofFloat(
                binding.rewardIcon,
                View.ALPHA,
                0f,
                1f
        );

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(850L);
        set.start();
    }

    private void markSeenOnce() {
        if (markedSeen || getArguments() == null) {
            return;
        }

        markedSeen = true;
        viewModel.markRewardSeen(
                requireArguments().getString(ARG_REWARD_ID)
        );
    }
}
