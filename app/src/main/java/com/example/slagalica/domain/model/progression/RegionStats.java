package com.example.slagalica.domain.model.progression;

public class RegionStats {
    private String regionKey;
    private String icon;
    private long totalMonthlyStars;
    private long totalPlayers;
    private int rank;

    public RegionStats(String regionKey, String icon) {
        this.regionKey = regionKey;
        this.icon = icon;
        this.totalMonthlyStars = 0;
        this.totalPlayers = 0;
        this.rank = 0;
    }

    public String getRegionKey() { return regionKey; }
    public String getIcon() { return icon; }
    public long getTotalMonthlyStars() { return totalMonthlyStars; }
    public long getTotalPlayers() { return totalPlayers; }
    public int getRank() { return rank; }

    public void addPlayer(long monthlyStars) {
        totalPlayers++;
        totalMonthlyStars += monthlyStars;
    }

    public void setRank(int rank) { this.rank = rank; }
}
