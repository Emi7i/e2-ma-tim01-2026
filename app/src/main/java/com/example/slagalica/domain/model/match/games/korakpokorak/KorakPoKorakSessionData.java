package com.example.slagalica.domain.model.match.games.korakpokorak;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KorakPoKorakSessionData {
    private int currentRound;
    private String currentPlayer;
    private boolean hasEnded;
    private int currentHint;
    private boolean stealOpportunity;
    private String term;
    private List<String> hints;

}
