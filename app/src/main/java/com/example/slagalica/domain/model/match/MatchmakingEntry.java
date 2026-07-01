package com.example.slagalica.domain.model.match;

import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingEntry {
    @DocumentId
    private String userId;
}
