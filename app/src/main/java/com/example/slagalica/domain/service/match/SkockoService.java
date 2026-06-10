package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.SkockoPolje;
import com.example.slagalica.domain.model.match.games.SkockoPokusaj;
import com.example.slagalica.domain.model.match.games.SkockoTabla;

import java.util.ArrayList;
import java.util.List;

public class SkockoService {

    private final SkockoTabla skockoTabla;

    public SkockoService(SkockoTabla skockoTabla) {
        this.skockoTabla = skockoTabla;
    }

    public SkockoTabla getSkockoTabla() {
        return skockoTabla;
    }

    public void appendSymbol(String symbol) {
        if (skockoTabla.isFinished()) return;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());
        if (currentAttempt.isSubmitted()) return;

        for (SkockoPolje polje : currentAttempt.getGuess()) {
            if (polje.isEmpty()) {
                polje.setSymbol(symbol);
                return;
            }
        }
    }

    public void removeLastSymbol() {
        if (skockoTabla.isFinished()) return;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());
        if (currentAttempt.isSubmitted()) return;

        for (int i = currentAttempt.getGuess().size() - 1; i >= 0; i--) {
            SkockoPolje polje = currentAttempt.getGuess().get(i);
            if (!polje.isEmpty()) {
                polje.setSymbol("");
                return;
            }
        }
    }

    public SubmitResult submitCurrentRow() {
        if (skockoTabla.isFinished()) {
            return SubmitResult.gameAlreadyFinished();
        }

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());

        for (SkockoPolje polje : currentAttempt.getGuess()) {
            if (polje.isEmpty()) {
                return SubmitResult.error("Popuni sva 4 polja");
            }
        }

        evaluateAttempt(currentAttempt);
        currentAttempt.setSubmitted(true);

        boolean solved = isExactAttempt(currentAttempt);

        if (solved) {
            skockoTabla.setSolved(true);
            skockoTabla.setFinished(true);

            if (skockoTabla.getCurrentPlayer() == 1) {
                skockoTabla.setWinnerName(skockoTabla.getPlayerOneName());
            } else {
                skockoTabla.setWinnerName(skockoTabla.getPlayerTwoName());
            }

            return SubmitResult.success(true, false, null);
        } else {
            if (skockoTabla.getCurrentPlayer() == 1) {
                if (skockoTabla.getCurrentRow() == 5) {
                    skockoTabla.setCurrentPlayer(2);
                    skockoTabla.setCurrentRow(6);
                    return SubmitResult.success(false, true, "Sada igra Igrač 2");
                } else {
                    skockoTabla.setCurrentRow(skockoTabla.getCurrentRow() + 1);
                    return SubmitResult.success(false, false, null);
                }
            } else {
                skockoTabla.setFinished(true);
                return SubmitResult.success(false, false, null);
            }
        }
    }

    public boolean canAppendMoreSymbols() {
        if (skockoTabla.isFinished()) return false;

        SkockoPokusaj currentAttempt = skockoTabla.getAttempts().get(skockoTabla.getCurrentRow());
        if (currentAttempt.isSubmitted()) return false;

        for (SkockoPolje polje : currentAttempt.getGuess()) {
            if (polje.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isExactAttempt(SkockoPokusaj attempt) {
        for (int i = 0; i < 4; i++) {
            if (!attempt.getGuess().get(i).getSymbol().equals(skockoTabla.getSecretCombination().get(i))) {
                return false;
            }
        }
        return true;
    }

    public void evaluateAttempt(SkockoPokusaj attempt) {
        List<String> secret = new ArrayList<>(skockoTabla.getSecretCombination());
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

    public String getGameStateText() {
        if (skockoTabla.isSolved()) {
            return "Pobednik: " + skockoTabla.getWinnerName();
        } else if (skockoTabla.isFinished()) {
            return "Niko nije pogodio | Kombinacija prikazana";
        } else if (skockoTabla.getCurrentPlayer() == 1) {
            return "Na potezu: " + skockoTabla.getPlayerOneName()
                    + " | pokušaj " + (skockoTabla.getCurrentRow() + 1) + " / 6";
        } else {
            return "Na potezu: " + skockoTabla.getPlayerTwoName() + " | pokušaj 1 / 1";
        }
    }

    public static class SubmitResult {
        private final boolean success;
        private final boolean solved;
        private final boolean switchedToPlayerTwo;
        private final boolean gameAlreadyFinished;
        private final String message;

        private SubmitResult(boolean success,
                             boolean solved,
                             boolean switchedToPlayerTwo,
                             boolean gameAlreadyFinished,
                             String message) {
            this.success = success;
            this.solved = solved;
            this.switchedToPlayerTwo = switchedToPlayerTwo;
            this.gameAlreadyFinished = gameAlreadyFinished;
            this.message = message;
        }

        public static SubmitResult success(boolean solved, boolean switchedToPlayerTwo, String message) {
            return new SubmitResult(true, solved, switchedToPlayerTwo, false, message);
        }

        public static SubmitResult error(String message) {
            return new SubmitResult(false, false, false, false, message);
        }

        public static SubmitResult gameAlreadyFinished() {
            return new SubmitResult(false, false, false, true, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isSolved() {
            return solved;
        }

        public boolean isSwitchedToPlayerTwo() {
            return switchedToPlayerTwo;
        }

        public boolean isGameAlreadyFinished() {
            return gameAlreadyFinished;
        }

        public String getMessage() {
            return message;
        }
    }
}