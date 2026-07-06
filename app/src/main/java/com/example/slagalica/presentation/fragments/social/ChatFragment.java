package com.example.slagalica.presentation.fragments.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slagalica.databinding.FragmentChatBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.social.ChatMessage;
import com.example.slagalica.domain.service.social.ChatNotificationService;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.adapters.ChatMessageAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    @Inject
    ChatNotificationService chatNotificationService;

    private FragmentChatBinding binding;
    private ChatMessageAdapter adapter;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppActivity) requireActivity()).setToolbarTitle("Čet");

        adapter = new ChatMessageAdapter(sessionManager.getCurrentUserId());
        binding.chatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatRecycler.setAdapter(adapter);

        binding.sendButton.setOnClickListener(v -> sendMessage());

        chatNotificationService.getMessages().observe(getViewLifecycleOwner(), this::renderMessages);
        chatNotificationService.startListening();
    }

    private void sendMessage() {
        String text = binding.messageInput.getText() != null
                ? binding.messageInput.getText().toString()
                : "";

        if (text.trim().isEmpty()) {
            return;
        }

        chatNotificationService.sendMessage(text);
        binding.messageInput.setText("");
    }

    private void renderMessages(List<ChatMessage> messages) {
        adapter.submitList(messages);

        boolean empty = messages == null || messages.isEmpty();
        binding.chatEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.chatRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (!empty) {
            binding.chatRecycler.scrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        chatNotificationService.setChatScreenVisible(true);
    }

    @Override
    public void onPause() {
        chatNotificationService.setChatScreenVisible(false);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.chatRecycler.setAdapter(null);
        binding = null;
    }
}
