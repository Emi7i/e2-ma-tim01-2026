package com.example.slagalica.presentation.fragments.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slagalica.databinding.FragmentNotificationTargetPlaceholderBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationTargetPlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_SENDER = "arg_sender";
    private static final String ARG_TIME = "arg_time";
    private static final String ARG_STATUS = "arg_status";
    private static final String ARG_ACTIONS = "arg_actions";

    private FragmentNotificationTargetPlaceholderBinding binding;

    public NotificationTargetPlaceholderFragment() {
    }

    public static NotificationTargetPlaceholderFragment newInstance(String title,
                                                                    String message,
                                                                    String type,
                                                                    String sender,
                                                                    String time,
                                                                    String status,
                                                                    String actions) {
        NotificationTargetPlaceholderFragment fragment = new NotificationTargetPlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_SENDER, sender);
        args.putString(ARG_TIME, time);
        args.putString(ARG_STATUS, status);
        args.putString(ARG_ACTIONS, actions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationTargetPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            binding.tvPlaceholderTitle.setText(args.getString(ARG_TITLE, "Detalji notifikacije"));
            binding.tvPlaceholderType.setText(args.getString(ARG_TYPE, ""));
            binding.tvPlaceholderSender.setText(args.getString(ARG_SENDER, ""));
            binding.tvPlaceholderTime.setText(args.getString(ARG_TIME, ""));
            binding.tvPlaceholderStatus.setText(args.getString(ARG_STATUS, ""));
            binding.tvPlaceholderMessage.setText(args.getString(ARG_MESSAGE, ""));
            binding.tvPlaceholderActions.setText(args.getString(ARG_ACTIONS, ""));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}