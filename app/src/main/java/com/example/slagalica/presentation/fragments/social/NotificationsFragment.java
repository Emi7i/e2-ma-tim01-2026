package com.example.slagalica.presentation.fragments.social;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentNotificationsBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.social.NotificationActionStatus;
import com.example.slagalica.domain.model.social.NotificationDocument;
import com.example.slagalica.domain.model.social.NotificationFilter;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.model.social.NotificationTarget;
import com.example.slagalica.domain.model.social.NotificationType;
import com.example.slagalica.domain.service.social.NotificationsMapper;
import com.example.slagalica.domain.service.social.NotificationsService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.notifications.AppNotificationHelper;
import com.example.slagalica.repository.impl.NotificationsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {

    @Inject
    SessionManager sessionManager;
    private FragmentNotificationsBinding binding;

    @Inject
    NotificationsRepository notificationsRepository;

    private NotificationsService notificationsService;
    private final NotificationsMapper notificationsMapper = new NotificationsMapper();
    private final List<NotificationItem> notificationItems = new ArrayList<>();

    private NotificationFilter currentFilter = NotificationFilter.ALL;
    private int demoNotificationIndex = 0;

    public NotificationsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppActivity) requireActivity()).setToolbarTitle("Notifikacije");

        notificationsService = new NotificationsService();

        binding.btnSendDemoNotification.setOnClickListener(v -> {
            sendDemoNotification();
        });

        setupFilterButtons();
        loadNotificationsFromFirestore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadNotificationsFromFirestore() {
        notificationsRepository.getNotificationsForUser(sessionManager.getCurrentUserId())
                .thenAccept(documents -> {
                    requireActivity().runOnUiThread(() -> {
                        notificationItems.clear();

                        for (NotificationDocument doc : documents) {
                            notificationItems.add(notificationsMapper.toRuntime(doc));
                        }

                        renderNotifications();
                    });
                });
    }

    private void sendDemoNotification() {
        NotificationItem demoItem;

        int caseIndex = demoNotificationIndex % 4;

        if (caseIndex == 0) {
            demoItem = new NotificationItem(
                    "runtime_chat_" + System.currentTimeMillis(),
                    NotificationType.CHAT,
                    "Nova poruka",
                    "Ivana vam je poslala poruku u čet.",
                    "Ivana",
                    System.currentTimeMillis(),
                    false,
                    true,
                    false,
                    NotificationTarget.CHAT,
                    NotificationActionStatus.NONE
            );
        } else if (caseIndex == 1) {
            demoItem = new NotificationItem(
                    "runtime_ranking_" + System.currentTimeMillis(),
                    NotificationType.RANKING,
                    "Rang lista",
                    "Završili ste ciklus na 2. mestu.",
                    "Sistem",
                    System.currentTimeMillis(),
                    false,
                    true,
                    false,
                    NotificationTarget.RANKING,
                    NotificationActionStatus.NONE
            );
        } else if (caseIndex == 2) {
            demoItem = new NotificationItem(
                    "runtime_reward_" + System.currentTimeMillis(),
                    NotificationType.REWARD,
                    "Nagrada",
                    "Osvojili ste 10 tokena za plasman na rang listi.",
                    "Sistem",
                    System.currentTimeMillis(),
                    false,
                    true,
                    false,
                    NotificationTarget.REWARD,
                    NotificationActionStatus.NONE
            );
        } else {
            demoItem = new NotificationItem(
                    "runtime_invite_" + System.currentTimeMillis(),
                    NotificationType.GAME_INVITE,
                    "Poziv u igru",
                    "Petar vas je pozvao u partiju.",
                    "Petar",
                    System.currentTimeMillis(),
                    false,
                    true,
                    true,
                    NotificationTarget.GAME_INVITE,
                    NotificationActionStatus.PENDING
            );
        }

        demoNotificationIndex++;

        NotificationDocument document = notificationsMapper.toDocument(demoItem, sessionManager.getCurrentUserId());

        notificationsRepository.saveNotification(document)
                .thenAccept(v -> requireActivity().runOnUiThread(() -> {
                    AppNotificationHelper.showSystemNotification(requireContext(), demoItem);
                    Toast.makeText(requireContext(), "Demo notifikacija poslata", Toast.LENGTH_SHORT).show();
                    loadNotificationsFromFirestore();
                }))
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Greška pri čuvanju demo notifikacije",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }

    private void setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener(v -> {
            currentFilter = NotificationFilter.ALL;
            updateFilterButtons();
            renderNotifications();
        });

        binding.btnFilterUnread.setOnClickListener(v -> {
            currentFilter = NotificationFilter.UNREAD;
            updateFilterButtons();
            renderNotifications();
        });

        binding.btnFilterRead.setOnClickListener(v -> {
            currentFilter = NotificationFilter.READ;
            updateFilterButtons();
            renderNotifications();
        });

        updateFilterButtons();
    }

    private void updateFilterButtons() {
        binding.btnFilterAll.setEnabled(currentFilter != NotificationFilter.ALL);
        binding.btnFilterUnread.setEnabled(currentFilter != NotificationFilter.UNREAD);
        binding.btnFilterRead.setEnabled(currentFilter != NotificationFilter.READ);
    }

    private void renderNotifications() {
        binding.notificationsContainer.removeAllViews();

        List<NotificationItem> filtered = getFilteredNotifications();

        if (filtered.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Nema notifikacija za izabrani filter.");
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            emptyView.setPadding(0, dp(16), 0, 0);
            binding.notificationsContainer.addView(emptyView);
            return;
        }

        for (NotificationItem item : filtered) {
            binding.notificationsContainer.addView(createNotificationCard(item));
        }
    }

    private List<NotificationItem> getFilteredNotifications() {
        List<NotificationItem> filtered = new ArrayList<>();

        for (NotificationItem item : notificationItems) {
            if (currentFilter == NotificationFilter.ALL) {
                filtered.add(item);
            } else if (currentFilter == NotificationFilter.READ && item.isRead()) {
                filtered.add(item);
            } else if (currentFilter == NotificationFilter.UNREAD && !item.isRead()) {
                filtered.add(item);
            }
        }

        return filtered;
    }

    private LinearLayout createNotificationCard(NotificationItem item) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_notification_card);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);

        TextView titleView = new TextView(requireContext());
        titleView.setText(item.getTitle());
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        TextView typeView = new TextView(requireContext());
        typeView.setText(notificationsService.getTypeLabel(item.getType()));
        typeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        typeView.setTextColor(0xFF666666);

        TextView senderView = new TextView(requireContext());
        senderView.setText("Od: " + item.getSender());
        senderView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        senderView.setTextColor(0xFF666666);

        TextView timeView = new TextView(requireContext());
        timeView.setText("Vreme: " + notificationsService.formatTimestamp(item.getTimestampMillis()));
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        timeView.setTextColor(0xFF666666);

        TextView statusView = new TextView(requireContext());
        statusView.setText(notificationsService.getStatusLabel(item));
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusView.setTextColor(item.isRead() ? 0xFF2E7D32 : 0xFFC62828);

        TextView actionStatusView = new TextView(requireContext());
        actionStatusView.setText(notificationsService.getActionStatusLabel(item));
        actionStatusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        actionStatusView.setTextColor(0xFF666666);

        TextView messageView = new TextView(requireContext());
        messageView.setText(item.getMessage());
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        messageView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        messageView.setPadding(0, dp(8), 0, dp(10));

        LinearLayout actionsContainer = new LinearLayout(requireContext());
        actionsContainer.setOrientation(LinearLayout.VERTICAL);
        actionsContainer.setGravity(Gravity.END);

        LinearLayout firstRow = createActionsRow();
        List<com.google.android.material.button.MaterialButton> buttons = new ArrayList<>();

        if (item.hasOpenAction()) {
            com.google.android.material.button.MaterialButton openButton = createActionButton("Otvori", true);
            openButton.setOnClickListener(v -> openNotification(item));
            buttons.add(openButton);
        }

        com.google.android.material.button.MaterialButton markButton =
                createActionButton(item.isRead() ? "Označi nepročitano" : "Označi pročitano", false);
        markButton.setOnClickListener(v -> {
            item.setRead(!item.isRead());
            updateNotificationInFirestore(item);
        });
        buttons.add(markButton);

        for (com.google.android.material.button.MaterialButton button : buttons) {
            firstRow.addView(button);
        }

        if (firstRow.getChildCount() > 0) {
            actionsContainer.addView(firstRow);
        }

        card.setOnClickListener(v -> openNotification(item));

        card.addView(titleView);
        card.addView(typeView);
        card.addView(senderView);
        card.addView(timeView);
        card.addView(statusView);
        card.addView(actionStatusView);
        card.addView(messageView);
        card.addView(actionsContainer);

        return card;
    }

    private void openNotification(NotificationItem item) {
        item.setRead(true);
        updateNotificationInFirestore(item);

        FragmentTransition.to(
                NotificationTargetPlaceholderFragment.newInstance(item.getId()),
                requireActivity(),
                true,
                R.id.appContainer
        );
    }

    private void updateNotificationInFirestore(NotificationItem item) {
        NotificationDocument document = notificationsMapper.toDocument(item, sessionManager.getCurrentUserId());

        notificationsRepository.updateNotification(document)
                .thenAccept(v -> requireActivity().runOnUiThread(this::loadNotificationsFromFirestore))
                .exceptionally(e -> {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Greška pri ažuriranju notifikacije",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }

    private LinearLayout createActionsRow() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.END);
        return row;
    }

    private com.google.android.material.button.MaterialButton createActionButton(String text, boolean primary) {
        com.google.android.material.button.MaterialButton button =
                new com.google.android.material.button.MaterialButton(requireContext());

        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = dp(8);
        button.setLayoutParams(params);

        if (primary) {
            button.setBackgroundResource(R.drawable.bg_notification_action_button);
            button.setTextColor(0xFFFFFFFF);
        } else {
            button.setBackgroundResource(R.drawable.bg_notification_secondary_button);
            button.setTextColor(0xFFFFFFFF);
        }

        return button;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                requireContext().getResources().getDisplayMetrics()
        );
    }
}