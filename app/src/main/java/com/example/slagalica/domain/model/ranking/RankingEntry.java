package com.example.slagalica.domain.model.ranking;

public class RankingEntry {

    private String userId;
    private String username;
    private String league;
    private long starsEarned;
    private long gamesPlayed;
    private long updatedAtMillis;

    public RankingEntry() {
    }

    public RankingEntry(
            String userId,
            String username,
            String league,
            long starsEarned,
            long gamesPlayed,
            long updatedAtMillis
    ) {
        this.userId = userId;
        this.username = username;
        this.league = league;
        this.starsEarned = starsEarned;
        this.gamesPlayed = gamesPlayed;
        this.updatedAtMillis = updatedAtMillis;
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

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public long getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(long starsEarned) {
        this.starsEarned = starsEarned;
    }

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(long gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public void setUpdatedAtMillis(long updatedAtMillis) {
        this.updatedAtMillis = updatedAtMillis;
    }
}
