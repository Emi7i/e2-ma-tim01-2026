package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkockoDemoFactory {

    public SkockoTabla createDemoTabla() {
        List<String> secret = Arrays.asList("★", "♠", "💥", "♠");

        List<SkockoPokusaj> attempts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            attempts.add(new SkockoPokusaj());
        }

        return new SkockoTabla(
                "Igrač 1",
                "Igrač 2",
                "02:00",
                secret,
                attempts,
                0,
                1,
                false,
                false,
                ""
        );
    }
}