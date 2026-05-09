package com.example.slagalica.repository.impl.stub;

import com.example.slagalica.domain.model.match.games.SkockoTabla;
import com.example.slagalica.domain.service.match.SkockoDemoFactory;
import com.example.slagalica.repository.impl.SkockoRepository;

public class StubSkockoRepository implements SkockoRepository {

    private final SkockoDemoFactory skockoDemoFactory;

    public StubSkockoRepository() {
        this.skockoDemoFactory = new SkockoDemoFactory();
    }

    @Override
    public SkockoTabla getSkockoTabla() {
        return skockoDemoFactory.createDemoTabla();
    }
}