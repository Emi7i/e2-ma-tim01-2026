package com.example.slagalica.domain.model.progression;

/**
 * The six leagues a player progresses through, ordered from the starting
 * league to the highest. Ordinal doubles as the daily token bonus (spec 2b)
 * since both happen to be "how many leagues above the starter league".
 */
public enum League {
    POCETNIK("Početnik", 0L),
    STUDENT("Student", 100L),
    PRAKTIKANT("Praktikant", 200L),
    ZAPOSLENI("Zaposleni", 400L),
    INZENJER("Inženjer", 800L),
    DIPLOMIRANI_INZENJER("Diplomirani inženjer", 1600L);

    private final String displayName;
    private final long starsRequired;

    League(String displayName, long starsRequired) {
        this.displayName = displayName;
        this.starsRequired = starsRequired;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getStarsRequired() {
        return starsRequired;
    }

    public int getTokenBonus() {
        return ordinal();
    }

    // Highest league whose star requirement the given total star count satisfies.
    public static League fromStars(long numStars) {
        League result = POCETNIK;
        for (League league : values()) {
            if (numStars >= league.starsRequired) {
                result = league;
            }
        }
        return result;
    }

    public static League fromDisplayName(String name) {
        if (name == null) {
            return POCETNIK;
        }
        for (League league : values()) {
            if (league.displayName.equalsIgnoreCase(name)) {
                return league;
            }
        }
        return POCETNIK;
    }
}
