package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.SkockoPolje;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.List;

public class SkockoService {

    private final List<SkockoTabla> rounds;
    private int currentRoundIndex;
    private boolean matchFinished;

    public SkockoService(List<SkockoTabla> rounds) {
        this.rounds = rounds;
        this.currentRoundIndex = 0;
        this.matchFinished = false;
    }

    public SkockoTabla getCurrentRound() {
        return rounds.get(currentRoundIndex);
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    public boolean canAdvanceRound() {
        return getCurrentRound().getGameState().isRoundFinished()
                && currentRoundIndex < rounds.size() - 1;
    }

    public ActionResult advanceToNextRound() {
        SkockoTabla currentRound = getCurrentRound();

        if (!currentRound.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda još nije završena");
        }

        if (!canAdvanceRound()) {
            return ActionResult.error("Nema više rundi");
        }

        int previousP1 = currentRound.getGameState().getPlayerOneScore();
        int previousP2 = currentRound.getGameState().getPlayerTwoScore();

        currentRoundIndex++;

        SkockoTabla nextRound = getCurrentRound();
        nextRound.getGameState().setPlayerOneScore(previousP1);
        nextRound.getGameState().setPlayerTwoScore(previousP2);

        return ActionResult.success(
                "Počinje runda " + nextRound.getGameState().getRoundNumber(),
                false,
                false
        );
    }

    public boolean canEditCurrentAttempt() {
        SkockoTabla round = getCurrentRound();
        return !round.getGameState().isRoundFinished();
    }

    public void appendSymbol(String symbol) {
        if (!canEditCurrentAttempt()) {
            return;
        }

        SkockoPokusaj attempt = getEditableAttempt();
        if (attempt == null || attempt.isSubmitted()) {
            return;
        }

        for (SkockoPolje polje : attempt.getGuess()) {
            if (polje.isEmpty()) {
                polje.setSymbol(symbol);
                return;
            }
        }
    }

    public void removeLastSymbol() {
        if (!canEditCurrentAttempt()) {
            return;
        }

        SkockoPokusaj attempt = getEditableAttempt();
        if (attempt == null || attempt.isSubmitted()) {
            return;
        }

        for (int i = attempt.getGuess().size() - 1; i >= 0; i--) {
            SkockoPolje polje = attempt.getGuess().get(i);
            if (!polje.isEmpty()) {
                polje.setSymbol("");
                return;
            }
        }
    }

    public ActionResult submitCurrentRow() {
        SkockoTabla round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda je završena");
        }

        SkockoPokusaj attempt = getEditableAttempt();
        if (attempt == null) {
            return ActionResult.error("Nema aktivnog pokušaja");
        }

        for (SkockoPolje polje : attempt.getGuess()) {
            if (polje.isEmpty()) {
                return ActionResult.error("Popuni sva 4 polja");
            }
        }

        evaluateAttempt(attempt);
        attempt.setSubmitted(true);

        if (isExactAttempt(attempt, round.getSecretCombination())) {
            int points = round.isBonusAttemptActive()
                    ? 10
                    : calculateRegularAttemptPoints(round.getCurrentRow());

            awardPoints(round.getGameState(), round.getGameState().getCurrentPlayer(), points);

            round.setSolved(true);
            finishRound(round);

            String message = round.isBonusAttemptActive()
                    ? "Tačno! Bonus pokušaj uspešan (+" + points + " bodova)"
                    : "Tačno! (+" + points + " bodova)";

            return ActionResult.success(message, false, true);
        }

        if (round.isBonusAttemptActive()) {
            round.setBonusAttemptUsed(true);
            finishRound(round);
            return ActionResult.success("Bonus pokušaj nije uspeo. Runda je završena.", false, true);
        }

        if (round.getCurrentRow() == 5) {
            activateBonusAttempt(round);
            return ActionResult.success(
                    "Nije pogođeno. Protivnik ima bonus pokušaj od 10 sekundi za 10 bodova.",
                    true,
                    false
            );
        }

        round.setCurrentRow(round.getCurrentRow() + 1);
        return ActionResult.success("Netačno. Prelaz na sledeći pokušaj.", false, false);
    }

    public ActionResult onTimeExpired() {
        SkockoTabla round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda je već završena");
        }

        if (round.isBonusAttemptActive()) {
            round.setBonusAttemptUsed(true);
            finishRound(round);
            return ActionResult.success("Isteklo je vreme za bonus pokušaj.", false, true);
        }

