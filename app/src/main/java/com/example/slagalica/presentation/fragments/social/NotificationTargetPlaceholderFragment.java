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
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.service.social.NotificationsMapper;
import com.example.slagalica.domain.service.social.NotificationsService;
import com.example.slagalica.repository.impl.NotificationsRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationTargetPlaceholderFragment extends Fragment {

    private static final String ARG_NOTIFICATION_ID = "arg_notification_id";
    private static final String TEST_USER_ID = "test_user_123";

    private FragmentNotificationTargetPlaceholderBinding binding;

    @Inject
    NotificationsRepository notificationsRepository;

    private NotificationsService notificationsService;
    private final NotificationsMapper notificationsMapper = new NotificationsMapper();
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

        notificationsService = new NotificationsService();

        String notificationId = null;
        if (getArguments() != null) {
            notificationId = getArguments().getString(ARG_NOTIFICATION_ID);
        }

        if (notificationId == null) {
            Toast.makeText(requireContext(), "Notifikacija nije pronađena", Toast.LENGTH_SHORT).show();
            return;
        }

        loadNotificationFromFirestore(notificationId);
    }

    private void loadNotificationFromFirestore(String notificationId) {
        notificationsRepository.getNotificationsForUser(TEST_USER_ID)
                .thenAccept(documents -> requireActivity().runOnUiThread(() -> {
                    notificationItem = findNotificationById(documents, notificationId);

                    if (notificationItem == null) {
                        Toast.makeText(requireContext(), "Notifikacija nije pronađena", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationsService.markAsRead(notificationItem);
                    updateNotificationInFirestore(notificationItem);

                    renderNotification();
                    setupActions();
                }))
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Greška pri učitavanju notifikacije",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }

    private NotificationItem findNotificationById(List<NotificationDocument> documents, String notificationId) {
        for (NotificationDocument doc : documents) {
            if (notificationId.equals(doc.getNotificationId())) {
                return notificationsMapper.toRuntime(doc);
            }
        }
        return null;
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
            if (notificationItem == null) {
                return;
            }

            notificationsService.acceptInvitation(notificationItem);
            updateNotificationInFirestore(notificationItem);
            Toast.makeText(requireContext(), "Poziv prihvaćen", Toast.LENGTH_SHORT).show();
            renderNotification();
        });

        binding.btnDeclineInvite.setOnClickListener(v -> {
            if (notificationItem == null) {
                return;
            }

            notificationsService.declineInvitation(notificationItem);
            updateNotificationInFirestore(notificationItem);
            Toast.makeText(requireContext(), "Poziv odbijen", Toast.LENGTH_SHORT).show();
            renderNotification();
        });
    }

    private void updateNotificationInFirestore(NotificationItem item) {
        NotificationDocument document = notificationsMapper.toDocument(item, TEST_USER_ID);

        notificationsRepository.updateNotification(document)
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Greška pri ažuriranju notifikacije",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}