package com.example.slagalica.domain.model.tournament;

public class TournamentParticipant {

    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String league;
    private long numStars;
    private int seed;
    private boolean eliminated;
    private boolean winner;

    public TournamentParticipant() {
    }

    public TournamentParticipant(
            String userId,
            String username,
            String email,
            String avatarUrl,
            String league,
            long numStars,
            int seed,
            boolean eliminated,
            boolean winner
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.league = league;
        this.numStars = numStars;
        this.seed = seed;
        this.eliminated = eliminated;
        this.winner = winner;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public long getNumStars() {
        return numStars;
    }

    public void setNumStars(long numStars) {
        this.numStars = numStars;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
}
