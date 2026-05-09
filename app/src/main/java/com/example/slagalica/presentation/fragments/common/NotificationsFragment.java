package com.example.slagalica.presentation.fragments.common;

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

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private final List<NotificationUiItem> allNotifications = new ArrayList<>();
    private NotificationFilter currentFilter = NotificationFilter.ALL;

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

        seedNotifications();
        setupFilterButtons();
        renderNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void seedNotifications() {
        allNotifications.clear();

        allNotifications.add(new NotificationUiItem(
                NotificationType.REWARD,
                "Nagrada",
                "Osvojili ste 5 tokena za plasman na rang listi.",
                "Sistem",
                "Danas u 11:05",
                false,
                true,
                false
        ));

        allNotifications.add(new NotificationUiItem(
                NotificationType.GAME_INVITE,
                "Poziv u igru",
                "Marko vas je pozvao u prijateljsku partiju.",
                "Marko",
                "Danas u 10:42",
                false,
                true,
                true
        ));

        allNotifications.add(new NotificationUiItem(
                NotificationType.CHAT,
                "Nova poruka",
                "Ivana vam je poslala poruku u čet.",
                "Ivana",
                "Juče u 22:18",
                true,
                true,
                false
        ));

        allNotifications.add(new NotificationUiItem(
                NotificationType.LEAGUE,
                "Nova liga",
                "Prešli ste u višu ligu. Čestitamo!",
                "Sistem",
                "Juče u 20:10",
                false,
                true,
                false
        ));

        allNotifications.add(new NotificationUiItem(
                NotificationType.RANKING,
                "Rang lista",
                "Završili ste ciklus na 3. mestu.",
                "Sistem",
                "24.04.2026. u 18:30",
                true,
                true,
                false
        ));
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

        List<NotificationUiItem> filtered = getFilteredNotifications();

        if (filtered.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Nema notifikacija za izabrani filter.");
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            emptyView.setPadding(0, dp(16), 0, 0);
            binding.notificationsContainer.addView(emptyView);
            return;
        }

        for (NotificationUiItem item : filtered) {
            binding.notificationsContainer.addView(createNotificationCard(item));
        }
    }

    private List<NotificationUiItem> getFilteredNotifications() {
        List<NotificationUiItem> filtered = new ArrayList<>();

        for (NotificationUiItem item : allNotifications) {
            if (currentFilter == NotificationFilter.ALL) {
                filtered.add(item);
            } else if (currentFilter == NotificationFilter.READ && item.isRead) {
                filtered.add(item);
            } else if (currentFilter == NotificationFilter.UNREAD && !item.isRead) {
                filtered.add(item);
            }
        }

        return filtered;
    }

    private LinearLayout createNotificationCard(NotificationUiItem item) {
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
        titleView.setText(item.title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        TextView typeView = new TextView(requireContext());
        typeView.setText(getTypeLabel(item.type));
        typeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        typeView.setTextColor(0xFF666666);

        TextView senderView = new TextView(requireContext());
        senderView.setText("Od: " + item.sender);
        senderView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        senderView.setTextColor(0xFF666666);

        TextView timeView = new TextView(requireContext());
        timeView.setText("Vreme: " + item.timestamp);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        timeView.setTextColor(0xFF666666);

        TextView statusView = new TextView(requireContext());
        statusView.setText(item.isRead ? "Status: Pročitano" : "Status: Nepročitano");
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusView.setTextColor(item.isRead ? 0xFF2E7D32 : 0xFFC62828);

        TextView messageView = new TextView(requireContext());
        messageView.setText(item.message);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        messageView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        messageView.setPadding(0, dp(8), 0, dp(10));

        LinearLayout actionsContainer = new LinearLayout(requireContext());
        actionsContainer.setOrientation(LinearLayout.VERTICAL);
        actionsContainer.setGravity(Gravity.END);

        LinearLayout firstRow = createActionsRow();
        LinearLayout secondRow = createActionsRow();

        List<com.google.android.material.button.MaterialButton> buttons = new ArrayList<>();

        if (item.hasOpenAction) {
            com.google.android.material.button.MaterialButton openButton = createActionButton("Otvori", true);
            openButton.setOnClickListener(v -> {
                item.isRead = true;
                FragmentTransition.to(
                        NotificationTargetPlaceholderFragment.newInstance(
                                item.title,
                                item.message,
                                getTypeLabel(item.type),
                                item.sender,
                                item.timestamp,
                                item.isRead ? "Pročitano" : "Nepročitano",
                                item.hasDecisionAction ? "Prihvati / Odbij" : "Otvori"
                        ),
                        requireActivity(),
                        true,
                        R.id.appContainer
                );
            });
            buttons.add(openButton);
        }

        if (item.hasDecisionAction) {
            com.google.android.material.button.MaterialButton acceptButton = createActionButton("Prihvati", true);
            acceptButton.setOnClickListener(v -> {
                item.isRead = true;
                Toast.makeText(requireContext(), "Poziv prihvaćen (GUI)", Toast.LENGTH_SHORT).show();
                renderNotifications();
            });
            buttons.add(acceptButton);

            com.google.android.material.button.MaterialButton declineButton = createActionButton("Odbij", true);
            declineButton.setOnClickListener(v -> {
                item.isRead = true;
                Toast.makeText(requireContext(), "Poziv odbijen (GUI)", Toast.LENGTH_SHORT).show();
                renderNotifications();
            });
            buttons.add(declineButton);
        }

        com.google.android.material.button.MaterialButton markButton =
                createActionButton(item.isRead ? "Označi nepročitano" : "Označi pročitano", false);
        markButton.setOnClickListener(v -> {
            item.isRead = !item.isRead;
            renderNotifications();
        });
        buttons.add(markButton);

        for (int i = 0; i < buttons.size(); i++) {
            if (i < 2) {
                firstRow.addView(buttons.get(i));
            } else {
                secondRow.addView(buttons.get(i));
            }
        }

        if (firstRow.getChildCount() > 0) {
            actionsContainer.addView(firstRow);
        }
        if (secondRow.getChildCount() > 0) {
            LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            row2Params.topMargin = dp(8);
            secondRow.setLayoutParams(row2Params);
            actionsContainer.addView(secondRow);
        }

        card.setOnClickListener(v -> {
            item.isRead = true;
            FragmentTransition.to(
                    NotificationTargetPlaceholderFragment.newInstance(
                            item.title,
                            item.message,
                            getTypeLabel(item.type),
                            item.sender,
                            item.timestamp,
                            item.isRead ? "Pročitano" : "Nepročitano",
                            item.hasDecisionAction ? "Prihvati / Odbij" : "Otvori"
                    ),
                    requireActivity(),
                    true,
                    R.id.appContainer
            );
        });

        card.addView(titleView);
        card.addView(typeView);
        card.addView(senderView);
        card.addView(timeView);
        card.addView(statusView);
        card.addView(messageView);
        card.addView(actionsContainer);

        return card;
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

    private String getTypeLabel(NotificationType type) {
        switch (type) {
            case REWARD:
                return "Tip: Nagrada";
            case GAME_INVITE:
                return "Tip: Poziv u igru";
            case CHAT:
                return "Tip: Čet";
            case LEAGUE:
                return "Tip: Liga";
            case RANKING:
                return "Tip: Rang lista";
            default:
                return "Tip: Ostalo";
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                requireContext().getResources().getDisplayMetrics()
        );
    }

    private enum NotificationFilter {
        ALL,
        READ,
        UNREAD
    }

    private enum NotificationType {
        REWARD,
        GAME_INVITE,
        CHAT,
        LEAGUE,
        RANKING
    }

    private static class NotificationUiItem {
        NotificationType type;
        String title;
        String message;
        String sender;
        String timestamp;
        boolean isRead;
        boolean hasOpenAction;
        boolean hasDecisionAction;

        NotificationUiItem(NotificationType type,
                           String title,
                           String message,
                           String sender,
                           String timestamp,
                           boolean isRead,
                           boolean hasOpenAction,
                           boolean hasDecisionAction) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.sender = sender;
            this.timestamp = timestamp;
            this.isRead = isRead;
            this.hasOpenAction = hasOpenAction;
            this.hasDecisionAction = hasDecisionAction;
        }
    }
}