        activateBonusAttempt(round);
        return ActionResult.success(
                "Isteklo je vreme. Protivnik dobija bonus pokušaj od 10 sekundi.",
                true,
                false
        );
    }

    public String getRoundInfoText() {
        TwoPlayerGameState state = getCurrentRound().getGameState();
        return "Runda " + state.getRoundNumber() + "/2 | Preostalo: " + formatTime(state.getRemainingSeconds());
    }

    public String getScoreText() {
        TwoPlayerGameState state = getCurrentRound().getGameState();
        return "Igrač 1: " + state.getPlayerOneScore() + " | Igrač 2: " + state.getPlayerTwoScore();
    }

    public String getStatusText() {
        SkockoTabla round = getCurrentRound();
        TwoPlayerGameState state = round.getGameState();

        if (matchFinished && state.isRoundFinished()) {
            if (state.getPlayerOneScore() > state.getPlayerTwoScore()) {
                return "Kraj igre | Pobednik: " + state.getPlayerOneName();
            } else if (state.getPlayerTwoScore() > state.getPlayerOneScore()) {
                return "Kraj igre | Pobednik: " + state.getPlayerTwoName();
            } else {
                return "Kraj igre | Nerešeno";
            }
        }

        if (state.isRoundFinished()) {
            return "Runda " + state.getRoundNumber() + " je završena";
        }

        if (round.isBonusAttemptActive()) {
            return "Bonus pokušaj: " + state.getCurrentPlayerName() + " | 1 pokušaj | 10 bodova";
        }

        return "Na potezu: " + state.getCurrentPlayerName()
                + " | pokušaj " + (round.getCurrentRow() + 1) + " / 6";
    }

    private SkockoPokusaj getEditableAttempt() {
        SkockoTabla round = getCurrentRound();

        if (round.isBonusAttemptActive()) {
            return round.getBonusAttempt();
        }

        if (round.getCurrentRow() >= 0 && round.getCurrentRow() < round.getAttempts().size()) {
            return round.getAttempts().get(round.getCurrentRow());
        }

        return null;
    }

    private void activateBonusAttempt(SkockoTabla round) {
        round.getGameState().switchTurn();
        round.getGameState().setRemainingSeconds(10);
        round.setBonusAttemptActive(true);
    }

    private void finishRound(SkockoTabla round) {
        round.setBonusAttemptActive(false);
        round.getGameState().setRoundFinished(true);

        if (currentRoundIndex == rounds.size() - 1) {
            matchFinished = true;
            round.getGameState().setGameFinished(true);
        }
    }

    private void awardPoints(TwoPlayerGameState gameState, int player, int points) {
        gameState.addPointsToPlayer(player, points);
    }

    private int calculateRegularAttemptPoints(int currentRow) {
        if (currentRow == 0 || currentRow == 1) {
            return 20;
        }
        if (currentRow == 2 || currentRow == 3) {
            return 15;
        }
        return 10;
    }

    private boolean isExactAttempt(SkockoPokusaj attempt, List<String> secret) {
        for (int i = 0; i < 4; i++) {
            String guessSymbol = attempt.getGuess().get(i).getSymbol();
            if (!guessSymbol.equals(secret.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void evaluateAttempt(SkockoPokusaj attempt) {
        List<String> secret = new ArrayList<>(getCurrentRound().getSecretCombination());
        List<String> guess = new ArrayList<>();

        for (SkockoPolje polje : attempt.getGuess()) {
            guess.add(polje.getSymbol());
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (guess.get(i).equals(secret.get(i))) {
                result.add("EXACT");
                guess.set(i, null);
                secret.set(i, null);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (guess.get(i) != null) {
                int index = secret.indexOf(guess.get(i));
                if (index != -1) {
                    result.add("PARTIAL");
                    secret.set(index, null);
                }
            }
        }

        while (result.size() < 4) {
            result.add("EMPTY");
        }

        for (int i = 0; i < 4; i++) {
            attempt.getFeedback().set(i, result.get(i));
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static class ActionResult {
        private final boolean success;
        private final String message;
        private final boolean bonusActivated;
        private final boolean roundFinished;

        private ActionResult(boolean success, String message, boolean bonusActivated, boolean roundFinished) {
            this.success = success;
            this.message = message;
            this.bonusActivated = bonusActivated;
            this.roundFinished = roundFinished;
        }

        public static ActionResult success(String message, boolean bonusActivated, boolean roundFinished) {
            return new ActionResult(true, message, bonusActivated, roundFinished);
        }

        public static ActionResult error(String message) {
            return new ActionResult(false, message, false, false);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isBonusActivated() {
            return bonusActivated;
        }

        public boolean isRoundFinished() {
            return roundFinished;
        }
    }
}