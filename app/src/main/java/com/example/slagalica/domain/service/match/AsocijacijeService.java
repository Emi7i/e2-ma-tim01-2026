package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;

import java.util.List;

public class AsocijacijeService {

    private final List<Asocijacija> rounds;
    private int currentRoundIndex;
    private boolean matchFinished;

    public AsocijacijeService(List<Asocijacija> rounds) {
        this.rounds = rounds;
        this.currentRoundIndex = 0;
        this.matchFinished = false;
    }

    public Asocijacija getCurrentRound() {
        return rounds.get(currentRoundIndex);
    }

    public int getCurrentRoundIndex() {
        return currentRoundIndex;
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    public boolean canAdvanceRound() {
        return getCurrentRound().getGameState().isRoundFinished() && currentRoundIndex < rounds.size() - 1;
    }

    public boolean canCurrentPlayerGuess() {
        Asocijacija round = getCurrentRound();
        return !round.getGameState().isRoundFinished() && round.isGuessWindowOpen();
    }

    public boolean canOpenFields() {
        Asocijacija round = getCurrentRound();
        return !round.getGameState().isRoundFinished() && !round.isGuessWindowOpen();
    }

    public ActionResult openField(int columnIndex, int fieldIndex) {
        Asocijacija round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda je završena");
        }

        if (round.isGuessWindowOpen()) {
            return ActionResult.error("Pogodi kolonu ili konačno rešenje, ili predaj potez");
        }

        AsocijacijaKolona column = round.getColumns().get(columnIndex);
        AsocijacijaPolje field = column.getFields().get(fieldIndex);

        if (column.isSolved()) {
            return ActionResult.error("Kolona je već rešena");
        }

        if (field.isOpened()) {
            return ActionResult.error("Polje je već otvoreno");
        }

        field.setOpened(true);
        round.setGuessWindowOpen(true);

        return ActionResult.success("Otvoreno polje " + column.getLabel() + (fieldIndex + 1));
    }

    public ActionResult submitColumnSolution(int columnIndex, String enteredText) {
        Asocijacija round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda je završena");
        }

        if (!round.isGuessWindowOpen()) {
            return ActionResult.error("Prvo otvori polje");
        }

        if (enteredText == null || enteredText.trim().isEmpty()) {
            return ActionResult.error("Unesi rešenje kolone");
        }

        AsocijacijaKolona column = round.getColumns().get(columnIndex);

        if (column.isSolved()) {
            return ActionResult.error("Kolona je već rešena");
        }

        if (normalizeAnswer(enteredText).equals(normalizeAnswer(column.getSolution()))) {
            int points = calculateColumnPoints(column);
            column.setSolved(true);
            column.openAllFields();

            awardPoints(round.getGameState(), round.getGameState().getCurrentPlayer(), points);

            return ActionResult.success(
                    "Tačno rešenje kolone " + column.getLabel() + " (+" + points + " bodova)"
            );
        }

