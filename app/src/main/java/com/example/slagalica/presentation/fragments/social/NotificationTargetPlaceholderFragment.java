package com.example.slagalica.presentation.fragments.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slagalica.databinding.FragmentNotificationTargetPlaceholderBinding;
import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.service.social.NotificationsService;
import com.example.slagalica.repository.impl.InMemoryNotificationsRepository;

public class NotificationTargetPlaceholderFragment extends Fragment {

    private static final String ARG_NOTIFICATION_ID = "arg_notification_id";

    private FragmentNotificationTargetPlaceholderBinding binding;
    private NotificationsService notificationsService;
    private NotificationItem notificationItem;

    public NotificationTargetPlaceholderFragment() {
    }

    public static NotificationTargetPlaceholderFragment newInstance(String notificationId) {
        NotificationTargetPlaceholderFragment fragment = new NotificationTargetPlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOTIFICATION_ID, notificationId);
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

        notificationsService = new NotificationsService(InMemoryNotificationsRepository.getInstance());

        String notificationId = null;
        if (getArguments() != null) {
            notificationId = getArguments().getString(ARG_NOTIFICATION_ID);
        }

        notificationItem = InMemoryNotificationsRepository.getInstance().findById(notificationId);

        if (notificationItem == null) {
            Toast.makeText(requireContext(), "Notifikacija nije pronađena", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationsService.markAsRead(notificationItem);

        renderNotification();
        setupActions();
    }

    private void renderNotification() {
        binding.tvPlaceholderNotificationTitle.setText(notificationItem.getTitle());
        binding.tvPlaceholderType.setText(notificationsService.getTypeLabel(notificationItem.getType()));
        binding.tvPlaceholderSender.setText(notificationItem.getSender());
        binding.tvPlaceholderTime.setText(notificationsService.formatTimestamp(notificationItem.getTimestampMillis()));
        binding.tvPlaceholderStatus.setText(notificationsService.getStatusLabel(notificationItem));
        binding.tvPlaceholderActionStatus.setText(notificationsService.getActionStatusLabel(notificationItem));
        binding.tvPlaceholderMessage.setText(notificationItem.getMessage());

        if (notificationItem.hasDecisionAction()
                && notificationItem.getActionStatus() == NotificationActionStatus.PENDING) {
            binding.actionsContainer.setVisibility(View.VISIBLE);
        } else {
            binding.actionsContainer.setVisibility(View.GONE);
        }
    }

    private void setupActions() {
        binding.btnAcceptInvite.setOnClickListener(v -> {
            notificationsService.acceptInvitation(notificationItem);
            Toast.makeText(requireContext(), "Poziv prihvaćen", Toast.LENGTH_SHORT).show();
            renderNotification();
        });

        binding.btnDeclineInvite.setOnClickListener(v -> {
            notificationsService.declineInvitation(notificationItem);
            Toast.makeText(requireContext(), "Poziv odbijen", Toast.LENGTH_SHORT).show();
            renderNotification();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}