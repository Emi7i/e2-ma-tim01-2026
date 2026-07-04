package com.example.slagalica.domain.model.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KoZnaZnaSessionData {
    private int currentRound;
    private int currentPlayer;
    private boolean hasEnded;
}
