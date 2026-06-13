package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.Asocijacija;

import java.util.List;

public interface AsocijacijeRepository {
    List<Asocijacija> getRounds();
}