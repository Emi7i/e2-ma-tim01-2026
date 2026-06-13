package com.example.slagalica.domain.model.match.games.korakpokorak;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TermWithHints {
    private String term;
    private List<String> hints;
}
