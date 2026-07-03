package com.example.slagalica.domain.model.progression;

// Fired once per league promotion/demotion (spec 2.g) so the UI can show a
// banner (in-app) or the system can show a notification (not in-app).
public class LeagueChangeEvent {
    private final League newLeague;
    private final boolean promoted;
    private final String title;
    private final String message;

    public LeagueChangeEvent(League newLeague, boolean promoted, String title, String message) {
        this.newLeague = newLeague;
        this.promoted = promoted;
        this.title = title;
        this.message = message;
    }

    public League getNewLeague() {
        return newLeague;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
