package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.mojbroj.MojBroj;
import com.example.slagalica.domain.service.match.MojBrojService;

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

    @Inject
    public MojBrojViewModel(){

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
        areOperandsSpinning.postValue(false);
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
        try {
            Log.d("MojBroj", "Tokens found!! " + tokens);
            game.submitAnswer(tokens);
            myNumber = game.getCurrentPlayerResult();
            isCorrect.postValue(myNumber == goalNumber && myNumber != 0);
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
