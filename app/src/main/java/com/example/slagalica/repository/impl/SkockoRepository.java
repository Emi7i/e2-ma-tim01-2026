package com.example.slagalica.repository.impl;

import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.List;

public interface SkockoRepository {
    List<SkockoTabla> getRounds();
}