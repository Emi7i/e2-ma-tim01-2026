package com.example.slagalica.domain.model.match.games.common;

public abstract class AbstractGame implements IGame {
    private final GameConfig config;
    protected final GameSession session;

    protected OnPointsChangedListener pointsListener;
    protected OnGameEndedListener endedListener;
    protected OnActivePlayerChangedListener activePlayerChangedListener;
    protected OnSessionUpdateListener onSessionUpdateListener;

    protected AbstractGame(GameConfig config, GameSession session) {
        this.config = config;
        this.session = session;
    }

    @Override
    public int getId() {
        return config.getId();
    }

    @Override
    public int getRoundLength() {
        return config.getRoundLength();
    }

    @Override
    public int getRounds() {
        return config.getRounds();
    }

    @Override
    public String getMatchId() {
        return session.getMatchId();
    }

    @Override
    public String getPlayer1Id() {
        return session.getPlayer1Id();
    }

    @Override
    public String getPlayer2Id() {
        return session.getPlayer2Id();
    }

    @Override
    public int getCurrentRound() {
        return session.getCurrentRound();
    }

    @Override
    public String getCurrentPlayer() {
        return session.getCurrentPlayer();
    }

    @Override
    public void setCurrentPlayer(String playerId) {
        session.setCurrentPlayer(playerId);
    }



    @Override
    public String getOtherPlayer() {
        if(getPlayer1Id() == session.getCurrentPlayer())
            return getPlayer2Id();
        return getPlayer1Id();
    }

    @Override
    public boolean hasEnded() {
        return session.isHasEnded();
    }

    @Override
    public void setOnPointsChangedListener(OnPointsChangedListener listener) {
        this.pointsListener = listener;
    }

    @Override
    public void setOnGameEndedListener(OnGameEndedListener listener) {
        this.endedListener = listener;
    }

    @Override
    public void setOnActivePlayerChangedListener(OnActivePlayerChangedListener listener) {
        this.activePlayerChangedListener = listener;
    }

    public void setOnSessionUpdateListener(OnSessionUpdateListener listener) {
        this.onSessionUpdateListener = listener;
    }

    @Override
    public void startNewRound(){
        int currentRound = session.getCurrentRound();
        currentRound++;
        if(currentRound > config.getRounds()){
            session.setHasEnded(true);
            return;
        }
        session.setCurrentRound(currentRound);
    }

    protected void notifyPointsChanged(String playerId, int amount) {
        if (pointsListener != null) {
            pointsListener.onPointsChanged(playerId, amount);
        }
    }

    protected void notifyGameEnded() {
        if (endedListener != null) {
            endedListener.onGameEnded();
        }
    }

    protected void notifyActivePlayerChanged(String playerId) {
        if (activePlayerChangedListener != null) {
            activePlayerChangedListener.onActivePlayerChanged(playerId);
        }
    }

    protected void endGame() {
        session.setHasEnded(true);
        notifyGameEnded();
    }
}