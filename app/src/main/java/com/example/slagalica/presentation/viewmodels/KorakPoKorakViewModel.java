package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

@HiltViewModel
public class KorakPoKorakViewModel extends ViewModel {
    @Getter
    private KorakPoKorak game;
    private CountDownTimer timer;

    @Inject
    public KorakPoKorakViewModel() {}

    @Getter
    private final List<String> hints = List.of(
            "This is hint 1!", "This is hint 2!", "This is hint 3!", "This is hint 4!", "This is hint 5!",
            "This is hint 6!", "This is hint 7!");
    @Getter
    private final int[] points = {20, 18, 16, 14, 12, 10, 8};
    @Getter
    private final String answer = "sezame";


    // hardcoded for now - stands in for "is it my turn" check
    private static final long LOGGED_IN_USER_ID = 1L;

    private final MutableLiveData<String> latestHint = new MutableLiveData<>();
    private final MutableLiveData<Boolean> revealAllHints = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> stealWindowOpen = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> gameOver = new MutableLiveData<>(false);

    public LiveData<String> getLatestHint() { return latestHint; }
    public LiveData<Boolean> getRevealAllHints() { return revealAllHints; }
    public LiveData<Boolean> getStealWindowOpen() { return stealWindowOpen; }
    public LiveData<Boolean> getGameOver() { return gameOver; }

    public void start(KorakPoKorak game) {
        if (this.game == game) return;
        this.game = game;
        game.startNewRound();
        startTimer();
    }

    private boolean isMyTurn() {
        return game.getCurrentPlayer() == LOGGED_IN_USER_ID;
    }

    private void startTimer() {
        long roundLengthMs = game.getRoundLength() * 1000L;
        long secondsPerHintMs = KorakPoKorak.SECONDS_PER_ANSWER * 1000L;

        timer = new CountDownTimer(roundLengthMs, 1000) {
            long elapsedSeconds = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                elapsedSeconds++;

                if (isMyTurn() && elapsedSeconds % KorakPoKorak.SECONDS_PER_ANSWER == 0) {
                    String hint = game.revealNextHint();
                    latestHint.postValue(hint);
                }
            }

            @Override
            public void onFinish() {
                if (isMyTurn()) {
                    if (!game.isStealOpportunity()) {
                        // round timed out with no correct answer - open steal window for other player
                        game.openStealOpportunity();
                        stealWindowOpen.postValue(true);
                        startStealTimer();
                    } else {
                        // steal window also expired - move to next round
                        advanceRound();
                    }
                }
            }
        };
        timer.start();
    }

    private void startStealTimer() {
        timer = new CountDownTimer(KorakPoKorak.SECONDS_PER_ANSWER * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                if (isMyTurn()) {
                    advanceRound();
                }
            }
        };
        timer.start();
    }

    private void advanceRound() {
        game.startNewRound();
        if (game.hasEnded()) {
            gameOver.postValue(true);
        } else {
            revealAllHints.postValue(false);
            stealWindowOpen.postValue(false);
            startTimer(); // restart for round 2
        }
    }

    /**
     * Called by fragment when user submits an answer.
     */
    public void submitAnswer(String answer) {
        boolean correct = game.isAnswerCorrect(answer);
        if (correct) {
            timer.cancel();
            revealAllHints.postValue(true);
            advanceRound();
        }
        // if incorrect: nothing happens, fragment shows red flash, timer keeps running
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
}
