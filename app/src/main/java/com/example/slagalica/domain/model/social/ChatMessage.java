package com.example.slagalica.domain.model.social;

import com.google.firebase.firestore.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @DocumentId
    private String id;
    private String region;
    private String senderId;
    private String senderName;
    private String text;
    private long timestampMillis;
}
