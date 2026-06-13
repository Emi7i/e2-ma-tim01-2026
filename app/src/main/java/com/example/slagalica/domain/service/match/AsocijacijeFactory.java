package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsocijacijeFactory {

    private final AsocijacijeMapper mapper = new AsocijacijeMapper();

    public List<Asocijacija> createRounds(List<AsocijacijaDocument> documents) {
        List<AsocijacijaDocument> shuffled = new ArrayList<>(documents);
        Collections.shuffle(shuffled);

        List<Asocijacija> rounds = new ArrayList<>();

        if (shuffled.size() > 0) {
            rounds.add(mapper.toRuntime(shuffled.get(0), 1, 1));
        }

        if (shuffled.size() > 1) {
            rounds.add(mapper.toRuntime(shuffled.get(1), 2, 2));
        }

        return rounds;
    }
}