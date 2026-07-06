package com.example.slagalica.domain.model.tournament;

import java.util.ArrayList;
import java.util.List;

public class TournamentSession {

    private String tournamentId;
    private String status;
    private long createdAtMillis;
    private long updatedAtMillis;
    private String winnerId;
    private List<String> playerIds = new ArrayList<>();
    private List<TournamentParticipant> participants = new ArrayList<>();
    private List<TournamentMatch> matches = new ArrayList<>();

    public TournamentSession() {
    }

    public TournamentSession(
            String tournamentId,
            TournamentStatus status,
            long createdAtMillis,
            long updatedAtMillis,
            List<String> playerIds
    ) {
        this.tournamentId = tournamentId;
        this.status = status.name();
        this.createdAtMillis = createdAtMillis;
        this.updatedAtMillis = updatedAtMillis;
        this.playerIds = playerIds;
    }

    public TournamentStatus getStatusEnum() {
        if (status == null) {
            return TournamentStatus.WAITING_FOR_PLAYERS;
        }
        return TournamentStatus.valueOf(status);
    }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
    public long getUpdatedAtMillis() { return updatedAtMillis; }
    public void setUpdatedAtMillis(long updatedAtMillis) { this.updatedAtMillis = updatedAtMillis; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    public List<String> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<String> playerIds) { this.playerIds = playerIds; }
    public List<TournamentParticipant> getParticipants() { return participants; }
    public void setParticipants(List<TournamentParticipant> participants) { this.participants = participants; }
    public List<TournamentMatch> getMatches() { return matches; }
    public void setMatches(List<TournamentMatch> matches) { this.matches = matches; }
}
