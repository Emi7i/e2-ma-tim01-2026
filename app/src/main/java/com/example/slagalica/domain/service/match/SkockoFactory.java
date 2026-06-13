package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.SkockoCombinationDocument;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkockoFactory {

    private final SkockoMapper mapper = new SkockoMapper();

    public List<SkockoTabla> createRounds(List<SkockoCombinationDocument> documents) {
        List<SkockoCombinationDocument> shuffled = new ArrayList<>(documents);
        Collections.shuffle(shuffled);

        List<SkockoTabla> rounds = new ArrayList<>();

        if (shuffled.size() > 0) {
            rounds.add(mapper.toRuntime(shuffled.get(0), 1, 1));
        }

        if (shuffled.size() > 1) {
            rounds.add(mapper.toRuntime(shuffled.get(1), 2, 2));
        }

        return rounds;
    }
}