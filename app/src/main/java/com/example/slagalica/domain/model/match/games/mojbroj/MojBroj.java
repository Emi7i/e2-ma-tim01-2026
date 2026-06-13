package com.example.slagalica.domain.model.match.games.mojbroj;

import com.example.slagalica.domain.model.match.games.common.AbstractGame;
import com.example.slagalica.domain.model.match.games.common.GameConfig;
import com.example.slagalica.domain.model.match.games.common.GameSession;
import com.example.slagalica.domain.service.match.MojBrojService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

public class MojBroj extends AbstractGame {

    private static final int ROUND_LENGTH = 60;
    private static final int ROUNDS = 2;
    private static final int MAX_POINTS = 20;
    private static final int MIN_POINTS = 0;

    private static final int CORRECT_SOLUTION_POINTS = 10;
    private static final int CLOSEST_SOLUTION_POINTS = 5;

    private final MojBrojService gameService;

    @Getter
    private int goalNumber;
    @Getter
    private List<Integer> operands = new ArrayList<>();

    // result of the player whose round it currently is (the only one who plays on this device)
    @Getter
    private int currentPlayerResult = 0;
    @Getter
    private List<String> currentPlayerTokens = new ArrayList<>();

    // result of the other player for this round (0 = no answer, per spec)
    @Getter
    private int otherPlayerResult = 0;
    @Getter
    private List<String> otherPlayerTokens = new ArrayList<>();

    public MojBroj(GameSession session, MojBrojService service) {
        super(new GameConfig(6, ROUND_LENGTH, ROUNDS, MAX_POINTS, MIN_POINTS), session);
        this.gameService = service;
    }

    @Override
    public void startNewRound() {
        super.startNewRound();
        if (hasEnded()) {
            notifyGameEnded();
            updateSessionData();
            return;
        }

        if (session.getCurrentRound() == 2) {
            setCurrentPlayer(getOtherPlayer());
        }
        notifyActivePlayerChanged(getCurrentPlayer());

        goalNumber = 0;
        operands = new ArrayList<>();
        currentPlayerResult = 0;
        currentPlayerTokens = new ArrayList<>();
        otherPlayerResult = 0;
        otherPlayerTokens = new ArrayList<>();

        updateSessionData();
    }

    public int generateGoalNumber() {
        goalNumber = gameService.generateGoalNumber();
        updateSessionData();
        return goalNumber;
    }

    public List<Integer> generateOperands() {
        operands = gameService.generateOperands();
        updateSessionData();
        return operands;
    }

    /**
     * Validates and evaluates the given expression tokens against the current operands.
     * Throws IllegalArgumentException if the expression is invalid (uses operands not
     * available, reuses an operand more times than it appears, malformed expression, etc).
     *
     * @return the evaluated integer result
     */
    public int evaluateExpression(List<String> tokens) {
        return gameService.evaluateExpression(tokens, operands);
    }

    /**
     * Called when the active player submits their answer (tokens).
     * Stores the result, awards points, and ends the round.
     */
    public void submitAnswer(List<String> tokens) {
        int result;
        if (tokens == null || tokens.isEmpty()) {
            result = 0;
        } else {
            result = evaluateExpression(tokens);
        }

        currentPlayerTokens = tokens == null ? new ArrayList<>() : new ArrayList<>(tokens);
        currentPlayerResult = result;

        evaluatePoints();
        updateSessionData();
    }

    /**
     * Awards points per spec rules (g, h, i, j).
     * currentPlayerResult / currentPlayerTokens belong to the player whose round it is.
     * otherPlayerResult is always 0 here since the opponent does not play on this device yet.
     */
    private void evaluatePoints() {
        boolean currentHasGoal = currentPlayerResult == goalNumber && currentPlayerResult != 0;
        boolean otherHasGoal = otherPlayerResult == goalNumber && otherPlayerResult != 0;

        if (currentHasGoal) {
            // (g) current player found the goal number
            awardPoints(getCurrentPlayer(), CORRECT_SOLUTION_POINTS);
        } else if (otherHasGoal) {
            // (h) current player doesn't have it, other player does
            awardPoints(getOtherPlayer(), CORRECT_SOLUTION_POINTS);
        } else {
            // neither found the goal number
            int currentDiff = currentPlayerResult == 0 ? Integer.MAX_VALUE : Math.abs(goalNumber - currentPlayerResult);
            int otherDiff = otherPlayerResult == 0 ? Integer.MAX_VALUE : Math.abs(goalNumber - otherPlayerResult);

            if (currentPlayerResult == 0 && otherPlayerResult == 0) {
                // (i) neither entered anything -> 0 points each, nothing to award
            } else if (currentDiff < otherDiff) {
                // (i) current player closer
                awardPoints(getCurrentPlayer(), CLOSEST_SOLUTION_POINTS);
            } else if (otherDiff < currentDiff) {
                // (i) other player closer
                awardPoints(getOtherPlayer(), CLOSEST_SOLUTION_POINTS);
            } else {
                // (j) equal non-zero results, not equal to goal -> player whose round it is gets points
                awardPoints(getCurrentPlayer(), CLOSEST_SOLUTION_POINTS);
            }
        }
    }

    private void awardPoints(long playerId, int amount) {
        notifyPointsChanged(playerId, amount);
    }

    private void updateSessionData() {
        MojBrojSessionData data = new MojBrojSessionData(
                getCurrentRound(),
                getCurrentPlayer(),
                hasEnded(),
                goalNumber,
                operands,
                currentPlayerTokens,
                currentPlayerResult,
                otherPlayerTokens,
                otherPlayerResult
        );
        gameService.updateSessionData(getMatchId(), data);
    }

    public void applyRemoteUpdate(MojBrojSessionData data) {
        this.goalNumber = data.getGoalNumber();
        this.operands = data.getOperands();
        this.currentPlayerTokens = data.getPlayer1Tokens();
        this.currentPlayerResult = data.getPlayer1Result();
        this.otherPlayerTokens = data.getPlayer2Tokens();
        this.otherPlayerResult = data.getPlayer2Result();

        session.setCurrentRound(data.getCurrentRound());
        session.setCurrentPlayer(data.getCurrentPlayer());
        session.setHasEnded(data.isHasEnded());

        if (data.isHasEnded()) {
            notifyGameEnded();
        }
    }
}