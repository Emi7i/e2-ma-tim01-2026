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

    // Accuracy Tracking Fields
    private long koZnaZnaTotal;
    private long koZnaZnaCorrect;
    private long mojBrojTotal;
    private long mojBrojCorrect;
    private long korakPoKorakTotal;
    private long korakPoKorakCorrect;
    private long asocijacijeTotal;
    private long asocijacijeCorrect;
    private long skockoTotal;
    private long skockoCorrect;
    private long spojniceTotal;
    private long spojniceCorrect;

    public void calculateOverallStats() {
        int count = 0;
        double sum = 0;
        if (koZnaZnaTotal > 0) { sum += koZnaZna; count++; }
        if (mojBrojTotal > 0) { sum += mojBroj; count++; }
        if (korakPoKorakTotal > 0) { sum += korakPoKorak; count++; }
        if (asocijacijeTotal > 0) { sum += asocijacije; count++; }
        if (skockoTotal > 0) { sum += skocko; count++; }
        if (spojniceTotal > 0) { sum += spojnice; count++; }
        
        if (count > 0) {
            overallStats = sum / count;
        } else {
            overallStats = 0;
        }
    }
}
