package com.example.slagalica.domain.model.tournament;

public class TournamentMatch {

    private String matchId;
    private String tournamentId;
    private String round;
    private int matchIndex;
    private String player1Id;
    private String player2Id;
    private String player1Username;
    private String player2Username;
    private String winnerId;
    private String loserId;
    private int player1Score;
    private int player2Score;
    private String status;
    private long createdAtMillis;
    private long startedAtMillis;
    private long finishedAtMillis;

    public TournamentMatch() {
    }

    public TournamentMatch(
            String matchId,
            String tournamentId,
            TournamentRound round,
            int matchIndex,
            String player1Id,
            String player2Id,
            String player1Username,
            String player2Username,
            TournamentMatchStatus status,
            long createdAtMillis
    ) {
        this.matchId = matchId;
        this.tournamentId = tournamentId;
        this.round = round.name();
        this.matchIndex = matchIndex;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.status = status.name();
        this.createdAtMillis = createdAtMillis;
    }

    public TournamentRound getRoundEnum() {
        if (round == null) {
            return TournamentRound.SEMIFINAL;
        }
        return TournamentRound.valueOf(round);
    }

    public TournamentMatchStatus getStatusEnum() {
        if (status == null) {
            return TournamentMatchStatus.WAITING;
        }
        return TournamentMatchStatus.valueOf(status);
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }
    public int getMatchIndex() { return matchIndex; }
    public void setMatchIndex(int matchIndex) { this.matchIndex = matchIndex; }
    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }
    public String getPlayer1Username() { return player1Username; }
    public void setPlayer1Username(String player1Username) { this.player1Username = player1Username; }
    public String getPlayer2Username() { return player2Username; }
    public void setPlayer2Username(String player2Username) { this.player2Username = player2Username; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    public String getLoserId() { return loserId; }
    public void setLoserId(String loserId) { this.loserId = loserId; }
    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
    public long getStartedAtMillis() { return startedAtMillis; }
    public void setStartedAtMillis(long startedAtMillis) { this.startedAtMillis = startedAtMillis; }
    public long getFinishedAtMillis() { return finishedAtMillis; }
    public void setFinishedAtMillis(long finishedAtMillis) { this.finishedAtMillis = finishedAtMillis; }
}
