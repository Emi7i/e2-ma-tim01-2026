package com.example.slagalica.domain.model.match.games.korakpokorak;

import com.example.slagalica.domain.model.match.games.common.AbstractGame;
import com.example.slagalica.domain.model.match.games.common.GameConfig;
import com.example.slagalica.domain.model.match.games.common.GameSession;
import com.example.slagalica.domain.service.match.KorakPoKorakService;

import java.util.List;
import java.util.Objects;

public class KorakPoKorak extends AbstractGame {
    private static final int MAX_POINTS_PER_HINT = 20;
    private static final int POINTS_LOST_PER_HINT = 2;
    private static final int POINTS_FOR_STEAL = 5;
    private static final int SECONDS_PER_ANSWER = 10;
    private static final int ROUND_LENGTH = 70;
    private static final int ROUNDS = 2;
    private static final int MAX_POINTS = 40;
    private static final int MIN_POINTS = 0;

    private KorakPoKorakService gameService;
    private int currentHint = 1;
    private List<String> hints;
    private String term;
    private boolean stealOpportunity = false;

    public KorakPoKorak(GameSession session) {
        super(new GameConfig(5, ROUND_LENGTH, ROUNDS, MAX_POINTS, MIN_POINTS), session);
    }

    @Override
    public void startNewRound(){
        super.startNewRound();
        if(hasEnded()){
            // reveal all
            notifyGameEnded();
            updateSessionData();
            return;
        }
        gameService.getRandomTermWithHints()
                .thenAccept(termWithHints -> {
                    this.hints = termWithHints.getHints();
                    this.term = termWithHints.getTerm();
                    currentHint = 1;
                    updateSessionData();
                })
                .exceptionally(ex -> {
                    // handle error
                    return null;
                });
    }

    public String revealNextHint(){
        currentHint++;
        return hints.get(currentHint - 1);
    }

    public boolean isAnswerCorrect(String answer){
        if(Objects.equals(answer, term)){
            if(!stealOpportunity){
                awardCurrentPlayerPoints();
            }
            else{
                awardStealPoints();
            }

            return true;
        }

        return false;
    }

    private int getPointsForAnswer(){
        return MAX_POINTS_PER_HINT - currentHint * POINTS_LOST_PER_HINT;
    }

    private void awardCurrentPlayerPoints(){
        notifyPointsChanged(getCurrentPlayer(), getPointsForAnswer());
    }

    private void awardStealPoints(){
        notifyPointsChanged(getOtherPlayer(), POINTS_FOR_STEAL);
    }

    private void updateSessionData(){
        KorakPoKorakSessionData data = new KorakPoKorakSessionData(getCurrentRound(), getCurrentPlayer(), hasEnded(), currentHint, stealOpportunity, term, hints);
        gameService.updateSessionData(getMatchId(), data);
    }
}
