package com.example.slagalica.domain.model.progression;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserStatistics {
    @DocumentId
    private String userId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
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

    // Point Tracking (for average points)
    private long koZnaZnaPoints;
    private long mojBrojPoints;
    private long korakPoKorakPoints;
    private long asocijacijePoints;
    private long skockoPoints;
    private long spojnicePoints;

    // Per-game match counts (to calculate averages correctly)
    private long koZnaZnaPlayed;
    private long mojBrojPlayed;
    private long korakPoKorakPlayed;
    private long asocijacijePlayed;
    private long skockoPlayed;
    private long spojnicePlayed;

    // Granular Tracking (Requirements ii, iv, v, vi)
    private long asocijacijeSolved; // Rounds where final solution was found
    private long asocijacijeTotalRounds;

    // For Korak po korak breakdown: Success at step 0-6
    private List<Long> korakPoKorakStepSuccessCount;

    // For Skocko breakdown: Success at attempt 0-6 (6 is bonus)
    private List<Long> skockoAttemptSuccessCount;

    public static UserStatistics createNew(String userId) {
        UserStatistics stats = new UserStatistics();
        stats.setUserId(userId);
        stats.korakPoKorakStepSuccessCount = new java.util.ArrayList<>(java.util.Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L));
        stats.skockoAttemptSuccessCount = new java.util.ArrayList<>(java.util.Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L));
        return stats;
    }

    public void calculateOverallStats() {
        int count = 0;
        double sum = 0;

        // Recalculate game percentages based on weighting or raw accuracy
        // Note: Individual game accuracy calculations moved to ViewModels for now,
        // but overall stats uses them.

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
