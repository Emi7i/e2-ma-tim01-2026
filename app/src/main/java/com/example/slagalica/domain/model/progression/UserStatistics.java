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

    public UserStatistics(String userId) {
        this.userId = userId;
        this.overallStats = 0;
        this.koZnaZna = 0;
        this.mojBroj = 0;
        this.korakPoKorak = 0;
        this.asocijacije = 0;
        this.skocko = 0;
        this.spojnice = 0;
        this.gamesPlayed = 0L;
        this.wonGames = 0L;
        this.koZnaZnaTotal = 0L;
        this.koZnaZnaCorrect = 0L;
        this.mojBrojTotal = 0L;
        this.mojBrojCorrect = 0L;
        this.korakPoKorakTotal = 0L;
        this.korakPoKorakCorrect = 0L;
        this.asocijacijeTotal = 0L;
        this.asocijacijeCorrect = 0L;
        this.skockoTotal = 0L;
        this.skockoCorrect = 0L;
        this.spojniceTotal = 0L;
        this.spojniceCorrect = 0L;
    }

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
