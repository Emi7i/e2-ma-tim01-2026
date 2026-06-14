package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.mojbroj.MojBroj;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.domain.service.match.MojBrojService;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;
import lombok.Setter;

@HiltViewModel
public class MojBrojViewModel extends ViewModel {
    private static final long STOP_TIMEOUT_MS = 5000L;
    @Getter
    private MojBroj game;

    private CountDownTimer stopTimer;
    private CountDownTimer roundTimer;

    private final UserStatisticsRepository statsRepository;
    private static final String MOCK_USER_ID = "test_user_123";

    private int roundsPlayed = 0;
    private int correctRounds = 0;

    @Inject
    public MojBrojViewModel(UserStatisticsRepository statsRepository){
        this.statsRepository = statsRepository;
    }
    @Getter
    private int goalNumber = 0;

    @Getter
    private List<Integer> singleDigits = new ArrayList<>();
    @Getter
    private List<Integer> doubleDigits = new ArrayList<>();

    @Getter @Setter
    private String opponentAnswer = "";
    @Getter @Setter
    private int myNumber = 0;
    @Getter @Setter
    private int opponentNumber = 0;

    private final MutableLiveData<Boolean> isGoalSpinning = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> areOperandsSpinning = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isCorrect = new MutableLiveData<>();
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gameOver = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> roundOver = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsCorrect() { return isCorrect; }
    public LiveData<Boolean> getIsGoalSpinning(){
        return isGoalSpinning;
    }
    public LiveData<Boolean> getAreOperandsSpinning() { return areOperandsSpinning; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Boolean> getGameOver() { return gameOver; }
    public LiveData<Boolean> getRoundOver() { return roundOver; }

    public void start(MojBroj game) {
        if (this.game == game) return;
        this.game = game;
        startRound();
    }

    private void startRound() {
        game.startNewRound();
        if (game.hasEnded()) {
            gameOver.postValue(true);
            return;
        }

        goalNumber = 0;
        singleDigits = new ArrayList<>();
        doubleDigits = new ArrayList<>();
        opponentAnswer = "";
        myNumber = 0;
        opponentNumber = 0;

        isGoalSpinning.postValue(true);
        areOperandsSpinning.postValue(true);
        isCorrect.postValue(false);
        roundOver.postValue(false);

        startStopTimer(this::stopGoalSpinning);
    }

    /**
     * Starts a 5-second timer that auto-triggers onTimeout if the player
     * doesn't manually call stop in time (spec rule d).
     */
    private void startStopTimer(Runnable onTimeout) {
        if (stopTimer != null) stopTimer.cancel();
        stopTimer = new CountDownTimer(STOP_TIMEOUT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                onTimeout.run();
            }
        };
        stopTimer.start();
    }

    public int generateGoalNumber() {
        goalNumber = game.generateGoalNumber();
        return goalNumber;
    }

    public List<Integer> generateOperands() {
        List<Integer> operands = game.generateOperands();
        singleDigits = operands.subList(0, 4);
        doubleDigits = operands.subList(4, 6);
        return operands;
    }

    /**
     * Called by the fragment's stop button (or shake sensor later) while the
     * goal number is spinning.
     */
    public void stopGoalSpinning() {
        if (stopTimer != null) stopTimer.cancel();
        isGoalSpinning.setValue(false);
        areOperandsSpinning.setValue(true);
        game.saveNumbers();
        startStopTimer(this::stopOperandsSpinning);
    }

    /**
     * Called by the fragment's stop button (or shake sensor later) while the
     * operands are spinning.
     */
    public void stopOperandsSpinning() {
        if (stopTimer != null) stopTimer.cancel();
        areOperandsSpinning.setValue(false);
        game.saveNumbers();
        startRoundTimer();
    }

    /**
     * Hook for the shake sensor (to be wired up later). Triggers whichever
     * "stop" action is currently pending.
     */
    public void onShakeDetected() {
        if (Boolean.TRUE.equals(isGoalSpinning.getValue())) {
            stopGoalSpinning();
        } else if (Boolean.TRUE.equals(areOperandsSpinning.getValue())) {
            stopOperandsSpinning();
        }
    }

