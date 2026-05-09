package com.example.slagalica.domain.model.match.games;

import java.util.ArrayList;
import java.util.List;

public class SkockoPokusaj {

    private final List<SkockoPolje> guess;
    private final List<String> feedback;
    private boolean submitted;

    public SkockoPokusaj() {
        this.guess = new ArrayList<>();
        this.feedback = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            guess.add(new SkockoPolje());
            feedback.add("EMPTY");
        }
        this.submitted = false;
    }

    public List<SkockoPolje> getGuess() {
        return guess;
    }

    public List<String> getFeedback() {
        return feedback;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}