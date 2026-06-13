package com.example.slagalica.domain.model.match.games.korakpokorak;

import com.example.slagalica.domain.model.match.games.common.AbstractGame;
import com.example.slagalica.domain.model.match.games.common.GameConfig;
import com.example.slagalica.domain.model.match.games.common.GameSession;
import com.example.slagalica.domain.service.match.KorakPoKorakService;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

public class KorakPoKorak extends AbstractGame {
    private static final int MAX_POINTS_PER_HINT = 20;
    private static final int POINTS_LOST_PER_HINT = 2;
    private static final int POINTS_FOR_STEAL = 5;
    public static final int SECONDS_PER_ANSWER = 5;
    private static final int ROUND_LENGTH = 35;
    private static final int ROUNDS = 2;
    private static final int MAX_POINTS = 40;
    private static final int MIN_POINTS = 0;

    private final KorakPoKorakService gameService;
    @Getter
    private int currentHint = 1;
    @Getter
    private List<String> hints;
    @Getter
    private String term;
    @Getter
    private boolean stealOpportunity = false;

    public KorakPoKorak(GameSession session, KorakPoKorakService service) {
        super(new GameConfig(5, ROUND_LENGTH, ROUNDS, MAX_POINTS, MIN_POINTS), session);
        gameService = service;
    }

    public void startNewRound(Runnable onReady){
        super.startNewRound();
        if(hasEnded()){
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
                    if (onReady != null) onReady.run();
                })
                .exceptionally(ex -> {
                    return null;
                });
    }

    @Override
    public void startNewRound(){
        startNewRound(null);
    }

    public String revealNextHint(){
        if (currentHint >= hints.size()) {
            return null;
        }
        currentHint++;
        updateSessionData();
        return hints.get(currentHint - 1);
    }

    public void openStealOpportunity() {
        stealOpportunity = true;
        updateSessionData();
    }

    public boolean isAnswerCorrect(String answer){
        if(answer != null && answer.equalsIgnoreCase(term)){
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
        return MAX_POINTS_PER_HINT - (currentHint - 1) * POINTS_LOST_PER_HINT;
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

    public void applyRemoteUpdate(KorakPoKorakSessionData data) {
        this.currentHint = data.getCurrentHint();
        this.hints = data.getHints();
        this.term = data.getTerm();
        this.stealOpportunity = data.isStealOpportunity();

        session.setCurrentRound(data.getCurrentRound());
        session.setCurrentPlayer(data.getCurrentPlayer());
        session.setHasEnded(data.isHasEnded());

        if (data.isHasEnded()) {
            notifyGameEnded();
        }
    }
}
