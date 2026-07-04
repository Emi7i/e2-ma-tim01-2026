package com.example.slagalica.domain.model.match;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class MatchmakingEntry {
    @DocumentId
    private String userId;

    @ServerTimestamp
    private Date queuedAt;

    private String matchedWith;
    private String matchId;

    public MatchmakingEntry(String userId) {
        this.userId = userId;
    }
}
