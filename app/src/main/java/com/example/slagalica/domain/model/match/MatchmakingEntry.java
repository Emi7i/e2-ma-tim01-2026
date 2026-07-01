package com.example.slagalica.domain.model.match;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingEntry {
    @DocumentId
    private String userId;

    @ServerTimestamp
    private Date queuedAt;

    public MatchmakingEntry(String userId) {
        this.userId = userId;
    }
}
