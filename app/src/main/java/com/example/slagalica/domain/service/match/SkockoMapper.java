package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.List;

public class SkockoMapper {

    public SkockoTabla toRuntime(SkockoCombinationDocument source, int roundNumber, int startingPlayer) {
        List<SkockoPokusaj> attempts = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            attempts.add(new SkockoPokusaj());
        }

        TwoPlayerGameState gameState = new TwoPlayerGameState(
                "Igrač 1",
                "Igrač 2",
                roundNumber,
                startingPlayer,
                startingPlayer,
                30,
                0,
                0,
                false,
                false
        );

        return new SkockoTabla(
                gameState,
                source.getCombination(),
                attempts,
                new SkockoPokusaj(),
                0,
                false,
                false,
                false
        );
    }
}