package com.example.slagalica.domain.model.progression;

public class RegionStats {
    private String regionKey;
    private String icon;
    private long totalMonthlyStars;
    private long totalPlayers;
    private long activePlayers;
    private long firstPlaces;
    private long secondPlaces;
    private long thirdPlaces;
    private int rank;

    public RegionStats(String regionKey, String icon) {
        this.regionKey = regionKey;
        this.icon = icon;
    }

    public String getRegionKey()                          { return regionKey; }
    public String getIcon()                               { return icon; }
    public long   getTotalMonthlyStars()                  { return totalMonthlyStars; }
    public long   getTotalPlayers()                       { return totalPlayers; }
    public long   getActivePlayers()                      { return activePlayers; }
    public long   getFirstPlaces()                        { return firstPlaces; }
    public long   getSecondPlaces()                       { return secondPlaces; }
    public long   getThirdPlaces()                        { return thirdPlaces; }
    public int    getRank()                               { return rank; }

    public void setTotalMonthlyStars(long v)              { totalMonthlyStars = v; }
    public void setTotalPlayers(long v)                   { totalPlayers = v; }
    public void setActivePlayers(long v)                  { activePlayers = v; }
    public void setFirstPlaces(long v)                    { firstPlaces = v; }
    public void setSecondPlaces(long v)                   { secondPlaces = v; }
    public void setThirdPlaces(long v)                    { thirdPlaces = v; }
    public void setRank(int rank)                         { this.rank = rank; }
}
