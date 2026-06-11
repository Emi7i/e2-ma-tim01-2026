package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkockoDemoFactory {

    public List<SkockoTabla> createDemoRounds() {
        List<SkockoTabla> rounds = new ArrayList<>();
        rounds.add(createRoundOne());
        rounds.add(createRoundTwo());
        return rounds;
    }
    private SkockoTabla createRoundOne() {
        return new SkockoTabla(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        1,
                        1,
                        1,
                        30,
                        0,
                        0,
                        false,
                        false
                ),
                Arrays.asList("★", "♠", "💥", "♠"),
                createRegularAttempts(),
                new SkockoPokusaj(),
                0,
                false,
                false,
                false
        );
    }

    private SkockoTabla createRoundTwo() {
        return new SkockoTabla(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        2,
                        2,
                        2,
                        30,
                        0,
                        0,
                        false,
                        false
                ),
                Arrays.asList("♥", "♣", "♣", "♦"),
                createRegularAttempts(),
                new SkockoPokusaj(),
                0,
                false,
                false,
                false
        );
    }

    private List<SkockoPokusaj> createRegularAttempts() {
        List<SkockoPokusaj> attempts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            attempts.add(new SkockoPokusaj());
        }
        return attempts;
    }
}