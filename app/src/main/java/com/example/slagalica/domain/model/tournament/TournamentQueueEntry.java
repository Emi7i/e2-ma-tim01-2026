package com.example.slagalica.domain.model.tournament;

public class TournamentQueueEntry {

    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String league;
    private long numStars;
    private long createdAtMillis;

    public TournamentQueueEntry() {
    }

    public TournamentQueueEntry(
            String userId,
            String username,
            String email,
            String avatarUrl,
            String league,
            long numStars,
            long createdAtMillis
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.league = league;
        this.numStars = numStars;
        this.createdAtMillis = createdAtMillis;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }
    public long getNumStars() { return numStars; }
    public void setNumStars(long numStars) { this.numStars = numStars; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
}
