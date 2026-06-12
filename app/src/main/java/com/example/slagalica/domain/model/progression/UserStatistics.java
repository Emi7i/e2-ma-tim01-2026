package com.example.slagalica.domain.model.progression;

import com.google.firebase.firestore.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    @DocumentId
    private String userId;
    private double overallStats;
    private double koZnaZna;
    private double mojBroj;
    private double korakPoKorak;
    private double asocijacije;
    private double skocko;
    private double spojnice;
    private long gamesPlayed;
    private long wonGames;
}
