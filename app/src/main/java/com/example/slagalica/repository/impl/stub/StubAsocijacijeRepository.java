package com.example.slagalica.repository.impl.stub;

import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.service.match.AsocijacijeDemoFactory;
import com.example.slagalica.repository.impl.AsocijacijeRepository;

import java.util.List;

public class StubAsocijacijeRepository implements AsocijacijeRepository {

    private final AsocijacijeDemoFactory asocijacijeDemoFactory;

    public StubAsocijacijeRepository() {
        this.asocijacijeDemoFactory = new AsocijacijeDemoFactory();
    }

    @Override
    public List<Asocijacija> getRounds() {
        return asocijacijeDemoFactory.createDemoRounds();
    }
}