package com.example.slagalica.domain.model.match.games.common;

public abstract class AbstractGame implements IGame {
    private final GameConfig config;
    protected final GameSession session;

    protected OnPointsChangedListener pointsListener;
    protected OnGameEndedListener endedListener;

    protected AbstractGame(GameConfig config, GameSession session) {
        this.config = config;
        this.session = session;
    }

    @Override
    public long getId() {
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
    public int getMaxPoints() {
        return config.getMaxPoints();
    }

    @Override
    public int getMinPoints() {
        return config.getMinPoints();
    }

    @Override
    public long getMatchId() {
        return session.getMatchId();
    }

    @Override
    public long getPlayer1Id() {
        return session.getPlayer1Id();
    }

    @Override
    public long getPlayer2Id() {
        return session.getPlayer2Id();
    }

    @Override
    public int getCurrentRound() {
        return session.getCurrentRound();
    }

    @Override
    public long getCurrentPlayer() {
        return session.getCurrentPlayer();
    }

    public long getOtherPlayer() {
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

    public void startNewRound(){
        int currentRound = session.getCurrentRound();
        currentRound++;
        if(currentRound > config.getRounds()){
            session.setHasEnded(true);
            return;
        }
        session.setCurrentRound(currentRound);
    }

    protected void notifyPointsChanged(long playerId, int amount) {
        if (pointsListener != null) {
            pointsListener.onPointsChanged(playerId, amount);
        }
    }

    protected void notifyGameEnded() {
        if (endedListener != null) {
            endedListener.onGameEnded();
        }
    }

    protected void endGame() {
        session.setHasEnded(true);
        notifyGameEnded();
    }
}