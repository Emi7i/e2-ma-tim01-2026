package com.example.slagalica.domain.model.match;

import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchSessionData {
    @DocumentId public String id;
    public String player1Id;
    public String player2Id;
    public int player1Score;
    public int player2Score;
    public int currentGameId;
    public String activePlayer;
}
