package com.example.slagalica.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slagalica.databinding.ItemChatMessageIncomingBinding;
import com.example.slagalica.databinding.ItemChatMessageOutgoingBinding;
import com.example.slagalica.domain.model.social.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final String currentUserId;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault());

    public ChatMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void submitList(List<ChatMessage> newMessages) {
        messages.clear();

        if (newMessages != null) {
            messages.addAll(newMessages);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        boolean isOwnMessage = currentUserId != null && currentUserId.equals(message.getSenderId());
        return isOwnMessage ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_OUTGOING) {
            return new OutgoingViewHolder(
                    ItemChatMessageOutgoingBinding.inflate(inflater, parent, false));
        }

        return new IncomingViewHolder(
                ItemChatMessageIncomingBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String time = timeFormat.format(new Date(message.getTimestampMillis()));

        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message, time);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message, time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageIncomingBinding binding;

        IncomingViewHolder(ItemChatMessageIncomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message, String time) {
            binding.senderName.setText(
                    message.getSenderName() == null ? "Nepoznat igrač" : message.getSenderName());
            binding.messageText.setText(message.getText());
            binding.messageTime.setText(time);
        }
    }

    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageOutgoingBinding binding;

        OutgoingViewHolder(ItemChatMessageOutgoingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message, String time) {
            binding.messageText.setText(message.getText());
            binding.messageTime.setText(time);
        }
    }
}
