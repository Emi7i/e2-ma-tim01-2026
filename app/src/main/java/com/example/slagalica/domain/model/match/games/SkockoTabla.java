package com.example.slagalica.domain.model.match.games;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;

import java.util.List;

public class SkockoTabla {

    private final TwoPlayerGameState gameState;
    private final List<String> secretCombination;
    private final List<SkockoPokusaj> attempts;
    private final SkockoPokusaj bonusAttempt;

    private int currentRow;
    private boolean solved;
    private boolean bonusAttemptActive;
    private boolean bonusAttemptUsed;

    public SkockoTabla(TwoPlayerGameState gameState,
                       List<String> secretCombination,
                       List<SkockoPokusaj> attempts,
                       SkockoPokusaj bonusAttempt,
                       int currentRow,
                       boolean solved,
                       boolean bonusAttemptActive,
                       boolean bonusAttemptUsed) {
        this.gameState = gameState;
        this.secretCombination = secretCombination;
        this.attempts = attempts;
        this.bonusAttempt = bonusAttempt;
        this.currentRow = currentRow;
        this.solved = solved;
        this.bonusAttemptActive = bonusAttemptActive;
        this.bonusAttemptUsed = bonusAttemptUsed;
    }

    public TwoPlayerGameState getGameState() {
        return gameState;
    }

    public List<String> getSecretCombination() {
        return secretCombination;
    }

    public List<SkockoPokusaj> getAttempts() {
        return attempts;
    }

    public SkockoPokusaj getBonusAttempt() {
        return bonusAttempt;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isBonusAttemptActive() {
        return bonusAttemptActive;
    }

    public void setBonusAttemptActive(boolean bonusAttemptActive) {
        this.bonusAttemptActive = bonusAttemptActive;
    }

    public boolean isBonusAttemptUsed() {
        return bonusAttemptUsed;
    }

    public void setBonusAttemptUsed(boolean bonusAttemptUsed) {
        this.bonusAttemptUsed = bonusAttemptUsed;
    }
}