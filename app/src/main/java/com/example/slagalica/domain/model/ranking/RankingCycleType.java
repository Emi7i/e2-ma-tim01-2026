package com.example.slagalica.domain.model.ranking;

public enum RankingCycleType {
    WEEKLY("Nedeljna"),
    MONTHLY("Mesečna");

    private final String displayName;

    RankingCycleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
