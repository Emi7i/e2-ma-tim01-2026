package com.example.slagalica.domain.model.tournament;

public class TournamentResultUi {

    private final String title;
    private final String message;
    private final boolean winner;

    public TournamentResultUi(String title, String message, boolean winner) {
        this.title = title;
        this.message = message;
        this.winner = winner;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isWinner() {
        return winner;
    }
}