package com.example.slagalica.domain.model.progression;

import com.google.firebase.firestore.DocumentId;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegionStatsDocument {
    @DocumentId
    private String regionKey;
    private long registeredPlayers;
    private long activePlayers;
    private long totalMonthlyStars;
    private long firstPlaces;
    private long secondPlaces;
    private long thirdPlaces;
}