        switchTurn(round);
        return ActionResult.success("Netačno. Potez prelazi na drugog igrača.");
    }

    public ActionResult submitFinalSolution(String enteredText) {
        Asocijacija round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            return ActionResult.error("Runda je završena");
        }

        if (!round.isGuessWindowOpen()) {
            return ActionResult.error("Prvo otvori polje");
        }

        if (enteredText == null || enteredText.trim().isEmpty()) {
            return ActionResult.error("Unesi konačno rešenje");
        }

        if (normalizeAnswer(enteredText).equals(normalizeAnswer(round.getFinalSolution()))) {
            int finalPoints = calculateFinalPoints(round);
            awardPoints(round.getGameState(), round.getGameState().getCurrentPlayer(), finalPoints);

            round.setFinalSolved(true);
            round.getGameState().setRoundFinished(true);
            round.setGuessWindowOpen(false);
            revealWholeBoard(round);

            if (currentRoundIndex == rounds.size() - 1) {
                matchFinished = true;
            }

            return ActionResult.success(
                    "Tačno konačno rešenje! (+" + finalPoints + " bodova)"
            );
        }

        switchTurn(round);
        return ActionResult.success("Netačno konačno rešenje. Potez prelazi na drugog igrača.");
    }

    public ActionResult passTurn() {
        Asocijacija round = getCurrentRound();

        if (round.getGameState().isRoundFinished()) {
            if (canAdvanceRound()) {
                int previousP1 = round.getGameState().getPlayerOneScore();
                int previousP2 = round.getGameState().getPlayerTwoScore();
                currentRoundIndex++;
                Asocijacija nextRound = getCurrentRound();
                nextRound.getGameState().setPlayerOneScore(previousP1);
                nextRound.getGameState().setPlayerTwoScore(previousP2);

                return ActionResult.success("Počinje runda " + nextRound.getGameState().getRoundNumber());
            }
            return ActionResult.error("Partija je završena");
        }

        if (!round.isGuessWindowOpen()) {
            return ActionResult.error("Prvo otvori polje");
        }

        switchTurn(round);
        return ActionResult.success("Potez je predat drugom igraču");
    }

    public ActionResult onTimeExpired() {
        Asocijacija round = getCurrentRound();

        round.getGameState().setRoundFinished(true);
        round.setGuessWindowOpen(false);
        revealWholeBoard(round);

        if (currentRoundIndex == rounds.size() - 1) {
            matchFinished = true;
        }

        return ActionResult.success("Vreme je isteklo za rundu " + round.getGameState().getRoundNumber());
    }

    public String getCurrentPlayerName() {
        return getCurrentRound().getGameState().getCurrentPlayerName();
    }

    public String getRoundInfoText() {
        TwoPlayerGameState gameState = getCurrentRound().getGameState();
        return "Runda " + gameState.getRoundNumber() + "/2 | Preostalo: " + formatTime(gameState.getRemainingSeconds());
    }

    public String getScoreText() {
        TwoPlayerGameState gameState = getCurrentRound().getGameState();
        return "Igrač 1: " + gameState.getPlayerOneScore() + " | Igrač 2: " + gameState.getPlayerTwoScore();
    }

    public String getStatusText() {
        Asocijacija round = getCurrentRound();
        TwoPlayerGameState gameState = round.getGameState();

        if (matchFinished && round.getGameState().isRoundFinished()) {
            if (gameState.getPlayerOneScore() > gameState.getPlayerTwoScore()) {
                return "Kraj igre | Pobednik: " + round.getGameState().getPlayerOneName();
            } else if (gameState.getPlayerTwoScore() > gameState.getPlayerOneScore()) {
                return "Kraj igre | Pobednik: " + round.getGameState().getPlayerTwoName();
            } else {
                return "Kraj igre | Nerešeno";
            }
        }

        if (round.getGameState().isRoundFinished()) {
            return "Runda " + round.getGameState().getRoundNumber() + " je završena";
        }

        if (round.isGuessWindowOpen()) {
            return "Na potezu: " + getCurrentPlayerName() + " | Pogađaj kolonu/finale ili predaj potez";
        }

        return "Na potezu: " + getCurrentPlayerName() + " | Otvori jedno polje";
    }

    private void switchTurn(Asocijacija round) {
        round.getGameState().switchTurn();
        round.setGuessWindowOpen(false);
    }

    private void awardPoints(TwoPlayerGameState gameState, int player, int points) {
        gameState.addPointsToPlayer(player, points);
    }

    private int calculateColumnPoints(AsocijacijaKolona column) {
        return 2 + column.getUnopenedFieldsCount();
    }

    private int calculateFinalPoints(Asocijacija round) {
        int total = 7;   //konacno resenje

        for (AsocijacijaKolona column : round.getColumns()) {
            if (column.isSolved()) {        //ako je kolona resena, ne boduj je opet
                continue;
            }

            if (column.isUntouched()) {
                total += 6;             //ako nije nista otvarano u koloni
            } else {
                total += calculateColumnPoints(column);     //ako je otvoreno nesto ali nereseno +2 +1 za neotvorena polja
            }
        }

        return total;
    }

    private void revealWholeBoard(Asocijacija round) {
        for (AsocijacijaKolona column : round.getColumns()) {
            column.openAllFields();
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

        private ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResult success(String message) {
            return new ActionResult(true, message);
        }

        public static ActionResult error(String message) {
            return new ActionResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    private String normalizeAnswer(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text.trim().toUpperCase();

        normalized = normalized.replace("Č", "C");
        normalized = normalized.replace("Ć", "C");
        normalized = normalized.replace("Ž", "Z");
        normalized = normalized.replace("Š", "S");
        normalized = normalized.replace("Đ", "DJ");

        return normalized;
    }
}