    private void startRoundTimer() {
        long roundLengthMs = game.getRoundLength() * 1000L;
        roundTimer = new CountDownTimer(roundLengthMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timeLeft.postValue(0);
                // time ran out without manual submission -> treat as no answer (rule i)
                finishRound(new ArrayList<>());
            }
        };
        roundTimer.start();
    }

    /**
     * Called by the fragment when the user submits an answer (or when the
     * round timer expires with no submission).
     */
    // TODO: refactor to not immediately finish game
    public void checkAnswer(List<String> tokens) {
        if (roundTimer != null) roundTimer.cancel();
        finishRound(tokens);
    }

    private void finishRound(List<String> tokens) {
        roundsPlayed++;
        try {
            Log.d("MojBroj", "Tokens found!! " + tokens);
            game.submitAnswer(tokens);
            myNumber = game.getCurrentPlayerResult();
            boolean correct = myNumber == goalNumber && myNumber != 0;
            if (correct) correctRounds++;
            isCorrect.postValue(correct);
        } catch (IllegalArgumentException e) {
            // invalid expression - treat as no answer (0), per current UI constraints
            Log.d("MojBroj", "invalid >:(: " + e);
            game.submitAnswer(new ArrayList<>());
            myNumber = 0;
            isCorrect.postValue(false);
        }

        opponentNumber = game.getOtherPlayerResult();
        opponentAnswer = String.join(" ", game.getOtherPlayerTokens());

        roundOver.postValue(true);
    }

    private boolean statsUpdated = false;

    public void updateUserStatistics(int player1Score, int player2Score) {
        if (statsUpdated) return;
        statsUpdated = true;
        statsRepository.getStatistics(MOCK_USER_ID).thenAccept(stats -> {
            UserStatistics finalStats = (stats != null) ? stats : UserStatistics.createNew(MOCK_USER_ID);
            
            // Overall match win/loss (Assume we are player 1)
            double oldWinRate = (finalStats.getGamesPlayed() > 0) ? (double) finalStats.getWonGames() / finalStats.getGamesPlayed() * 100.0 : 0.0;
            
            finalStats.setGamesPlayed(finalStats.getGamesPlayed() + 1);
            if (player1Score > player2Score) {
                finalStats.setWonGames(finalStats.getWonGames() + 1);
            }
            double newWinRate = (double) finalStats.getWonGames() / finalStats.getGamesPlayed() * 100.0;
            
            Log.d("UserStats", String.format("Stats changed: Overall Match - Result: %s | Played: %d | Win Rate: %.1f%% -> %.1f%%",
                    (player1Score > player2Score ? "WON" : (player1Score < player2Score ? "LOST" : "DRAW")),
                    finalStats.getGamesPlayed(), oldWinRate, newWinRate));

            // Accuracy Update for Moj Broj
            int gameTotal = roundsPlayed;
            int gameCorrect = correctRounds; // Exact matches
            if (gameTotal > 0) {
                double oldAccuracy = finalStats.getMojBroj();
                long newTotal = finalStats.getMojBrojTotal() + gameTotal;
                long newCorrect = finalStats.getMojBrojCorrect() + gameCorrect;
                finalStats.setMojBrojTotal(newTotal);
                finalStats.setMojBrojCorrect(newCorrect);
                finalStats.setMojBroj((double) newCorrect / newTotal * 100.0);
                double newAccuracy = finalStats.getMojBroj();

                // Track points and game count
                finalStats.setMojBrojPoints(finalStats.getMojBrojPoints() + player1Score); 
                finalStats.setMojBrojPlayed(finalStats.getMojBrojPlayed() + 1);

                Log.d("UserStats", String.format("Stats changed: Moj Broj - Game Correct: %d/%d | Total Accuracy: %.1f%% -> %.1f%%",
                        gameCorrect, gameTotal, oldAccuracy, newAccuracy));
            }
            
            finalStats.calculateOverallStats();
            
            statsRepository.saveStatistics(finalStats).exceptionally(e -> {
                Log.e("UserStats", "Failed to save statistics", e);
                return null;
            });
        });
    }

    /**
     * Called by the fragment after showing round results, to advance to the
     * next round or end the game.
     */
    public void nextRound() {
        startRound();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (stopTimer != null) stopTimer.cancel();
        if (roundTimer != null) roundTimer.cancel();
    }

}
