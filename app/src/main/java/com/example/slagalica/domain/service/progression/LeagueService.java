package com.example.slagalica.domain.service.progression;

import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.League;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Singleton;

// Only reacts to a star count, no notion of cycles/weeks/months. Doesn't persist —
// callers mutate the UserProfile and save it themselves.
@Singleton
public class LeagueService {

    public static final long BASE_DAILY_TOKENS = 5L;
    // Ukoliko se igrač ne plasira na mesečnoj rang listi, gubi 30% zvezda.
    private static final double NON_PLACEMENT_PENALTY_RATIO = 0.30;

    @Inject
    public LeagueService() {
    }

    // Re-applies the league implied by the current star count.
    public void syncLeague(UserProfile profile) {
        profile.setLeague(League.fromStars(profile.getNumStars()).getDisplayName());
    }

    public long dailyTokensFor(UserProfile profile) {
        return BASE_DAILY_TOKENS + League.fromStars(profile.getNumStars()).getTokenBonus();
    }

    // Grants once per calendar day. Returns true if tokens were granted (profile changed).
    public boolean grantDailyTokensIfNeeded(UserProfile profile) {
        String today = LocalDate.now().toString();
        if (today.equals(profile.getLastTokenGrantDate())) {
            return false;
        }
        profile.setNumTokens(profile.getNumTokens() + dailyTokensFor(profile));
        profile.setLastTokenGrantDate(today);
        return true;
    }

    // Call once per player that doesn't place on a rank list when its cycle ends.
    public void applyNonPlacementPenalty(UserProfile profile) {
        long penalty = Math.round(profile.getNumStars() * NON_PLACEMENT_PENALTY_RATIO);
        profile.setNumStars(Math.max(0, profile.getNumStars() - penalty));
        syncLeague(profile);
    }
}
